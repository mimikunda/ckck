package com.ckck.android

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.ckck.android.mainui.HomeScreen
import com.ckck.android.models.HomeTab
import com.ckck.android.navigation.Route
import com.ckck.android.screens.FavoritesScreen
import com.ckck.android.screens.SettingsScreen
import com.ckck.android.screens.StationDetailScreen
import com.ckck.android.screens.TripDetailScreen

@Composable
fun CkckApp() {
    val navController = rememberNavController()
    var currentTab by rememberSaveable { mutableStateOf(HomeTab.Home) }

    NavHost(
        navController = navController,
        startDestination = Route.Home
    ) {
        composable<Route.Home> {
            HomeScreen(
                currentTab = currentTab,
                onTabSelected = { currentTab = it },
                onStationClick = { id, name ->
                    navController.navigate(Route.StationDetail(id, name))
                },
                onNavigateClick = { from, to ->
                    navController.navigate(Route.TripDetail(from, to))
                },
                onSettingsClick = {
                    navController.navigate(Route.Settings)
                },
                onFavoritesClick = {
                    navController.navigate(Route.Favorites)
                }
            )
        }

        composable<Route.StationDetail> { backStackEntry ->
            val stationDetail: Route.StationDetail = backStackEntry.toRoute()
            StationDetailScreen(
                stationId = stationDetail.stationId,
                stationName = stationDetail.stationName,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<Route.TripDetail> { backStackEntry ->
            val tripDetail: Route.TripDetail = backStackEntry.toRoute()
            TripDetailScreen(
                from = tripDetail.from,
                to = tripDetail.to,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<Route.Settings> {
            SettingsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<Route.Favorites> {
            FavoritesScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
