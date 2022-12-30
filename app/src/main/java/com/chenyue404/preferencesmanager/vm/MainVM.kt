package com.chenyue404.preferencesmanager.vm

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.chenyue404.preferencesmanager.entity.AppItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Created by cy on 2022/12/29.
 */
class MainVM : ViewModel() {
    private val _appListState = mutableStateOf<List<AppItem>>(emptyList())
    val appListState: State<List<AppItem>> = _appListState

    suspend fun readAppList(context: Context) = withContext(Dispatchers.IO) {
        val installedPackages = context.packageManager.getInstalledPackages(0)
        _appListState.value = installedPackages.map {
            AppItem.create(it, context)
        }
//            .sortedBy { it.headerChar }
            .sortedWith(compareBy({ it.headerChar }, { it.appName }))
    }
}