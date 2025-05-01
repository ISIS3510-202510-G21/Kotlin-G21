package com.isis3510.growhub.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.*
import android.net.Network
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*

/**
 * Created by: Juan Manuel Jáuregui
 */

enum class ConnectionStatus {
    Available, Unavailable, Losing, Lost
}

class ConnectivityObserver(context: Context) {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val connectionStatus: Flow<ConnectionStatus> = callbackFlow {
        val callback = object : NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(ConnectionStatus.Available)
            }

            override fun onLost(network: Network) {
                trySend(ConnectionStatus.Lost)
            }

            override fun onLosing(network: Network, maxMsToLive: Int) {
                trySend(ConnectionStatus.Losing)
            }

            override fun onUnavailable() {
                trySend(ConnectionStatus.Unavailable)
            }
        }

        connectivityManager.registerDefaultNetworkCallback(callback)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }
}
