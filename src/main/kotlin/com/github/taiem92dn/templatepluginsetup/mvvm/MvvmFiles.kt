package com.github.taiem92dn.templatepluginsetup.mvvm

import com.android.tools.idea.wizard.template.ProjectTemplateData

fun appFile(
    packageName: String,
    projectData: ProjectTemplateData
) = """package $packageName

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class App: Application() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG)
            Timber.plant(Timber.DebugTree())
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
    baseUrl: String,
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
    @Named("non_auth_retrofit")
    fun provideNonAuthRetrofit(@Named("non_auth_client") client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("$baseUrl")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

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
    <uses-permission android:name="android.permission.INTERNET" />
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

fun addDimensXml() = """
<resources>
    <dimen name="margin_large">32dp</dimen>
    <dimen name="margin_normal">16dp</dimen>
    <dimen name="margin_small">8dp</dimen>
    <dimen name="margin_extra_small">4dp</dimen>
</resources> 
""".trimIndent()

fun activityMainFileXml() = """
<?xml version="1.0" encoding="utf-8"?>
<androidx.coordinatorlayout.widget.CoordinatorLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:fitsSystemWindows="true"
    tools:context=".MainActivity">

<!--    <com.google.android.material.appbar.AppBarLayout-->
<!--        android:layout_width="match_parent"-->
<!--        android:layout_height="wrap_content"-->
<!--        android:theme="@style/Theme.AppTheme.AppBarOverlay">-->

<!--        <androidx.appcompat.widget.Toolbar-->
<!--            android:id="@+id/toolbar"-->
<!--            android:layout_width="match_parent"-->
<!--            android:layout_height="?attr/actionBarSize"-->
<!--            android:background="?attr/colorPrimary"-->
<!--            app:popupTheme="@style/Theme.AppTheme.PopupOverlay" />-->

<!--    </com.google.android.material.appbar.AppBarLayout>-->

    <include layout="@layout/content_main" />

</androidx.coordinatorlayout.widget.CoordinatorLayout>
""".trimIndent()

fun mainActivityFile(packageName: String) = """
package $packageName

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import $packageName.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val navController = findNavController(R.id.nav_host_fragment_content_main)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}
""".trimIndent()



