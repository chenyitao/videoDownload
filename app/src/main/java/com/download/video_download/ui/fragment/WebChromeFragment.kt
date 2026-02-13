package com.download.video_download.ui.fragment

import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.URLUtil
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.download.video_download.App
import com.download.video_download.R
import com.download.video_download.base.BaseFragment
import com.download.video_download.base.model.DetectState
import com.download.video_download.base.model.DetectStatus
import com.download.video_download.base.model.History
import com.download.video_download.base.room.entity.Video
import com.download.video_download.base.utils.DESUtil
import com.download.video_download.base.web.JsBg
import com.download.video_download.base.web.VideoParse
import com.download.video_download.base.web.VideoParse.isDouyinVideoUrl
import com.download.video_download.databinding.FragmentSearchChromeBinding
import com.download.video_download.ui.viewmodel.SearchViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Collections
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.text.isNullOrEmpty

class WebChromeFragment: BaseFragment<SearchViewModel, FragmentSearchChromeBinding>() {
    val searchViewModel: SearchViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )
    var parseJobList: MutableList<Job> = Collections.synchronizedList<Job>(mutableListOf())
    var task1 = ""
    var task2 = ""
    private var curUrl = ""
    var videoList: MutableList<Video> = CopyOnWriteArrayList()
    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSearchChromeBinding {
        return FragmentSearchChromeBinding.inflate(inflater, container, false)
    }

    override fun createViewModel(): SearchViewModel = searchViewModel

    override fun initViews(savedInstanceState: Bundle?) {
        val task1Str = DESUtil.readTxtFileSync(
            context = App.getAppContext(),
            fileName = "task1.txt"
        )
        if (task1Str.isNotEmpty()) {
            task1 = DESUtil.decryptCBC(task1Str)
        }
        val task2Str = DESUtil.readTxtFileSync(
            context = App.getAppContext(),
            fileName = "task2.txt"
        )
        if (task2Str.isNotEmpty()) {
            task2 = DESUtil.decryptCBC(task2Str)
        }
        binding.webview.apply {
            settings.domStorageEnabled = true
            settings.javaScriptEnabled = true
            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true
            settings.cacheMode = WebSettings.LOAD_DEFAULT
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
            settings.mediaPlaybackRequiresUserGesture = false
            settings.javaScriptCanOpenWindowsAutomatically = true
            settings.allowFileAccess = true
            settings.allowContentAccess = true
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            settings.userAgentString =
                "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
            CookieManager.getInstance().setAcceptCookie(true)
            CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
            webViewClient = webViewClient1
            webChromeClient = webChromeClient1
            addJavascriptInterface(
                JsBg(
                videoData = { videos, isLoading ->
                    updateVideoList(videos)
                    Log.d("VideoEngine", "videoData: $videos")
                    val currentHost =
                        curUrl.toUri().host
                    val findItem =
                        VideoParse.perWebsite.find {
                            currentHost?.contains(
                                it
                            ) == true
                        }
                    lifecycleScope.launch(Dispatchers.Main) {
                        findItem?.let {
                            searchViewModel.detect(DetectStatus(DetectState.SUPPORTWEB))
                        }
                        searchViewModel.isLoading(isLoading)
                        searchViewModel.saveVideos(videoList)
                    }
                },
                jobs = parseJobList,
                isInit = {
                    if (curUrl.isNotEmpty()) {
                        0
                    } else {
                        1
                    }
                }
            ), "VideoEngine")
        }
    }

    override fun initListeners() {
        binding.retry.setOnClickListener {
            binding.webview.reload()
            binding.llEmptyBtn.visibility = View.GONE
        }
        searchViewModel.setChromeUrl.observe( this, {
            val url = it
            if (url.isNotEmpty() && (URLUtil.isNetworkUrl(url) || url.contains("www.") || url.contains(".com"))){
                val host = url.toUri().host
                if (binding.webview.url == null || binding.webview.url?.contains(host.toString()) == false){
                    binding.webview.loadUrl(url)
                }
            }
        })
        searchViewModel.refreshWeb.observe(this){
            binding.webview.reload()
        }
        searchViewModel.goBackWeb.observe(this){
            if (binding.webview.canGoBack()) {
                binding.webview.goBack()
            }else{
                searchViewModel.notifyWebGoBackDone()
            }
        }
    }
    @Synchronized
    private fun updateVideoList(newVideos: List<Video>) {
        videoList.addAll(newVideos)

        val distinctMap = mutableMapOf<String, Video>()
        videoList.forEach { video ->
            distinctMap[video.url] = video
        }
        videoList.clear()
        videoList.addAll(distinctMap.values)
    }
    val webChromeClient1 by lazy {
        object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                binding.webProgress.progress = newProgress
                if (newProgress == 100) {
                    binding.webProgress.visibility = View.GONE // 加载完成隐藏
                } else {
                    binding.webProgress.visibility = View.VISIBLE // 加载中显示
                }
            }
        }
    }

    val webViewClient1 by lazy {
        object : WebViewClient() {
            override fun onPageStarted(
                view: WebView?,
                url: String?,
                favicon: Bitmap?
            ) {
                super.onPageStarted(view, url, favicon)
                curUrl = url?:""
                parseJobList.forEach { it.cancel() }
                videoList.clear()
                searchViewModel.saveVideos(videoList)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                curUrl = url?:""
                if (!URLUtil.isAboutUrl(url) && url?.contains("youtube") == false) {
                    if (isDouyinVideoUrl(url)) {
                        view?.evaluateJavascript(task1) { result ->
                        }
                    } else {
                        view?.evaluateJavascript(task2) { result ->
                        }
                    }
                    val currentHost =
                        curUrl.toUri().host
                    val findItem =
                        VideoParse.perWebsite.find {
                            currentHost?.contains(
                                it
                            ) == true
                        }
                    findItem?.let {
                        searchViewModel.detect(DetectStatus(DetectState.SUPPORTWEB))
                    }?:run {
                        searchViewModel.detect(DetectStatus(DetectState.YOUTUBE))
                    }
                } else {
                    searchViewModel.detect(DetectStatus(DetectState.YOUTUBE))
                }
                val currentHost =
                    url?.toUri()?.host
                if (!currentHost.isNullOrEmpty()) {
                    val curHistory = History(
                        url = currentHost,
                        time = System.currentTimeMillis()
                    )
                    searchViewModel.addHistory(curHistory)
                }
                if (!binding.empty.isVisible){
                    searchViewModel.setSearchInput(currentHost?:"")
                }
            }
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val uri = request?.url ?: return super.shouldOverrideUrlLoading(view, request)
                val url = uri.toString()
                return when {
                    URLUtil.isNetworkUrl(url) || URLUtil.isFileUrl(url) || URLUtil.isAboutUrl(
                        url
                    ) -> false

                    else -> {
                        true
                    }
                }
            }
            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): WebResourceResponse? {
                return super.shouldInterceptRequest(view, request)
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                if (request?.isForMainFrame == true) {
                    binding.empty.visibility = View.VISIBLE
                    binding.llEmptyBtn.visibility = View.VISIBLE
                    searchViewModel.setSearchInput(App.getAppContext().getString(R.string.load_error_web))
                }else{
                    binding.empty.visibility = View.GONE
                    binding.llEmptyBtn.visibility = View.GONE
                }
            }
            override fun onReceivedHttpError(
                view: WebView?,
                request: WebResourceRequest?,
                errorResponse: WebResourceResponse?
            ) {
                super.onReceivedHttpError(view, request, errorResponse)
                if (request?.isForMainFrame == true) {
                    binding.empty.visibility = View.VISIBLE
                    binding.llEmptyBtn.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        parseJobList.forEach {
            it.cancel()
        }
        parseJobList.clear()
    }
}