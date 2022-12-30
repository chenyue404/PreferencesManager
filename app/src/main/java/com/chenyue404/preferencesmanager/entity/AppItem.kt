package com.chenyue404.preferencesmanager.entity

import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageInfo
import android.net.Uri
import com.github.promeg.pinyinhelper.Pinyin

/**
 * Created by cy on 2022/12/29.
 */
private val AArray = arrayOf('Á', 'À', 'Â', 'Ã', 'Ä')
private val EArray = arrayOf('É', 'È', 'Ê', 'Ë')

data class AppItem(
    val pkgName: String,
    val appName: String,
    val headerChar: Char,
    val iconUri: Uri,
) {
    companion object {
        fun create(packageInfo: PackageInfo, context: Context): AppItem {
            val pkgName = packageInfo.packageName
            val applicationInfo = packageInfo.applicationInfo
            val appName = applicationInfo.loadLabel(context.packageManager).toString()

            val firstChar = with(appName.first()) {
                when {
                    this in '0'..'9' -> '#'
                    AArray.contains(this) -> 'A'
                    EArray.contains(this) -> 'E'
                    else -> this
                }
            }
            val headerChar =
                firstChar.takeIf { Pinyin.isChinese(it) }
                    ?.let { Pinyin.toPinyin(it).first() }
                    ?: firstChar.uppercaseChar()

            val iconUri = Uri.Builder()
                .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                .authority(pkgName)
                .appendPath(applicationInfo.icon.toString())
                .build()

            return AppItem(
                pkgName,
                appName,
                headerChar,
                iconUri
            )
        }
    }
}
