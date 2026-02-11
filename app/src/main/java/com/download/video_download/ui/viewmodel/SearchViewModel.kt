package com.download.video_download.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.download.video_download.base.BaseViewModel
import com.download.video_download.base.model.History
import com.download.video_download.base.utils.AppCache
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString

class SearchViewModel: BaseViewModel() {
    private val _historyList = MutableStateFlow<List<History>>(emptyList())
    val historyList: StateFlow<List<History>> = _historyList.asStateFlow()
    init {
    }

    fun getHistoryData(){
        viewModelScope.launch {
            val historyStr = AppCache.history
            if (historyStr.isNotEmpty()){
                val historyList = json.decodeFromString<MutableList<History>>(historyStr)
                _historyList.value = historyList
            }
        }
    }
    fun addHistory(history: History){
        viewModelScope.launch {
            val historyStr = AppCache.history
            if (historyStr.isNotEmpty()){
                val oldHistoryList = json.decodeFromString<MutableList<History>>(historyStr)
                val index = oldHistoryList.filter { it.url == history.url }
                if (index.isNotEmpty()){
                    index.forEach {
                        oldHistoryList.remove(it)
                    }
                }
                if (oldHistoryList.size>= 10){
                    oldHistoryList.removeLast()
                }
                oldHistoryList.add( history)
                val newHistoryList = oldHistoryList.toList()
                _historyList.value = newHistoryList
                AppCache.history =  json.encodeToString( _historyList.value)
                return@launch
            }
            val historyList = mutableListOf<History>()
            historyList.add(history)
            _historyList.value = historyList
            AppCache.history =  json.encodeToString( _historyList.value)
        }
    }
    fun deleteHistory(history: History) {
        viewModelScope.launch {
            val currentList = _historyList.value.toMutableList()

            currentList.removeIf({ it.url == history.url })

            val newList = currentList.toList()
            _historyList.value = newList
            AppCache.history = json.encodeToString( _historyList.value)
        }
    }
}