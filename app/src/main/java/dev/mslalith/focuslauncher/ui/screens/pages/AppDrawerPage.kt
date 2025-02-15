package dev.mslalith.focuslauncher.ui.screens.pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.google.accompanist.insets.LocalWindowInsets
import dev.mslalith.focuslauncher.data.models.AppDrawerViewType
import dev.mslalith.focuslauncher.data.models.AppWithIcon
import dev.mslalith.focuslauncher.data.models.BottomSheetContentType
import dev.mslalith.focuslauncher.data.models.MoreAppOptionsProperties
import dev.mslalith.focuslauncher.data.providers.LocalLauncherViewManager
import dev.mslalith.focuslauncher.extensions.isAlphabet
import dev.mslalith.focuslauncher.extensions.launchApp
import dev.mslalith.focuslauncher.extensions.toAppWithIconList
import dev.mslalith.focuslauncher.extensions.verticalSpacer
import dev.mslalith.focuslauncher.ui.viewmodels.AppsViewModel
import dev.mslalith.focuslauncher.ui.viewmodels.SettingsViewModel

private val ITEM_START_PADDING = 24.dp
private val ITEM_END_PADDING = 8.dp
private val ICON_SIZE = 28.dp
private val ICON_INNER_HORIZONTAL_PADDING = 4.dp

private enum class Position {
    TOP,
    BOTTOM
}

@Composable
fun AppDrawerPage(
    appsViewModel: AppsViewModel,
    settingsViewModel: SettingsViewModel,
) {
    val viewManager = LocalLauncherViewManager.current

    val appDrawerViewType by settingsViewModel.appDrawerViewTypeStateFlow.collectAsState()

    fun showMoreOptions(app: AppWithIcon) {
        viewManager.showBottomSheet(
            sheetType = BottomSheetContentType.MoreAppOptions(
                properties = MoreAppOptionsProperties(
                    appsViewModel = appsViewModel,
                    settingsViewModel = settingsViewModel,
                    app = app,
                    onClose = { viewManager.hideBottomSheet() },
                )
            ),
        )
    }


    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        Box(
            modifier = Modifier.weight(weight = 1f),
        ) {
            when (appDrawerViewType) {
                AppDrawerViewType.LIST -> AppsList(
                    appsViewModel = appsViewModel,
                    settingsViewModel = settingsViewModel,
                    onAppLongClick = ::showMoreOptions,
                )
                AppDrawerViewType.GRID -> AppsGrid(
                    appsViewModel = appsViewModel,
                    onAppLongClick = ::showMoreOptions,
                )
            }

            ListFadeOutEdgeGradient(position = Position.TOP)
            ListFadeOutEdgeGradient(position = Position.BOTTOM)
        }

        val insets = LocalWindowInsets.current
        val imeHeight = LocalDensity.current.run { (insets.ime.bottom - insets.navigationBars.bottom).toDp() }
        val imeOffset by animateDpAsState(targetValue = imeHeight)

        SearchAppField(
            appsViewModel = appsViewModel,
            settingsViewModel = settingsViewModel,
        )
        imeOffset.verticalSpacer()
    }
}

@Composable
private fun ListFadeOutEdgeGradient(
    position: Position,
    height: Dp = 14.dp,
) {
    val colors = listOf(MaterialTheme.colors.background, Color.Transparent).let {
        when (position) {
            Position.TOP -> it
            Position.BOTTOM -> it.reversed()
        }
    }
    val alignment = when (position) {
        Position.TOP -> Alignment.TopCenter
        Position.BOTTOM -> Alignment.BottomCenter
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Spacer(
            modifier = Modifier
                .align(alignment)
                .fillMaxWidth()
                .height(height)
                .background(brush = Brush.verticalGradient(colors))
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun AppsGrid(
    appsViewModel: AppsViewModel,
    onAppLongClick: (AppWithIcon) -> Unit,
) {
    val columnCount = 4
    val context = LocalContext.current
    val softwareKeyboardController = LocalSoftwareKeyboardController.current

    val configuration = LocalConfiguration.current
    val topSpacing = configuration.screenHeightDp.dp * 0.2f
    val bottomSpacing = configuration.screenHeightDp.dp * 0.05f

    val appsNoIconList by appsViewModel.appDrawerAppsStateFlow.collectAsState()
    val appsList = appsNoIconList.toAppWithIconList(context)

    LazyVerticalGrid(
        columns = GridCells.Fixed(count = columnCount),
        modifier = Modifier.padding(horizontal = 24.dp),
    ) {
        repeat(columnCount) {
            item { topSpacing.verticalSpacer() }
        }

        items(items = appsList) { app ->
            AppDrawerGridItem(
                app = app,
                onClick = {
                    softwareKeyboardController?.hide()
                    context.launchApp(it.toApp())
                },
                onLongClick = {
                    softwareKeyboardController?.hide()
                    onAppLongClick(app)
                },
            )
        }

        repeat(columnCount) {
            item { bottomSpacing.verticalSpacer() }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun AppsList(
    appsViewModel: AppsViewModel,
    settingsViewModel: SettingsViewModel,
    onAppLongClick: (AppWithIcon) -> Unit,
) {
    val context = LocalContext.current
    val softwareKeyboardController = LocalSoftwareKeyboardController.current

    val configuration = LocalConfiguration.current
    val topSpacing = configuration.screenHeightDp.dp * 0.2f
    val bottomSpacing = configuration.screenHeightDp.dp * 0.05f

    val appsNoIconList by appsViewModel.appDrawerAppsStateFlow.collectAsState()
    val appsList = appsNoIconList.toAppWithIconList(context)

    val groupedApps by remember(appsList) {
        derivedStateOf {
            appsList.groupBy { appModel ->
                appModel.name.first().let { if (it.isAlphabet()) it.uppercaseChar() else '#' }
            }
        }
    }

    val showAppGroupHeader by settingsViewModel.appGroupHeaderVisibilityStateFlow.collectAsState()
    val isSearchQueryEmpty by appsViewModel.isSearchQueryEmpty.collectAsState(initial = true)
    val spacing = when (isSearchQueryEmpty) {
        true -> topSpacing to bottomSpacing
        false -> 0.dp to 0.dp
    }

    LazyColumn(
        verticalArrangement = Arrangement.Bottom,
        modifier = Modifier.fillMaxSize().height(150.dp),
    ) {
        item { spacing.first.verticalSpacer() }

        for ((character, apps) in groupedApps) {
            item(key = character) {
                GroupedAppsList(
                    settingsViewModel = settingsViewModel,
                    apps = apps,
                    character = character,
                    showAppGroupHeader = showAppGroupHeader && groupedApps.size != 1,
                    onAppClick = {
                        softwareKeyboardController?.hide()
                        context.launchApp(it.toApp())
                    },
                    onAppLongClick = {
                        softwareKeyboardController?.hide()
                        onAppLongClick(it)
                    },
                )
            }
        }
        item { spacing.second.verticalSpacer() }
    }
}

@Composable
private fun GroupedAppsList(
    settingsViewModel: SettingsViewModel,
    apps: List<AppWithIcon>,
    character: Char,
    showAppGroupHeader: Boolean,
    onAppClick: (AppWithIcon) -> Unit,
    onAppLongClick: (AppWithIcon) -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(top = 20.dp)
    ) {
        if (showAppGroupHeader) {
            CharacterHeader(character = character)
        }
        apps.forEach { app ->
            AppDrawerListItem(
                settingsViewModel = settingsViewModel,
                app = app,
                onClick = { onAppClick(it) },
                onLongClick = { onAppLongClick(it) }
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun CharacterHeader(character: Char) {
    ListItem(
        icon = {
            Box(
                modifier = Modifier
                    .padding(start = ITEM_START_PADDING - ICON_INNER_HORIZONTAL_PADDING)
                    .size(size = ICON_SIZE + (ICON_INNER_HORIZONTAL_PADDING * 2))
                    .clip(shape = MaterialTheme.shapes.small)
                    .background(color = MaterialTheme.colors.primaryVariant)
                    .border(
                        width = 1.5f.dp,
                        color = MaterialTheme.colors.onBackground.copy(alpha = 0.5f),
                        shape = MaterialTheme.shapes.small
                    )
            ) {
                Text(
                    text = "$character",
                    style = TextStyle(
                        color = MaterialTheme.colors.onBackground,
                        fontSize = 18.sp
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        },
        text = { Spacer(Modifier) }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppDrawerGridItem(
    app: AppWithIcon,
    onClick: (AppWithIcon) -> Unit,
    onLongClick: (AppWithIcon) -> Unit,
) {
    val iconBitmap = remember(key1 = app.packageName) {
        app.icon.toBitmap().asImageBitmap()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 0.dp, vertical = 4.dp)
            .clip(shape = MaterialTheme.shapes.small)
            .combinedClickable(
                onClick = { onClick(app) },
                onLongClick = { onLongClick(app) }
            )
            .padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.size(size = ICON_SIZE * 1.5f)
        ) {
            Image(
                bitmap = iconBitmap,
                contentDescription = app.name,
                modifier = Modifier
                    .fillMaxSize()
            )
        }
        8.dp.verticalSpacer()
        Text(
            text = app.name,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            style = TextStyle(
                color = MaterialTheme.colors.onBackground,
                fontSize = 16.sp,
            ),
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun AppDrawerListItem(
    settingsViewModel: SettingsViewModel,
    app: AppWithIcon,
    onClick: (AppWithIcon) -> Unit,
    onLongClick: (AppWithIcon) -> Unit,
) {
    val showAppIcons by settingsViewModel.appIconsVisibilityStateFlow.collectAsState()

    val image: @Composable () -> Unit = {
        Image(
            bitmap = app.icon.toBitmap().asImageBitmap(),
            contentDescription = app.name,
            modifier = Modifier
                .padding(start = ITEM_START_PADDING)
                .size(ICON_SIZE)
        )
    }

    val textStartPadding = when (showAppIcons) {
        true -> ITEM_END_PADDING
        false -> ITEM_START_PADDING + ((ICON_SIZE + ICON_INNER_HORIZONTAL_PADDING) / 4)
    }

    ListItem(
        modifier = Modifier
            .pointerInput(app.packageName) {
                detectTapGestures(
                    onTap = { onClick(app) },
                    onLongPress = { onLongClick(app) }
                )
            },
        icon = if (showAppIcons) image else null,
        text = {
            Text(
                text = app.name,
                style = TextStyle(
                    color = MaterialTheme.colors.onBackground,
                    fontSize = 18.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = textStartPadding)
            )
        }
    )
}

@Composable
private fun SearchAppField(
    modifier: Modifier = Modifier,
    appsViewModel: AppsViewModel,
    settingsViewModel: SettingsViewModel,
) {
    val showSearchBar by settingsViewModel.searchBarVisibilityStateFlow.collectAsState()
    var query by remember { mutableStateOf("") }
    val colors = MaterialTheme.colors

    if (showSearchBar) {
        TextField(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            value = query,
            textStyle = TextStyle(color = colors.onBackground),
            onValueChange = {
                query = it
                appsViewModel.searchAppQuery(query)
            },
            placeholder = {
                Text(
                    text = "Search app...",
                    style = TextStyle(color = colors.onBackground)
                )
            },
            shape = MaterialTheme.shapes.small,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                autoCorrect = false,
                imeAction = ImeAction.Search
            ),
            colors = TextFieldDefaults.textFieldColors(
                cursorColor = colors.onBackground,
                backgroundColor = colors.primaryVariant,
                focusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            leadingIcon = {
                Icon(Icons.Rounded.Search, contentDescription = "Search")
            },
            trailingIcon = {
                AnimatedVisibility(visible = query.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable {
                                query = ""
                                appsViewModel.searchAppQuery(query)
                            }
                            .padding(4.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Clear,
                            contentDescription = "Clear",
                        )
                    }
                }
            }
        )
    }
}
