@file:OptIn(
    ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalMaterial3Api::class,
)

package com.ckck.android.mainui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ckck.android.BuildConfig
import com.ckck.android.R
import com.ckck.android.api.NominatimPlace
import com.ckck.android.viewmodels.LocationError
import com.ckck.android.viewmodels.MainTab
import com.ckck.android.viewmodels.MainUiState
import com.ckck.android.viewmodels.MainViewModel
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.map.MapOptions
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.OrnamentOptions
import org.maplibre.compose.style.BaseStyle
import org.maplibre.compose.style.rememberStyleState
import org.maplibre.spatialk.geojson.Position

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel = hiltViewModel(),
    onStationClick: (String, String) -> Unit = { _, _ -> },
    onNavigateClick: (String, String) -> Unit = { _, _ -> },
    onSettingsClick: () -> Unit = {},
    onFavoritesClick: () -> Unit = {},
) {
    val uiState by mainViewModel.uiState.collectAsState()
    val permissionHandler = rememberPermissionHandler(mainViewModel)

    uiState.locationError?.let { error ->
        data class DialogConfig(
            val title: String,
            val text: String,
            val confirmText: String,
            val onConfirm: () -> Unit
        )

        val config = when (error) {
            LocationError.MissingPermission -> DialogConfig(
                stringResource(R.string.permission_required_title),
                stringResource(R.string.permission_required_desc),
                stringResource(R.string.action_enable),
                { permissionHandler.requestPermission() }
            )

            LocationError.PermanentlyDenied -> DialogConfig(
                stringResource(R.string.permission_denied_title),
                stringResource(R.string.permission_denied_desc),
                stringResource(R.string.action_settings),
                { permissionHandler.openAppSettings() }
            )

            LocationError.LocationDisabled -> DialogConfig(
                stringResource(R.string.location_disabled_title),
                stringResource(R.string.location_disabled_desc),
                stringResource(R.string.action_enable),
                { permissionHandler.openLocationSettings() }
            )
        }

        CommonDialog(
            onDismissRequest = { mainViewModel.clearPermissionAlert() },
            title = config.title,
            text = config.text,
            confirmButtonText = config.confirmText,
            onConfirm = config.onConfirm,
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Fullscreen Map Background
        Map(modifier = Modifier.fillMaxSize())

        // Top Actions (Settings)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WindowInsets.statusBars.asPaddingValues())
                .padding(16.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                tonalElevation = 4.dp
            ) {
                IconButton(onClick = onSettingsClick) {
                    Icon(
                        Icons.Filled.Settings,
                        contentDescription = stringResource(R.string.desc_settings)
                    )
                }
            }
        }

        // Overlay Content based on Tab
        AnimatedContent(
            targetState = uiState.currentTab,
            transitionSpec = {
                (fadeIn(tween(200)) + slideInVertically(tween(200)) { it / 2 }) togetherWith
                        (fadeOut(tween(200)) + slideOutVertically(tween(200)) { it / 2 })
            },
            label = "TabContentTransition",
            modifier = Modifier.fillMaxSize()
        ) { tab ->
            when (tab) {
                MainTab.Map -> {
                    // Just the map (nothing to overlay here, or maybe a search bar)
                    Box(modifier = Modifier.fillMaxSize())
                }

                MainTab.Stations -> {
                    OverlayCard {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(bottom = 100.dp) // Space for navbar
                        ) {
                            item {
                                PinnedStations(
                                    stations = uiState.pinnedStations,
                                    isLoading = uiState.isLoadingStations,
                                    onStationClick = onStationClick
                                )
                            }
                            item {
                                FavoriteLocations(onFavoriteClick = onFavoritesClick)
                            }
                        }
                    }
                }

                MainTab.Navigate -> {
                    OverlayCard {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 100.dp) // Space for navbar
                        ) {
                            NavigationManager(
                                viewModel = mainViewModel,
                                permissionHandler = permissionHandler,
                                onNavigateClick = onNavigateClick
                            )
                        }
                    }
                }
            }
        }

        // Floating Navbar
        FloatingPillNavbar(
            selectedTab = uiState.currentTab,
            onTabSelected = mainViewModel::onTabSelected,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(
                    bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                )
        )
    }
}

@Composable
fun OverlayCard(
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 100.dp), // Space for top actions/status bar
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            tonalElevation = 8.dp
        ) {
            content()
        }
    }
}

@Composable
fun PinnedStations(
    stations: List<StationData>,
    isLoading: Boolean,
    onStationClick: (String, String) -> Unit = { _, _ -> }
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = stringResource(R.string.label_pinned_stations))
            if (isLoading) {
                LoadingIndicator(modifier = Modifier.height(16.dp))
            }
        }
        stations.forEach { stationData ->
            Card(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)

                ) {
                    Text(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        text = stationData.name
                    )
                    if (stationData.departures.isEmpty() && !isLoading) {
                        Text(
                            text = stringResource(R.string.msg_no_departures),
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                    }
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        items(stationData.departures) { departure ->
                            SuggestionChip(
                                onClick = { onStationClick(stationData.name, stationData.name) },
                                label = {
                                    Text(
                                        text = buildAnnotatedString {
                                            append(departure.time)
                                            if (departure.delay != null) {
                                                withStyle(
                                                    style = SpanStyle(
                                                        baselineShift = BaselineShift.Superscript,
                                                        color = departure.statusColor
                                                    )
                                                ) {
                                                    append(departure.delay)
                                                }
                                            }
                                        }
                                    )
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

data class StationData(
    val name: String,
    val departures: List<StationDemo>
)

data class StationDemo(
    val time: String,
    val delay: String?,
    val statusColor: Color
)

@Composable
fun NavigationManager(
    viewModel: MainViewModel = hiltViewModel(),
    permissionHandler: PermissionHandler,
    onNavigateClick: (String, String) -> Unit = { _, _ -> }
) {
    val uiState by viewModel.uiState.collectAsState()

    NavigationManagerContent(
        uiState = uiState,
        onFromQueryChanged = viewModel::onFromQueryChanged,
        onToQueryChanged = viewModel::onToQueryChanged,
        onFromPlaceSelected = viewModel::onFromPlaceSelected,
        onToPlaceSelected = viewModel::onToPlaceSelected,
        onGetCurrentLocationClick = { permissionHandler.requestPermission() },
        onNavigateClick = onNavigateClick
    )
}

@Composable
fun NavigationManagerContent(
    //TODO: Allow inputting long/lat manually
    uiState: MainUiState,
    onFromQueryChanged: (String) -> Unit,
    onToQueryChanged: (String) -> Unit,
    onFromPlaceSelected: (NominatimPlace) -> Unit,
    onToPlaceSelected: (NominatimPlace) -> Unit,
    onGetCurrentLocationClick: () -> Unit,
    onNavigateClick: (String, String) -> Unit = { _, _ -> }
) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(stringResource(R.string.label_navigate))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LocationSearchField(
                label = stringResource(R.string.label_from),
                query = uiState.fromQuery,
                results = uiState.fromResults,
                onQueryChange = onFromQueryChanged,
                onPlaceSelected = onFromPlaceSelected,
                onDismissRequest = { },
                modifier = Modifier.weight(1f)
            )
            IconToggleButton(
                checked = uiState.loadingCurrentLocation,
                onCheckedChange = { onGetCurrentLocationClick() },
                shapes = IconButtonDefaults.toggleableShapes(),
                enabled = !uiState.loadingCurrentLocation && uiState.canGetLocation
            ) {
                if (uiState.loadingCurrentLocation) {
                    LoadingIndicator(modifier = Modifier.fillMaxSize())
                } else {
                    Icon(
                        imageVector = Icons.Filled.MyLocation,
                        contentDescription = stringResource(R.string.desc_current_location)
                    )
                }
            }
        }

        LocationSearchField(
            label = stringResource(R.string.label_to),
            query = uiState.toQuery,
            results = uiState.toResults,
            onQueryChange = onToQueryChanged,
            onPlaceSelected = onToPlaceSelected,
            onDismissRequest = { }
        )

        Button(
            onClick = {
                onNavigateClick(uiState.fromQuery, uiState.toQuery)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState.fromLocation != null && uiState.toLocation != null,
            content = { Text(stringResource(R.string.action_go)) }
        )
    }
}

@Composable
fun LocationSearchField( //TODO: allow using https://api.transitous.org/api/v1/geocode as lookup
    label: String,
    query: String,
    results: List<NominatimPlace>,
    onQueryChange: (String) -> Unit,
    onPlaceSelected: (NominatimPlace) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    ExposedDropdownMenuBox(
        expanded = results.isNotEmpty(),
        onExpandedChange = { },
        modifier = modifier
    ) {
        TextField(
            value = query,
            onValueChange = {
                onQueryChange(it)
            },
            label = { Text(label) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryEditable),
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
        )

        if (results.isNotEmpty()) {
            ExposedDropdownMenu(
                expanded = true,
                onDismissRequest = onDismissRequest
            ) {
                results.forEach { place ->
                    DropdownMenuItem(
                        text = { Text(place.displayName) },
                        onClick = {
                            onPlaceSelected(place)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FavoriteLocations(
    onFavoriteClick: () -> Unit = {}
) {
    Column {
        Text(stringResource(R.string.label_favorite_locations))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val stations = listOf("Location1", "Location2", "Location3", "Location4", "Location5")
            stations.forEach { station ->
                item {
                    Card(
                        onClick = onFavoriteClick
                    ) {
                        Box(modifier = Modifier.padding(16.dp)) {
                            Text(station)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Map(modifier: Modifier = Modifier) {
    val protomapsApiKey = BuildConfig.PROTOMAPS_API_KEY

    val cameraState = rememberCameraState(
        firstPosition = CameraPosition(
            target = Position(longitude = 14.505999, latitude = 46.051366),
            zoom = 10.0
        )
    )
    val styleState = rememberStyleState()
    val variant = if (isSystemInDarkTheme()) "dark" else "light"
    MaplibreMap(
        modifier = modifier,
        baseStyle = BaseStyle.Uri("https://api.protomaps.com/styles/v4/$variant/en.json?key=$protomapsApiKey"),
        cameraState = cameraState,
        styleState = styleState,
        options = MapOptions(ornamentOptions = OrnamentOptions.OnlyLogo),
    )
}