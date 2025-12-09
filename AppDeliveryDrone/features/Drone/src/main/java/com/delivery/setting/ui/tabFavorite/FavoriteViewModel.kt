package com.delivery.setting.ui.tabFavorite

import androidx.lifecycle.viewModelScope
import com.delivery.core.base.BaseViewModel
import com.delivery.core.pref.RxPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoriteViewModel
    @Inject
    constructor(
        private val rxPreferences: RxPreferences,
    ) : BaseViewModel() {
        fun setLanguage(language: String) =
            viewModelScope.launch(Dispatchers.IO) {
                rxPreferences.setLanguage(language)
            }
    }
