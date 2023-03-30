package com.github.taiem92dn.templatepluginsetup.paging

import com.android.tools.idea.wizard.template.ModuleTemplateData
import com.android.tools.idea.wizard.template.RecipeExecutor
import com.android.tools.idea.wizard.template.impl.activities.common.addAllKotlinDependencies
import com.github.taiem92dn.templatepluginsetup.listapi.addListIntoNavigationXml
import com.github.taiem92dn.templatepluginsetup.listapi.addStringsXml
import com.github.taiem92dn.templatepluginsetup.listapi.contentApiXml
import com.github.taiem92dn.templatepluginsetup.listapi.itemListXml
import com.github.taiem92dn.templatepluginsetup.listapi.itemModelFile
import com.github.taiem92dn.templatepluginsetup.listapi.listFragmentXml
import com.github.taiem92dn.templatepluginsetup.listapi.listResponseFile
import com.github.taiem92dn.templatepluginsetup.listapi.updateNetworkModuleFile

fun RecipeExecutor.pagingRecipe(
    moduleData: ModuleTemplateData,
    packageName: String,
    itemName: String,
    addCoordinator: Boolean,
    addBackButton: Boolean,
    addRepository: Boolean,
    addContentApiXml: Boolean,
    addRetrofitService: Boolean,
    addListResponseFile: Boolean,
    addPagingCommonFile: Boolean,
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

    updateNetworkModuleFile(packageName, itemName, srcOut)

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
    // ========== Paging part ===========

    addDependency("androidx.paging:paging-runtime-ktx:3.1.1")

    save(
        pagingSourceFile(packageName, itemName),
        srcOut.resolve("data/${itemName}PagingSource.kt")
    )

    if (addPagingCommonFile) {
        save(
            itemLoadStateAdapter(packageName, itemName),
            srcOut.resolve("ui/${itemName}list/adapter/ItemsLoadStateAdapter.kt")
        )

        save(
            itemLoadStateViewHolder(packageName, itemName),
            srcOut.resolve("ui/${itemName}list/adapter/ItemsLoadStateViewHolder.kt")
        )

        save(
            listLoadStateFooterViewXml(packageName, itemName),
            resOut.resolve("layout/list_load_state_footer_view_item.xml")
        )
    }
}