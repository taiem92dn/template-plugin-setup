package com.github.taiem92dn.templatepluginsetup.mvvm

import com.android.tools.idea.wizard.template.ProjectTemplateData

fun appFile(
    packageName: String,
    projectData: ProjectTemplateData
) = """package $packageName

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App: Application() {

    override fun onCreate() {
        super.onCreate()
    }
}"""

fun appModuleFile(
    packageName: String,
    projectData: ProjectTemplateData
) = """package $packageName.di.module

import android.app.Application
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    @Named("ioDispatcher")
    fun provideIoDispatcher() = Dispatchers.IO
}
"""

fun networkModuleFile(
    packageName: String,
    projectData: ProjectTemplateData
) = """package $packageName.di.module

import android.app.Application
import com.facebook.stetho.okhttp3.StethoInterceptor
import $packageName.network.INetworkCheckService
import $packageName.util.Utils
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    @Named("non_auth_client")
    fun provideNonAuthOkHttpClient(): OkHttpClient {
        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BASIC)

        return OkHttpClient.Builder()
            .addInterceptor(interceptor)
            // use for Stetho
            .addNetworkInterceptor(StethoInterceptor())
            .build()
    }

    @Provides
    @Singleton
    fun provideNetworkCheckService(application: Application): INetworkCheckService {
        return object : INetworkCheckService {
            override fun hasInternet(): Boolean {
                return Utils.hasInternet(application)
            }
        }
    }
}"""

fun apiResourceFile(
    packageName: String,
    projectData: ProjectTemplateData
) = """package $packageName.network

sealed class ApiResource<T>(
    val data : T? = null,
    val message : String? = null
) {
    class Success<T>(data : T) : ApiResource<T>(data)
    class Loading<T>(data : T? = null) : ApiResource<T>(data)
    class NoInternet<T>(data : T? = null) : ApiResource<T>(data)
    class Error<T>(message: String?, data: T? = null) : ApiResource<T>(data, message)
}"""

fun inetworkCheckServiceFile(
    packageName: String,
    projectData: ProjectTemplateData
) = """package $packageName.network

interface INetworkCheckService {
    fun hasInternet(): Boolean
}"""

fun utilsFile(
    packageName: String,
    projectData: ProjectTemplateData
) = """package $packageName.util

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

object Utils {
    @JvmStatic
    fun hasInternet(app: Application) : Boolean {
        val cm = app.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = cm.activeNetwork ?: return false
            val networkCapabilities = cm.getNetworkCapabilities(activeNetwork) ?: return false
            return when {
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
                else -> false
            }
        } else {
            val nwInfo = cm.activeNetworkInfo ?: return false
            return nwInfo.isConnected
        }

    }
}"""

fun addPermissionManifest() = """
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
</manifest>
"""

fun addAppNameManifest() = """
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">
    <application
        android:name=".App" >
    </application>
</manifest>
""".trimIndent()