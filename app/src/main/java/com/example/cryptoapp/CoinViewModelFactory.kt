package com.example.cryptoapp

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider


class CoinViewModelFactory(private val application: Application?) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        Log.d("CoinViewModelFactory", "Requested ViewModel class: ${modelClass.name}")
        val app = application ?: throw IllegalStateException("Application is null")
        if (modelClass.isAssignableFrom(CoinViewModel::class.java)) {
            return CoinViewModel(app) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}