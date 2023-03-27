package com.github.taiem92dn.templatepluginsetup.utils

fun String.lowercaseFirstLetter(): String {
    return this.substring(0, 1).lowercase() + this.substring(1, this.length)
}