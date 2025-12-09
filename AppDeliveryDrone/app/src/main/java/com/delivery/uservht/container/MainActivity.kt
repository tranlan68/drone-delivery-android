package com.delivery.uservht.container

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import com.delivery.core.base.activity.BaseActivityNotRequireViewModel
import com.delivery.core.base.dialog.ConfirmDialogListener
import com.delivery.core.network.NetworkEvent
import com.delivery.core.network.NetworkState
import com.delivery.core.network.connectivity.NetworkConnectionManager
import com.delivery.core.pref.RxPreferences
import com.delivery.core.utils.setLanguage
import com.delivery.core.utils.toast
import com.delivery.uservht.navigation.AppNavigation
import com.delivery.vht.BuildConfig
import com.delivery.vht.R
import com.delivery.vht.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.maplibre.android.MapLibre
import org.maplibre.android.WellKnownTileServer
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : BaseActivityNotRequireViewModel<ActivityMainBinding>(), ConfirmDialogListener {
    @Inject
    lateinit var appNavigation: AppNavigation

    @Inject
    lateinit var networkConnectionManager: NetworkConnectionManager

    @Inject
    lateinit var rxPreferences: RxPreferences

    @Inject
    lateinit var networkEvent: NetworkEvent

    override val layoutId = R.layout.activity_main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val navHostFragment =
            supportFragmentManager
                .findFragmentById(R.id.nav_host) as NavHostFragment
        appNavigation.bind(navHostFragment.navController)

        lifecycleScope.launch {
            val language = rxPreferences.getLanguage().first()
            language?.let { setLanguage(it) }
        }
        MapLibre.getInstance(
            applicationContext,
            BuildConfig.MAPTILER_API_KEY,
            WellKnownTileServer.MapTiler,
        )
        networkConnectionManager.isNetworkConnectedFlow
            .onEach {
                if (it) {
                    Timber.tag("TrongVQ").d("onCreate: Network connected")
                } else {
                    Timber.tag("TrongVQ").d("onCreate: Network disconnected")
                }
            }
            .launchIn(lifecycleScope)
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            networkEvent.observableNetworkState.collect { status ->
                when (status) {
                    is NetworkState.UNAUTHORIZED -> {
                        networkEvent.publish(NetworkState.INITIALIZE)
                    }

                    is NetworkState.NO_INTERNET -> {
                        networkEvent.publish(NetworkState.INITIALIZE)
                    }

                    is NetworkState.CONNECTED_INTERNET -> {
                    }

                    is NetworkState.NO_CONNECT_INTERNET -> {
                        networkEvent.publish(NetworkState.INITIALIZE)
                    }

                    is NetworkState.GENERIC -> {
                        networkEvent.publish(NetworkState.INITIALIZE)
                    }

                    else -> {}
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        networkConnectionManager.startListenNetworkState()
    }

    override fun onStop() {
        Timber.tag("ahihi").d("onStop")
        super.onStop()
        networkConnectionManager.stopListenNetworkState()
    }

    override fun onClickOk(type: Int?) {
        "Ok Activity".toast(this)
    }

    override fun onClickCancel(type: Int?) {
        "Cancel Activity".toast(this)
    }

    override fun onDestroy() {
        Timber.tag("ahihi").d("onDestroy")
        super.onDestroy()
    }
}
