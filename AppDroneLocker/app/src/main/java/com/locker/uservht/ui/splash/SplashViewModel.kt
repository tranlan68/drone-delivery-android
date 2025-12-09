package com.locker.uservht.ui.splash

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.locker.uservht.R
import com.delivery.core.base.BaseViewModel
import com.delivery.core.pref.RxPreferences
import com.delivery.core.utils.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val  rxPreferences: RxPreferences
) : BaseViewModel() {

    val actionSPlash = SingleLiveEvent<SplashActionState>()

    val splashTitle = MutableLiveData(R.string.splash)

    init {
        viewModelScope.launch {
            delay(1000)
            val token = rxPreferences.getSelectedLockerId().first()
            if(token.isNullOrEmpty()){
                actionSPlash.value = SplashActionState.OpenLogin
            } else {
                actionSPlash.value = SplashActionState.OpenHome
            }
        }
    }

    sealed class SplashActionState {
        data object OpenLogin : SplashActionState()
        data object OpenHome : SplashActionState()
    }
}
