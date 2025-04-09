package com.isis3510.growhub.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.isis3510.growhub.utils.ConnectionStatus
import com.isis3510.growhub.utils.ConnectivityObserver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ConnectivityViewModel(application: Application) : AndroidViewModel(application) {
    private val connectivityObserver = ConnectivityObserver(application)

    private val _networkStatus = MutableStateFlow(ConnectionStatus.Unavailable)
    val networkStatus = _networkStatus.asStateFlow()

    init {
        monitorNetwork()
    }

    private fun monitorNetwork() {
        viewModelScope.launch {
            connectivityObserver.connectionStatus.collectLatest { status ->
                _networkStatus.value = status
            }
        }
    }
}
