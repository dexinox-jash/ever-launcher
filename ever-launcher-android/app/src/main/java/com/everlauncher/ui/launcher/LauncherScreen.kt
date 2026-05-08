package com.everlauncher.ui.launcher

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.everlauncher.R
import com.everlauncher.domain.model.AppItem
import com.everlauncher.domain.model.GateType
import com.everlauncher.ui.gates.BreathingGateScreen
import com.everlauncher.ui.gates.DelayGateScreen
import com.everlauncher.ui.gates.IntentionGateScreen

@Composable
fun LauncherScreen(
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LauncherViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    uiState.pendingGateApp?.let { gateApp ->
        if (gateApp.gateType == null) {
            LaunchedEffect(gateApp) {
                viewModel.dismissGateAndLaunch(gateApp, context)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount < -40f) viewModel.showSearch()
                }
            }
            .pointerInput("longPress") {
                detectTapGestures(onLongPress = { onOpenSettings() })
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp)
                .systemBarsPadding()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = viewModel::showSearch) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = stringResource(R.string.search)
                    )
                }
                IconButton(onClick = onOpenSettings) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(R.string.settings)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = uiState.currentTime,
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = uiState.currentDate,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            uiState.currentMode?.let { mode ->
                ModeIndicator(modeName = mode.name)
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (uiState.visibleApps.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_apps_in_mode),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(
                        items = uiState.visibleApps,
                        key = { it.id }
                    ) { app ->
                        AppListItem(
                            app = app,
                            showDivider = app.id != uiState.visibleApps.last().id,
                            onClick = { tappedApp ->
                                if (tappedApp.isGated) {
                                    viewModel.presentGate(tappedApp)
                                } else {
                                    viewModel.launchAndTrack(tappedApp, context)
                                }
                            }
                        )
                    }
                }
            }

            if (uiState.showHiddenCount && uiState.hiddenAppCount > 0 && uiState.currentMode != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.apps_hidden, uiState.hiddenAppCount, uiState.currentMode!!.name.lowercase()),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.05f),
                            shape = MaterialTheme.shapes.small
                        )
                        .padding(vertical = 10.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        AnimatedVisibility(
            visible = uiState.isSearchVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            SearchOverlay(
                query = uiState.searchQuery,
                results = uiState.searchResults,
                onQueryChange = viewModel::onSearchQueryChange,
                onAppClick = { app ->
                    if (app.isGated) {
                        viewModel.presentGate(app)
                    } else {
                        viewModel.launchAndTrack(app, context)
                    }
                    viewModel.hideSearch()
                },
                onDismiss = viewModel::hideSearch
            )
        }

        uiState.pendingGateApp?.let { gateApp ->
            when (gateApp.gateType) {
                GateType.BREATHING -> BreathingGateScreen(
                    appName = gateApp.displayName,
                    onGatePassed = { viewModel.onGatePassed(gateApp, context) }
                )
                GateType.INTENTION -> IntentionGateScreen(
                    appName = gateApp.displayName,
                    onGatePassed = { viewModel.onGatePassed(gateApp, context) },
                    onDismiss = viewModel::dismissGate
                )
                GateType.DELAY -> DelayGateScreen(
                    appName = gateApp.displayName,
                    onGatePassed = { viewModel.onGatePassed(gateApp, context) }
                )
                null -> { /* handled by LaunchedEffect above */ }
            }
        }
    }
}

@Composable
private fun SearchOverlay(
    query: String,
    results: List<AppItem>,
    onQueryChange: (String) -> Unit,
    onAppClick: (AppItem) -> Unit,
    onDismiss: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.5f),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(horizontal = 28.dp, vertical = 16.dp)) {
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                singleLine = true,
                decorationBox = { inner ->
                    Box {
                        if (query.isEmpty()) {
                            Text(
                                stringResource(R.string.search_apps),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                        inner()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
            )
            Spacer(Modifier.height(16.dp))
            LazyColumn {
                items(results, key = { it.id }) { app ->
                    AppListItem(
                        app = app,
                        showDivider = app.id != results.last().id,
                        onClick = onAppClick
                    )
                }
            }
        }
    }
}
