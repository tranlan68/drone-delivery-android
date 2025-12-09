package com.delivery.uservht.ui.splash

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.delivery.core.base.BaseViewModel
import com.delivery.core.pref.RxPreferences
import com.delivery.core.utils.SingleLiveEvent
import com.delivery.vht.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel
    @Inject
    constructor(
        private val rxPreferences: RxPreferences,
    ) : BaseViewModel() {
        val actionSPlash = SingleLiveEvent<SplashActionState>()

        val splashTitle = MutableLiveData(R.string.splash)

        init {
            viewModelScope.launch(Dispatchers.IO) {
                delay(3000)
                if (rxPreferences.getToken().toString().isEmpty()) {
                    actionSPlash.postValue(SplashActionState.LoginAccount)
                } else {
                    actionSPlash.postValue(SplashActionState.Finish)
                }
            }
        }

        sealed class SplashActionState {
            data object Finish : SplashActionState()

            data object LoginAccount : SplashActionState()
        }
    }
