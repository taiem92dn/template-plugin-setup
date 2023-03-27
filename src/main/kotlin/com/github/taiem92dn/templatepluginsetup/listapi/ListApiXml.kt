package com.github.taiem92dn.templatepluginsetup.listapi

import com.github.taiem92dn.templatepluginsetup.utils.lowercaseFirstLetter

fun itemListXml(
    packageName: String,
    itemName: String
) = """
<?xml version="1.0" encoding="utf-8"?>
<layout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools">

    <data>

        <import type="$packageName.model.${itemName}Item" />

        <variable
            name="${itemName.lowercaseFirstLetter()}Item"
            type="${itemName}Item" />

    </data>


    <androidx.cardview.widget.CardView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginStart="@dimen/margin_small"
        android:layout_marginTop="@dimen/margin_extra_small"
        android:layout_marginEnd="@dimen/margin_small"
        android:layout_marginBottom="@dimen/margin_extra_small"
        app:cardCornerRadius="5dp">

        <androidx.constraintlayout.widget.ConstraintLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:padding="@dimen/margin_small">

            <TextView
                android:id="@+id/tv${itemName}"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:textAppearance="@style/TextAppearance.Material3.BodySmall"
                app:layout_constraintEnd_toEndOf="parent"
                app:layout_constraintTop_toTopOf="parent"
                tools:text="Text View" />

        </androidx.constraintlayout.widget.ConstraintLayout>
    </androidx.cardview.widget.CardView>


</layout>
""".trimIndent()

fun listFragmentXml(
    packageName: String,
    itemName: String,
    useBackButton: Boolean
) = """<?xml version="1.0" encoding="utf-8"?>
<layout>

    <data>

        <import type="androidx.lifecycle.LiveData" />

        <import type="android.view.View" />

        <variable
            name="errorMessage"
            type="LiveData&lt;String>" />

        <variable
            name="showError"
            type="LiveData&lt;Boolean>" />
    </data>

    <androidx.coordinatorlayout.widget.CoordinatorLayout xmlns:android="http://schemas.android.com/apk/res/android"
        xmlns:app="http://schemas.android.com/apk/res-auto"
        xmlns:tools="http://schemas.android.com/tools"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        tools:context=".ui.${itemName.lowercase()}list.${itemName}ListFragment">

        <com.google.android.material.appbar.AppBarLayout
            android:layout_width="match_parent"
            android:layout_height="?attr/actionBarSize"
            android:visibility="gone"
            android:background="@android:color/transparent"
            app:elevation="0dp">
            ${if (useBackButton) { """
            <ImageView
                android:id="@+id/ivBack"
                android:layout_width="48dp"
                android:layout_height="48dp"
                android:padding="10dp"
                android:background="?attr/selectableItemBackgroundBorderless"
                android:src="@drawable/ic_arrow_back_24"
                app:layout_scrollFlags="scroll|enterAlwaysCollapsed"
                />
                    """.trimIndent()
                }
                else ""
            }
        </com.google.android.material.appbar.AppBarLayout>

        <include
            android:id="@+id/contentList"
            layout="@layout/content_api_list"
            app:showError="@{showError}"
            app:errorMessage="@{errorMessage}"/>

    </androidx.coordinatorlayout.widget.CoordinatorLayout>
</layout>
""".trimIndent()

fun contentApiXml(
    packageName: String,
    itemName: String
) = """<?xml version="1.0" encoding="utf-8"?>
<layout>
    <data>

        <import type="androidx.lifecycle.LiveData" />

        <import type="android.view.View" />

        <variable
            name="errorMessage"
            type="LiveData&lt;String>" />

        <variable
            name="showError"
            type="LiveData&lt;Boolean>" />
    </data>

    <FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
        xmlns:app="http://schemas.android.com/apk/res-auto"
        xmlns:tools="http://schemas.android.com/tools"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:layout_behavior="@string/appbar_scrolling_view_behavior">

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:textAppearance="@style/TextAppearance.MaterialComponents.Headline3"
            />

        <androidx.swiperefreshlayout.widget.SwipeRefreshLayout
            android:id="@+id/srlList"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            app:layout_constraintBottom_toBottomOf="parent"
            app:layout_constraintEnd_toEndOf="parent"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintTop_toTopOf="parent">

            <androidx.recyclerview.widget.RecyclerView
                android:id="@+id/rvList"
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                app:layoutManager="androidx.recyclerview.widget.LinearLayoutManager"/>
        </androidx.swiperefreshlayout.widget.SwipeRefreshLayout>

        <ProgressBar
            android:id="@+id/pbList"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:visibility="gone"
            app:layout_constraintBottom_toBottomOf="parent"
            app:layout_constraintEnd_toEndOf="parent"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintTop_toTopOf="parent" />

        <LinearLayout
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_gravity="center"
            android:layout_margin="@dimen/margin_large"
            android:gravity="center"
            android:orientation="vertical"
            android:visibility="@{showError ? View.VISIBLE : View.GONE}"
            tools:visibility="gone">

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:gravity="center"
                android:text="@{errorMessage}"
                android:textAppearance="@style/TextAppearance.AppCompat.Medium"
                tools:text="@string/no_internet" />

            <Button
                android:id="@+id/btRetry"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginTop="@dimen/margin_normal"
                android:padding="@dimen/margin_normal"
                android:text="@string/retry_button"
                android:textAllCaps="false"
                android:textAppearance="@style/TextAppearance.AppCompat.Medium" />

        </LinearLayout>
    </FrameLayout>
</layout> 
""".trimIndent()

fun addStringsXml(
    itemName: String
) = """
<resources>
    <string name="no_internet">No Internet connection. Make sure that Wifi or mobile data is turned on, then try again.</string>
    <string name="error_loading_list_tap_to_retry">Error Loading\nTap to retry</string>
    <string name="retry_button">Retry</string>
    <string name="unable_to_get_data">Unable to get data</string>
    
    <string name="${itemName.lowercase()}list_fragment_label">$itemName List Fragment</string>
</resources> 
""".trimIndent()

fun addListIntoNavigationXml(
    packageName: String,
    itemName: String
) = """
<navigation>
    <fragment
        android:id="@+id/${itemName}ListFragment"
        android:name="$packageName.ui.${itemName.lowercase()}list.${itemName}ListFragment"
        android:label="@string/${itemName.lowercase()}list_fragment_label"
        tools:layout="@layout/fragment_${itemName.lowercase()}_list" >
    </fragment>
</navigation>
""".trimIndent()
