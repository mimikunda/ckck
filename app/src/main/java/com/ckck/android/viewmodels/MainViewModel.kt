package com.ckck.android.viewmodels

import android.app.Application
import android.content.Context
import android.location.Location
import android.location.LocationManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ckck.android.api.NominatimPlace
import com.ckck.android.api.NominatimService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

data class MainUiState(
    val fromQuery: String = "",
    val toQuery: String = "",
    val fromResults: List<NominatimPlace> = emptyList(),
    val toResults: List<NominatimPlace> = emptyList(),
    val fromLocation: NominatimPlace? = null,
    val toLocation: NominatimPlace? = null,
    val loadingCurrentLocation: Boolean = false,
    val canGetLocation: Boolean = true,
    val permissionErrorMessage: String? = null
)

@OptIn(FlowPreview::class)
@HiltViewModel
class MainViewModel @Inject constructor(
    application: Application,
    private val nominatimService: NominatimService
) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val fromSearchFlow = MutableStateFlow("")
    private val toSearchFlow = MutableStateFlow("")

    init {
        viewModelScope.launch {
            fromSearchFlow
                .debounce(500.milliseconds)
                .filter { it.length > 2 }
                .collectLatest { query ->
                    performSearch(query, isFrom = true)
                }
        }
        viewModelScope.launch {
            toSearchFlow
                .debounce(500.milliseconds)
                .filter { it.length > 2 }
                .collectLatest { query ->
                    performSearch(query, isFrom = false)
                }
        }
    }

    fun onFromQueryChanged(query: String) {
        _uiState.update { it.copy(fromQuery = query) }
        fromSearchFlow.value = query
        if (query.isEmpty()) {
            _uiState.update { it.copy(fromResults = emptyList()) }
        }
    }

    fun onToQueryChanged(query: String) {
        _uiState.update { it.copy(toQuery = query) }
        toSearchFlow.value = query
        if (query.isEmpty()) {
            _uiState.update { it.copy(toResults = emptyList()) }
        }
    }

    fun onFromPlaceSelected(place: NominatimPlace) {
        _uiState.update {
            it.copy(
                fromLocation = place,
                fromQuery = place.displayName,
                fromResults = emptyList()
            )
        }
    }

    fun onToPlaceSelected(place: NominatimPlace) {
        _uiState.update {
            it.copy(
                toLocation = place,
                toQuery = place.displayName,
                toResults = emptyList()
            )
        }
    }

    private suspend fun performSearch(query: String, isFrom: Boolean) {
        try {
            val results = nominatimService.search(query)
            if (isFrom) {
                _uiState.update { it.copy(fromResults = results) }
            } else {
                _uiState.update { it.copy(toResults = results) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getCurrentLocations() {
        val locationManager =
            getApplication<Application>().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        try {
            _uiState.update { it.copy(loadingCurrentLocation = true) }

            val location: Location? =
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

            if (location != null) {
                val currentPlace = NominatimPlace(
                    displayName = "<Current Location>",
                    lat = location.latitude.toString(),
                    lon = location.longitude.toString(),
                    name = "<Current Location>"
                )
                onFromPlaceSelected(currentPlace)
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        } finally {
            _uiState.update { it.copy(loadingCurrentLocation = false) }
        }
    }


    fun showMissingPermissionAlert(message: String) {
        _uiState.update { it.copy(permissionErrorMessage = message) }
    }

    fun clearPermissionAlert() {
        _uiState.update { it.copy(permissionErrorMessage = null) }
    }
}
