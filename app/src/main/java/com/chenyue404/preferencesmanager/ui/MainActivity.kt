@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)

package com.chenyue404.preferencesmanager.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.chenyue404.androidlib.extends.indexOfAll
import com.chenyue404.androidlib.extends.launch
import com.chenyue404.preferencesmanager.BuildConfig
import com.chenyue404.preferencesmanager.R
import com.chenyue404.preferencesmanager.entity.AppItem
import com.chenyue404.preferencesmanager.ui.theme.PreferencesManagerTheme
import com.chenyue404.preferencesmanager.vm.MainVM

@OptIn(ExperimentalFoundationApi::class)
class MainActivity : ComponentActivity() {

    private val vm: MainVM by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PreferencesManagerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("Android")
                }
            }
        }
        lifecycle.launch {
            vm.readAppList(this@MainActivity)
        }
    }

    @Composable
    private fun Greeting(name: String) {
//    Text(text = "Hello $name!")
        Scaffold(
            topBar = { BuildTopBar() },
            content = { paddingValues -> BuildContent(paddingValues) },
        )
    }

    @Preview(showBackground = true)
    @Composable
    private fun DefaultPreview() {
        PreferencesManagerTheme {
            Greeting("Android")
        }
    }

    @Composable
    private fun BuildTopBar() {
        val appName = stringResource(R.string.app_name)
        val mContext = LocalContext.current
        val keyboardController = LocalSoftwareKeyboardController.current
        val focusRequester = remember { FocusRequester() }
        TopAppBar(
            title = {
                AnimatedVisibility(
                    !vm.isSearching.value,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    Text(text = "$appName(${BuildConfig.BUILD_TYPE})")
                }
                AnimatedVisibility(
                    vm.isSearching.value,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    TextField(
                        placeholder = { Text(stringResource(id = R.string.search_hint)) },
                        value = vm.keyword.value,
                        onValueChange = {
                            vm.keyword.value = it
                        },
                        leadingIcon = {
                            if (vm.isSearching.value) {
                                IconButton(onClick = { closeSearch() }) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = null
                                    )
                                }
                            }
                        },
                        trailingIcon = {
                            if (vm.keyword.value.isNotEmpty()) {
                                IconButton(onClick = { vm.keyword.value = "" }) {
                                    Icon(
                                        imageVector = Icons.Rounded.Clear,
                                        contentDescription = null
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp)
                            .navigationBarsPadding()
                            .focusRequester(focusRequester),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                keyboardController?.hide()
                                focusRequester.freeFocus()
                                Toast.makeText(mContext, vm.keyword.value, Toast.LENGTH_SHORT)
                                    .show()
                            }
                        )
                    )

                    LaunchedEffect(Unit) {
                        focusRequester.requestFocus()
                    }
                }
            },
            actions = {
                if (!vm.isSearching.value) {
                    IconButton(onClick = {
                        vm.isSearching.value = true
                    }) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = stringResource(id = android.R.string.search_go)
                        )
                    }
                }
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        contentDescription = stringResource(id = R.string.menu)
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
            )
        )
    }

    @Composable
    private fun BuildContent(paddingValues: PaddingValues) {
        Box(modifier = Modifier.padding(paddingValues)) {
            LazyColumn {
                vm.appListState.value.filter {
                    it.appName.contains(vm.keyword.value)
                            || it.pkgName.contains(vm.keyword.value)
                }.groupBy { it.headerChar }.forEach { (c, appItems) ->
                    stickyHeader {
                        BuildStickyHeader(headerStr = c.toString())
                    }
                    itemsIndexed(appItems) { index: Int, item: AppItem ->
                        BuildItem(appItem = item, index != appItems.size - 1)
                    }
                }
            }
        }
    }

    @Composable
    private fun BuildStickyHeader(headerStr: String) {
        Column(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.background,
                )
        ) {
            Text(
                text = headerStr,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .padding(horizontal = 8.dp),
            )
            Divider(
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }

    @Composable
    private fun BuildItem(appItem: AppItem, showDivider: Boolean = true) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
        ) {
            Row(
                modifier = Modifier.padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = appItem.iconUri,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(verticalArrangement = Arrangement.Center) {
                    Text(
                        text = buildItemText(text = appItem.appName),
                        style = MaterialTheme.typography.headlineSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = buildItemText(text = appItem.pkgName),
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (showDivider) {
                Divider()
            }
        }
    }

    @Composable
    private fun buildItemText(text: String): AnnotatedString =
        buildAnnotatedString {
            append(text)
            val keyword = vm.keyword.value
            if (keyword.isNotEmpty()) {
                text.indexOfAll(keyword, true).forEach {
                    addStyle(
                        style = SpanStyle(
                            color = Color.Cyan,
                            fontWeight = FontWeight.Bold,
                        ),
                        start = it,
                        end = it + keyword.length,
                    )
                }
            }
        }

    override fun onBackPressed() {
        if (vm.isSearching.value) {
            closeSearch()
            return
        }
        super.onBackPressed()
    }

    private fun closeSearch() {
        vm.keyword.value = ""
        vm.isSearching.value = false
    }
}