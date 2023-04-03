package com.github.taiem92dn.templatepluginsetup.pagingdb

import com.android.tools.idea.wizard.template.ModuleTemplateData
import com.android.tools.idea.wizard.template.RecipeExecutor
import com.android.tools.idea.wizard.template.impl.activities.common.addAllKotlinDependencies
import com.github.taiem92dn.templatepluginsetup.listapi.addListIntoNavigationXml
import com.github.taiem92dn.templatepluginsetup.listapi.addStringsXml
import com.github.taiem92dn.templatepluginsetup.listapi.contentApiXml
import com.github.taiem92dn.templatepluginsetup.listapi.itemListXml
import com.github.taiem92dn.templatepluginsetup.listapi.listFragmentXml
import com.github.taiem92dn.templatepluginsetup.listapi.listResponseFile
import com.github.taiem92dn.templatepluginsetup.listapi.updateNetworkModuleFile
import com.github.taiem92dn.templatepluginsetup.paging.dataSourceFile
import com.github.taiem92dn.templatepluginsetup.paging.itemLoadStateAdapter
import com.github.taiem92dn.templatepluginsetup.paging.itemLoadStateViewHolder
import com.github.taiem92dn.templatepluginsetup.paging.listLoadStateFooterViewXml
import com.github.taiem92dn.templatepluginsetup.paging.recyclerViewAdapterFile
import com.github.taiem92dn.templatepluginsetup.paging.remoteDataSourceFile
import com.github.taiem92dn.templatepluginsetup.paging.retrofitServiceFile
import com.github.taiem92dn.templatepluginsetup.paging.viewModelList
import java.io.File

fun RecipeExecutor.pagingDatabaseRecipe(
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
    addDatabaseFile: Boolean,
    databaseName: String
) {
    val (projectData, srcOut, resOut) = moduleData
    val manifestOut = moduleData.manifestDir

    addAllKotlinDependencies(moduleData)

    // add repository file
    if (addRepository) {
        save(
            repositoryFile(packageName, itemName, databaseName),
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
        itemModelDbFile(packageName, itemName),
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
        pagingDbFragmentFile(packageName, itemName),
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

    val pagingVersion = "3.1.1"
    addDependency("androidx.paging:paging-runtime-ktx:$pagingVersion")

    save(
        remoteMediatorFile(packageName, itemName, databaseName),
        srcOut.resolve("data/${itemName}RemoteMediator.kt")
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

    // ============ Database part ============
    val roomVersion = "2.4.0-alpha03"
    addDependency("androidx.room:room-runtime:$roomVersion")
    addDependency("androidx.room:room-ktx:$roomVersion")
    addDependency("androidx.room:room-compiler:$roomVersion", "kapt")

//    need to add this line to build.gradle
//    freeCompilerArgs += ["-Xopt-in=kotlin.RequiresOptIn"]
    modifyBuildGradleFile(moduleData.rootDir)

    if (addDatabaseFile) {
        save(
            pagingDatabaseFile(packageName, itemName, databaseName),
            srcOut.resolve("db/${databaseName}Database.kt")
        )
    }

    save(
        remoteKeysDaoFile(packageName, itemName),
        srcOut.resolve("db/Remote${itemName}KeysDao.kt")
    )

    save(
        remoteKeyModelFile(packageName, itemName),
        srcOut.resolve("db/Remote${itemName}Keys.kt")
    )

    save(
        itemDbDaoFile(packageName, itemName),
        srcOut.resolve("db/${itemName}Dao.kt")
    )

    // update AppModule file
    // add a few code lines in NetworkModule.kt
    val newContent = srcOut.resolve("di/module/AppModule.kt")
        .readText()
        .replace("\n}",
            "\n${addProviderInAppModule(packageName, itemName, databaseName)}\n }"
        )
        .replace("import android.app.Application\n" +
                "import dagger.Module",
            """import android.app.Application
import $packageName.db.${databaseName}Database
import dagger.Module
"""
        )

    srcOut.resolve("di/module/AppModule.kt").writeText(newContent)
}

fun modifyBuildGradleFile(rootDir: File) {
    val newContent = rootDir.resolve("build.gradle")
        .readText()
        .replace("kotlinOptions {\n" +
                "        jvmTarget = '1.8'\n" +
                "    }",
            "kotlinOptions {\n" +
                    "        jvmTarget = '1.8'\n" +
                    "        freeCompilerArgs += [\"-Xopt-in=kotlin.RequiresOptIn\"]\n" +
                    "    }"
        )


    rootDir.resolve("build.gradle").writeText(newContent)
}