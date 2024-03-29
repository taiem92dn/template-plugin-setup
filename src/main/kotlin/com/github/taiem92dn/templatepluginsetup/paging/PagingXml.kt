package com.github.taiem92dn.templatepluginsetup.paging

import com.github.taiem92dn.templatepluginsetup.utils.lowercaseFirstLetter

fun listLoadStateFooterViewXml(
    packageName: String,
    itemName: String
) = """
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    xmlns:tools="http://schemas.android.com/tools"
    android:orientation="vertical"
    android:padding="8dp">
    <TextView
        android:id="@+id/error_msg"
        android:textColor="?android:textColorPrimary"
        android:textSize="16sp"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="center"
        android:textAlignment="center"
        tools:text="Timeout"/>
    <ProgressBar
        android:id="@+id/progress_bar"
        style="?android:attr/progressBarStyle"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_gravity="center"/>
    <Button
        android:id="@+id/retry_button"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="center"
        android:text="@string/retry_button"/>
</LinearLayout>
""".trimIndent()
