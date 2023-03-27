package com.github.taiem92dn.templatepluginsetup.listapi

import com.android.tools.idea.wizard.template.ModuleTemplateData
import com.android.tools.idea.wizard.template.RecipeExecutor
import com.android.tools.idea.wizard.template.impl.activities.common.addAllKotlinDependencies

fun RecipeExecutor.listApiRecipe(
    moduleData: ModuleTemplateData,
    packageName: String,
    itemName: String,
    addCoordinator: Boolean,
    addBackButton: Boolean,
    addRepository: Boolean,
    addContentApiXml: Boolean,
    addRetrofitService: Boolean,
    addListResponseFile: Boolean,
    apiPath: String,
) {
    val (projectData, srcOut, resOut) = moduleData
    val manifestOut = moduleData.manifestDir

    addAllKotlinDependencies(moduleData)

    // add repository file
    if (addRepository) {
        save(
            repositoryFile(packageName, itemName),
            srcOut.resolve("repository/${itemName}Repository.kt")
        )
    }

    // add a few code lines in NetworkModule.kt
    val newContent = srcOut.resolve("di/module/NetworkModule.kt")
            .readText()
            .replace("NetworkModule {",
                "NetworkModule  {${addDependenciesInNetworkModule(packageName, itemName)}"
            )

    srcOut.resolve("di/module/NetworkModule.kt").writeText(newContent)

    // data directory files
    if (addRepository) {
        save(
            dataSourceFile(packageName, itemName),
            srcOut.resolve("data/${itemName}DataSource.kt")
        )
        save(
            remoteDataSourceFile(packageName, itemName),
            srcOut.resolve("data/Remote${itemName}DataSource.kt")
        )
    }

    // save model file
    save(
        itemModelFile(packageName, itemName),
        srcOut.resolve("model/${itemName}Item.kt")
    )
    // save ListResponse File
    if (addListResponseFile) {
        save(
            listResponseFile(packageName, itemName),
            srcOut.resolve("model/ListResponse.kt")
        )
    }

    if (addRetrofitService) {
        // network directory
        save(
            retrofitServiceFile(packageName, itemName, apiPath),
            srcOut.resolve("network/${itemName}Service.kt")
        )
    }


    // add ui files
    save(
        recyclerViewAdapterFile(packageName, itemName),
        srcOut.resolve("ui/${itemName.lowercase()}list/adapter/${itemName}ListAdapter.kt")
    )
    save(
        listFragmentFile(packageName, itemName),
        srcOut.resolve("ui/${itemName}list/${itemName}ListFragment.kt")
    )
    save(
        viewModelList(packageName, itemName),
        srcOut.resolve("ui/${itemName}list/${itemName}ListViewModel.kt")
    )

    // add xml files
    if (addContentApiXml) {
        save(
            contentApiXml(packageName, itemName),
            resOut.resolve("layout/content_api_list.xml")
        )
    }

    if (addCoordinator) {
        save(
            listFragmentXml(packageName, itemName, addBackButton),
            resOut.resolve("layout/fragment_${itemName.lowercase()}_list.xml")
        )
    }
    else {
        if (!addContentApiXml) {
            save(
                contentApiXml(packageName, itemName),
                resOut.resolve("layout/fragment_${itemName.lowercase()}_list.xml")
            )
        }
    }

    save(
        itemListXml(packageName, itemName),
        resOut.resolve("layout/item_${itemName.lowercase()}_list.xml")
    )

    mergeXml(
        source = addStringsXml(itemName),
        to = resOut.resolve("values/strings.xml")
    )

    mergeXml(
        source = addListIntoNavigationXml(packageName, itemName),
        to = resOut.resolve("navigation/nav_graph.xml")
    )
}