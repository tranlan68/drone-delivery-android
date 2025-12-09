package com.delivery.setting.ui.location

import androidx.lifecycle.viewModelScope
import com.delivery.core.base.BaseViewModel
import com.delivery.setting.model.SearchResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocationSearchViewModel @Inject constructor(
    private val locationRepository: LocationRepository
) : BaseViewModel() {

    private val _searchResults = MutableStateFlow<List<SearchResult>>(emptyList())
    val searchResults: StateFlow<List<SearchResult>> = _searchResults

    private val _selectedLocation = MutableStateFlow<SearchResult?>(null)
    val selectedLocation: StateFlow<SearchResult?> = _selectedLocation

    fun searchLocations(query: String) {
        viewModelScope.launch {
            try {
                isLoading.value = true
                val results = locationRepository.searchPlaces(query)
                _searchResults.value = results
            } catch (e: Exception) {
                messageError.value = e.message
            } finally {
                isLoading.value = false
            }
        }
    }

    fun getCurrentLocation() {
        viewModelScope.launch {
            try {
                isLoading.value = true
                val currentLocation = locationRepository.getCurrentLocation()
                currentLocation?.let { location ->
                    val searchResult = SearchResult(
                        placeId = "current_location",
                        name = "Vị trí hiện tại",
                        address = location.address,
                        latitude = location.latitude,
                        longitude = location.longitude
                    )
                    selectLocation(searchResult)
                }
            } catch (e: Exception) {
                messageError.value = e.message
            } finally {
                isLoading.value = false
            }
        }
    }

    fun selectLocation(searchResult: SearchResult) {
        _selectedLocation.value = searchResult
    }
}
