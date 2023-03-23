package com.github.taiem92dn.templatepluginsetup.mvvm

fun buildGradleProject(
    kotlin_version: String,
    dagger_hilt_version: String,
    nav_version: String
) = """
// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    ext.kotlin_version = "$kotlin_version"
    ext.dagger_hilt_version = "$dagger_hilt_version"
    ext.nav_version = "$nav_version"

    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath "com.android.tools.build:gradle:7.0.4"
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:${'$'}kotlin_version"
        classpath "com.google.dagger:hilt-android-gradle-plugin:${'$'}dagger_hilt_version"
        classpath "androidx.navigation:navigation-safe-args-gradle-plugin:${'$'}nav_version"

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

plugins {
    id 'com.android.application' version '7.4.1' apply false
    id 'com.android.library' version '7.4.1' apply false
}
 
""".trimIndent()

fun buildGradleModule(
    packageName: String,
    isAddNetwork: Boolean,
) = """
plugins {
    id 'com.android.application'
    id 'kotlin-android'
    id 'kotlin-kapt'
    id 'dagger.hilt.android.plugin'
    id "androidx.navigation.safeargs.kotlin"
    id 'kotlin-parcelize'
}

android {
    namespace '$packageName'
    compileSdk 33

    defaultConfig {
        applicationId "$packageName"
        minSdk 21
        targetSdk 33
        versionCode 1
        versionName "1.0"

        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = '1.8'
    }
    buildFeatures {
        viewBinding true
        dataBinding true
    }
}

ext {

    // Appcompat
    appcompat_version = "1.3.1"

    corektx_version = "1.9.0"
    lifecycle_version = "2.5.1"
    // di
    hilt_ex_version = "1.0.0-alpha03"

    coroutines_android_version = '1.3.2'

    ${if (isAddNetwork) { """
    // network
    retrofit_version = "2.9.0"
    okhttp_version = "4.9.1"       
    """
        }
        else ""
    }

    // ui
    material_version = "1.8.0"

    timber_version = "5.0.1"
    ${if (isAddNetwork) { """
    // stetho
    stetho_version = "1.5.1"
    """
        }
        else ""
    }
    // unit test
    junit_version = "4.13.2"
    androidx_test_version = '1.4.0'
}

dependencies {
    implementation fileTree(dir: "libs", include: ["*.jar"])
    implementation "org.jetbrains.kotlin:kotlin-stdlib:${'$'}kotlin_version"
    implementation "androidx.core:core-ktx:${'$'}corektx_version"

    // architect component
    implementation "androidx.lifecycle:lifecycle-viewmodel-ktx:${'$'}lifecycle_version"
    implementation "androidx.lifecycle:lifecycle-livedata-ktx:${'$'}lifecycle_version"
    implementation "androidx.lifecycle:lifecycle-viewmodel-savedstate:${'$'}lifecycle_version"
    implementation 'androidx.navigation:navigation-fragment-ktx:2.5.3'
    implementation 'androidx.navigation:navigation-ui-ktx:2.5.3'
    implementation "androidx.lifecycle:lifecycle-common-java8:${'$'}lifecycle_version"

    // di
    implementation "com.google.dagger:hilt-android:${'$'}dagger_hilt_version"
    implementation 'androidx.legacy:legacy-support-v4:1.0.0'
    kapt "com.google.dagger:hilt-android-compiler:${'$'}dagger_hilt_version"
    implementation "androidx.hilt:hilt-lifecycle-viewmodel:${'$'}hilt_ex_version"
    kapt "androidx.hilt:hilt-compiler:${'$'}hilt_ex_version"

    // multidex
    implementation 'androidx.multidex:multidex:2.0.1'

    // Coroutines
    implementation "org.jetbrains.kotlinx:kotlinx-coroutines-core:${'$'}coroutines_android_version"
    implementation "org.jetbrains.kotlinx:kotlinx-coroutines-android:${'$'}coroutines_android_version"

    ${if (isAddNetwork) { """
    // network
    implementation "com.squareup.retrofit2:retrofit:${'$'}retrofit_version"
    implementation "com.squareup.retrofit2:converter-gson:${'$'}retrofit_version"
    implementation "com.squareup.okhttp3:logging-interceptor:${'$'}okhttp_version"
    """
    }
    else ""
    }

    // Gson
    implementation 'com.google.code.gson:gson:2.10.1'

    // ui
//    implementation 'androidx.constraintlayout:constraintlayout:2.1.1'
    implementation "androidx.recyclerview:recyclerview:1.2.1"
    implementation "androidx.swiperefreshlayout:swiperefreshlayout:1.1.0"
    implementation "com.google.android.material:material:${'$'}material_version"

    ${if (isAddNetwork) { """
    // Stetho (debug tool)
    implementation "com.facebook.stetho:stetho:${'$'}stetho_version"
    implementation "com.facebook.stetho:stetho-okhttp3:${'$'}stetho_version"
    """
    }
    else ""
    }
    
    // Timber logger
    implementation "com.jakewharton.timber:timber:${'$'}timber_version"

    // test
    testImplementation "junit:junit:${'$'}junit_version"
//    testImplementation "androidx.room:room-testing:${'$'}room_version"
    androidTestImplementation 'androidx.test.ext:junit:1.1.3'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.4.0'
//    testImplementation "androidx.paging:paging-common:${'$'}paging_version"
    androidTestImplementation "androidx.test:rules:${'$'}androidx_test_version"
}""".trimIndent()
