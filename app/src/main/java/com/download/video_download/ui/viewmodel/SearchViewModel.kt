package com.download.video_download.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.download.video_download.base.BaseViewModel
import com.download.video_download.base.model.DetectStatus
import com.download.video_download.base.model.History
import com.download.video_download.base.model.SearchState
import com.download.video_download.base.room.entity.Video
import com.download.video_download.base.utils.AppCache
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString

class SearchViewModel: BaseViewModel() {
    private val _historyList = MutableStateFlow<List<History>>(emptyList())
    val historyList: StateFlow<List<History>> = _historyList.asStateFlow()
    private val _nav = MutableLiveData<SearchState?>()
    val nav: LiveData<SearchState?> get() = _nav
    private val _showGuide = MutableLiveData<Boolean>()
    val showGuide: LiveData<Boolean> get() = _showGuide
    private val _setChromeUrl = MutableLiveData<String>()
    val setChromeUrl: LiveData<String> get() = _setChromeUrl
    private val _setSearchInput = MutableLiveData<String>()
    val setSearchInput: LiveData<String> get() = _setSearchInput
    private val _refreshWeb = MutableLiveData<Boolean>()
    val refreshWeb: LiveData<Boolean> get() = _refreshWeb
    private val _goBackWeb = MutableLiveData<Boolean>()
    val goBackWeb: LiveData<Boolean> get() = _goBackWeb
    private val _goBackDone = MutableLiveData<Boolean>()
    val goBackDone: LiveData<Boolean> get() = _goBackDone
    private val _videos = MutableLiveData<MutableList<Video>>()
    val videos: LiveData<MutableList<Video>> get() = _videos
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading
    private val _detect= MutableLiveData<DetectStatus>()
    val detect: LiveData<DetectStatus> get() = _detect
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
                    oldHistoryList.removeAt(oldHistoryList.lastIndex)
                }
                oldHistoryList.add( history)
                oldHistoryList.sortByDescending { it.time }
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
    fun  clearHistory() {
        viewModelScope.launch {
            _historyList.value = emptyList()
            AppCache.history = ""
        }
    }
    fun navigate(item: SearchState) {
        _nav.value = item
    }
    fun clearNavigation() {
        _nav.value = null
    }
    fun showGuide() {
        _showGuide.value = AppCache.isFirstDetect
    }
    fun setChromeUrl(url: String) {
        _setChromeUrl.value = url
    }
    fun clearChromeUrl() {
        _setChromeUrl.value = ""
    }
    override fun onCleared() {
        super.onCleared()
    }

    fun setSearchInput(input: String) {
        _setSearchInput.value = input
    }

    fun refreshWeb(){
        _refreshWeb.value = true
    }
    fun canWebGoBack() {
       _goBackWeb.value = true
    }
    fun notifyWebGoBackDone() {
        _goBackDone.value = true
    }
    fun saveVideos(videos: MutableList<Video>) {
        _videos.value = videos
    }
    fun isLoading(isLoading: Boolean) {
        _isLoading.value = isLoading
    }
    fun detect(state: DetectStatus) {
        _detect.value = state
    }
}