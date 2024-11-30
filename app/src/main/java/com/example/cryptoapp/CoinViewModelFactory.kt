package com.example.cryptoapp

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider


class CoinViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CoinViewModel::class.java)) {
                return CoinViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
}
