package com.appsforlife.cryptocourse.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.appsforlife.cryptocourse.api.ApiFactory
import com.appsforlife.cryptocourse.database.AppDatabase
import com.appsforlife.cryptocourse.models.CoinInfo
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class CoinViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val compositeDisposable = CompositeDisposable()

    var isLoading = MutableLiveData<Boolean>()

    val priceList = db.coinPriceInfoDao().getPriceList()

    fun getDetailInfo(id: String): LiveData<CoinInfo> {
        return db.coinPriceInfoDao().getDetail(id)
    }

    init {
        loadTopCoins()
    }

    private fun loadTopCoins() {
        val disposable = ApiFactory.apiService.getTopCoins()
            .map { it -> it.datumCoins.map { it.coinInfo } }
            .subscribeOn(Schedulers.io())
            .retry()
            .subscribe({
                isLoading.postValue(false)
                db.coinPriceInfoDao().insertCoinList(it)
                Log.d("LOAD_DATA", "Success $it")
            }, {
                Log.d("LOAD_DATA", "Failure ${it.message}")
                isLoading.postValue(false)
            })
        compositeDisposable.add(disposable)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }
}