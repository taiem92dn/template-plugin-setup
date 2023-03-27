package com.github.taiem92dn.templatepluginsetup.mvvm

import com.android.tools.idea.wizard.template.ModuleTemplateData
import com.android.tools.idea.wizard.template.RecipeExecutor
import com.android.tools.idea.wizard.template.impl.activities.common.addAllKotlinDependencies

fun RecipeExecutor.mvvmTemplateSetup(
    moduleData: ModuleTemplateData,
    packageName: String,
    addNetwork: Boolean,
    overWriteBuildGradle: Boolean,
    baseUrl: String,
    entityName: String,
) {
    val (projectData, srcOut, resOut) = moduleData
    val manifestOut = moduleData.manifestDir

    addAllKotlinDependencies(moduleData)

    // This will generate new manifest (with activity) to merge it with existing
//    generateManifest(moduleData, activityClass, packageName,
//        isLauncher = false, hasNoActionBar = true, generateActivityTitle = true)

    val kotlin_version = "1.5.31"
    val hilt_ex_version = "1.0.0-alpha03"
    val dagger_hilt_version = "2.38.1"
    val navigation_version = "2.5.3"

    if (!overWriteBuildGradle) {
        applyPlugin("kotlin-kapt", revision = null)
        applyPlugin("dagger.hilt.android.plugin", revision = null)
        applyPlugin("androidx.navigation.safeargs.kotlin", revision = null)
        applyPlugin("kotlin-parcelize", revision = null)

        addClasspathDependency("com.google.dagger:hilt-android-gradle-plugin:$dagger_hilt_version")
        addClasspathDependency("androidx.navigation:navigation-safe-args-gradle-plugin:$navigation_version")

        addDependency("androidx.navigation:navigation-fragment-ktx:$navigation_version")
        addDependency("androidx.navigation:navigation-ui-ktx:$navigation_version")

        addDependency("com.google.dagger:hilt-android:$dagger_hilt_version")
        addDependency("com.google.dagger:hilt-android-compiler:$dagger_hilt_version", "kapt")
        addDependency("androidx.hilt:hilt-lifecycle-viewmodel:$hilt_ex_version")
        addDependency("androidx.hilt:hilt-compiler:$hilt_ex_version", "kapt")
    }

    save(
        appFile(packageName, projectData),
        srcOut.resolve("App.kt")
    )

    save(
        appModuleFile(packageName, projectData),
        srcOut.resolve("di/module/AppModule.kt")
    )

    mergeXml(
        source = addAppNameManifest(),
        to = manifestOut.resolve("AndroidManifest.xml")
    )

    mergeXml(
        source = addDimensXml(),
        to = resOut.resolve("values/dimens.xml")
    )

    resOut.resolve("layout/activity_main.xml").writeText(
        activityMainFileXml()
    )

    srcOut.resolve("MainActivity.kt").writeText(
        mainActivityFile(packageName)
    )

    if (overWriteBuildGradle) {
        // overwrite build.gradle file of project
        moduleData.rootDir.resolve("../build.gradle").writeText(
            buildGradleProject(kotlin_version, dagger_hilt_version, navigation_version)
        )

        // overwrite build.gradle file
        moduleData.rootDir.resolve("build.gradle").writeText(
            buildGradleModule(packageName, addNetwork)
        )
    }

    if (addNetwork) {
        val retrofit_version = "2.9.0"
        val okhttp_version = "4.9.1"
        val stetho_version = "1.5.1"

        mergeXml(
            source = addPermissionManifest(),
            to = manifestOut.resolve("AndroidManifest.xml")
        )

        if (!overWriteBuildGradle) {
            // network
            addDependency("com.squareup.retrofit2:retrofit:$retrofit_version")
            addDependency("com.squareup.retrofit2:converter-gson:$retrofit_version")
            addDependency("com.squareup.okhttp3:logging-interceptor:$okhttp_version")

            // Stetho (debug tool)
            addDependency("com.facebook.stetho:stetho:$stetho_version")
            addDependency("com.facebook.stetho:stetho-okhttp3:$stetho_version")
        }

        save(
            networkModuleFile(packageName, baseUrl, projectData),
            srcOut.resolve("di/module/NetworkModule.kt")
        )

        save(
            apiResourceFile(packageName, projectData),
            srcOut.resolve("network/ApiResource.kt")
        )

        save(
            inetworkCheckServiceFile(packageName, projectData),
            srcOut.resolve("network/INetworkCheckService.kt")
        )

        save(
            utilsFile(packageName, projectData),
            srcOut.resolve("util/Utils.kt")
        )
    }
}
