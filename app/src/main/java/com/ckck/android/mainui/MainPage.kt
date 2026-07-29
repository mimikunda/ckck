@file:OptIn(
    ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalMaterial3Api::class,
)

package com.ckck.android.mainui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ckck.android.BuildConfig
import com.ckck.android.R
import com.ckck.android.api.NominatimPlace
import com.ckck.android.api.StopTime
import com.ckck.android.models.HomeTab
import com.ckck.android.utils.TimeUtils
import com.ckck.android.viewmodels.HomeUiState
import com.ckck.android.viewmodels.HomeViewModel
import com.ckck.android.viewmodels.LocationError
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.map.MapOptions
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.OrnamentOptions
import org.maplibre.compose.style.BaseStyle
import org.maplibre.compose.style.rememberStyleState
import org.maplibre.spatialk.geojson.Position

@Composable
fun HomeScreen(
    currentTab: HomeTab,
    onTabSelected: (HomeTab) -> Unit,
    onStationClick: (String, String) -> Unit,
    onNavigateClick: (String, String) -> Unit,
    onFavoritesClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = currentTab,
            transitionSpec = {
                fadeIn(tween(300)) togetherWith fadeOut(tween(300))
            },
            label = "TabTransition"
        ) { tab ->
            when (tab) {
                HomeTab.Home -> {
                    HomePage(
                        onStationClick = onStationClick,
                        onNavigateClick = onNavigateClick,
                        onFavoritesClick = onFavoritesClick,
                        onSettingsClick = onSettingsClick
                    )
                }

                HomeTab.Map -> {
                    MapPage()
                }
            }
        }

        FloatingPillNavbar(
            selectedTab = currentTab,
            onTabSelected = onTabSelected,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(
                    bottom = WindowInsets.navigationBars
                        .asPaddingValues()
                        .calculateBottomPadding()
                )
        )
    }
}

@Composable
fun HomePage(
    homeViewModel: HomeViewModel = hiltViewModel(),
    onStationClick: (String, String) -> Unit,
    onNavigateClick: (String, String) -> Unit,
    onFavoritesClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    val uiState by homeViewModel.uiState.collectAsState()
    val permissionHandler = rememberPermissionHandler(homeViewModel)

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
                onConfirm = { permissionHandler.requestPermission() }
            )

            LocationError.PermanentlyDenied -> DialogConfig(
                stringResource(R.string.permission_denied_title),
                stringResource(R.string.permission_denied_desc),
                stringResource(R.string.action_settings),
                onConfirm = { permissionHandler.openAppSettings() }
            )

            LocationError.LocationDisabled -> DialogConfig(
                stringResource(R.string.location_disabled_title),
                stringResource(R.string.location_disabled_desc),
                stringResource(R.string.action_enable),
                onConfirm = { permissionHandler.openLocationSettings() }
            )
        }

        CommonDialog(
            onDismissRequest = { homeViewModel.clearPermissionAlert() },
            title = config.title,
            text = config.text,
            confirmButtonText = config.confirmText,
            onConfirm = config.onConfirm,
        )
    }

    HomePageContent(
        uiState = uiState,
        onStationClick = onStationClick,
        onNavigateClick = onNavigateClick,
        onFavoritesClick = onFavoritesClick,
        onFromQueryChanged = homeViewModel::onFromQueryChanged,
        onToQueryChanged = homeViewModel::onToQueryChanged,
        onFromPlaceSelected = homeViewModel::onFromPlaceSelected,
        onToPlaceSelected = homeViewModel::onToPlaceSelected,
        onGetCurrentLocationClick = { permissionHandler.requestPermission() }
    )
}

@Composable
fun HomePageContent(
    uiState: HomeUiState,
    onStationClick: (String, String) -> Unit,
    onNavigateClick: (String, String) -> Unit,
    onFavoritesClick: () -> Unit,
    onFromQueryChanged: (String) -> Unit,
    onToQueryChanged: (String) -> Unit,
    onFromPlaceSelected: (NominatimPlace) -> Unit,
    onToPlaceSelected: (NominatimPlace) -> Unit,
    onGetCurrentLocationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
    ) { innerPadding ->
        Box(
            modifier = Modifier.padding(
                top = innerPadding.calculateTopPadding(),
                start = innerPadding.calculateStartPadding(LocalLayoutDirection.current),
                end = innerPadding.calculateEndPadding(LocalLayoutDirection.current)
            )
        ) {
            LazyColumn {
                item {
                    PinnedStations(
                        stations = uiState.pinnedStations,
                        isLoading = uiState.isLoadingStations,
                        onStationClick = onStationClick
                    )
                }
                item {
                    NavigationManagerContent(
                        uiState = uiState,
                        onFromQueryChanged = onFromQueryChanged,
                        onToQueryChanged = onToQueryChanged,
                        onFromPlaceSelected = onFromPlaceSelected,
                        onToPlaceSelected = onToPlaceSelected,
                        onGetCurrentLocationClick = onGetCurrentLocationClick,
                        onNavigateClick = onNavigateClick
                    )
                }
                item {
                    FavoriteLocations(onFavoriteClick = onFavoritesClick)
                }
                item {
                    Spacer(Modifier.height(100.dp)) // Space for navbar
                }
            }
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
                onClick = {
                    onStationClick(
                        stationData.name,
                        stationData.name
                    )
                }, // Fixed onClick for Card
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
                                onClick = { },
                                icon = {
                                    val backgroundColor = departure.color?.let {
                                        runCatching { Color("#$it".toColorInt()) }.getOrNull()
                                    } ?: Color.Transparent

                                    val textColor = departure.textColor?.let {
                                        runCatching { Color("#$it".toColorInt()) }.getOrNull()
                                    } ?: Color.Black

                                    Box(
                                        modifier = Modifier
                                            .background(
                                                backgroundColor,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .padding(horizontal = 4.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            color = textColor,
                                            text = departure.displayName
                                        )
                                    }
                                },
                                label = {
                                    Text(
                                        text = TimeUtils.formatDepartureTime(
                                            departure.place.departure,
                                            departure.place.scheduledDeparture,
                                            departure.place.tz
                                        )
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
    val departures: List<StopTime>
)

@Composable
fun NavigationManagerContent(
    uiState: HomeUiState,
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
fun LocationSearchField(
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
    Column(modifier = Modifier.padding(16.dp)) {
        Text(stringResource(R.string.label_favorite_locations))
        Spacer(Modifier.height(8.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
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
