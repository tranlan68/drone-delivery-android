package com.delivery.core.network

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

sealed class NetworkState {
    object NO_INTERNET : NetworkState()

    object UNAUTHORIZED : NetworkState()

    object INITIALIZE : NetworkState()

    object ERROR : NetworkState()

    object NOT_FOUND : NetworkState()

    object BAD_REQUEST : NetworkState()

    object CONNECTION_LOST : NetworkState()

    object FORBIDDEN : NetworkState()

    object SERVER_NOT_AVAILABLE : NetworkState()

    object DATA_ERROR : NetworkState()

    object ACCESS_DENY : NetworkState()

    object NO_PERMISSION : NetworkState()

    object NO_CONNECT_INTERNET : NetworkState()

    object CONNECTED_INTERNET : NetworkState()

    data class GENERIC(val exception: ApiException) : NetworkState()
}

class NetworkEvent
    @Inject
    constructor() {
        private val _events = MutableStateFlow<NetworkState>(NetworkState.INITIALIZE)

        val observableNetworkState: Flow<NetworkState> = _events.asStateFlow()

        fun publish(networkState: NetworkState) {
            _events.value = networkState
        }
    }
