package com.example.cryptoapp

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.cryptoapp.api.ApiFactory
import com.example.cryptoapp.database.AppDatabase
import com.example.cryptoapp.pojo.CoinPriceInfo
import com.example.cryptoapp.pojo.CoinPriceInfoRawData
import com.google.gson.Gson
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class CoinViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val compositeDisposable = CompositeDisposable()

    val priceList = db.coinPriceInfoDao().getPriceList()

    fun getDetailInfo(fSym: String): LiveData<CoinPriceInfo> {
        return db.coinPriceInfoDao().getPriceInfoAboutCoin(fSym)
    }

    init {
        loadData()
    }


    private fun loadData() {
        val disposable = ApiFactory.apiService.getTopCoinsInfo(limit = 50)
            .doOnSubscribe { Log.d("LOAD_DATA", "Fetching top coins info") }
            .doOnError { Log.e("LOAD_DATA", "Error fetching top coins info: ${it.message}", it) }
            .map {
                it.data?.map { it.coinInfo?.name }?.joinToString(",")
            }
            .doOnSuccess { Log.d("LOAD_DATA", "Top coins: $it") }
            .flatMap { ApiFactory.apiService.getFullPriceList(fSyms = it) }
            .doOnError { Log.e("LOAD_DATA", "Error fetching price list: ${it.message}", it) }
            .map { getPriceListFromRawData(it) }
            .doOnSuccess{ Log.d("LOAD_DATA", "Parsed price list: $it") }
            .delaySubscription(10, TimeUnit.SECONDS)
            .repeat()
            .retry()
            .subscribeOn(Schedulers.io())
            .subscribe({
                db.coinPriceInfoDao().insertPriceList(it)
                Log.d("TEST_OF_LOADING_DATA", "Success: $it")
            }, {
                Log.e("TEST_OF_LOADING_DATA", "Failure: ${it.message}", it)
            })
        compositeDisposable.add(disposable)
    }

    private fun getPriceListFromRawData(
        coinPriceInfoRawData: CoinPriceInfoRawData
    ): List<CoinPriceInfo> {
        val result = ArrayList<CoinPriceInfo>()
        val jsonObject = coinPriceInfoRawData.coinPriceInfoJsonObject ?: return result
        val coinKeySet = jsonObject.keySet()
        for (coinKey in coinKeySet) {
            val currencyJson = jsonObject.getAsJsonObject(coinKey)
            val currencyKeySet = currencyJson.keySet()
            for (currencyKey in currencyKeySet) {
                val priceInfo = Gson().fromJson(
                    currencyJson.getAsJsonObject(currencyKey),
                    CoinPriceInfo::class.java
                )
                result.add(priceInfo)
            }
        }
        return result
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }
}