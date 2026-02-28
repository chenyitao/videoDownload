import android.content.Context
import android.content.res.Resources
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.download.video_download.App
import com.download.video_download.R
import com.download.video_download.base.BaseViewModel
import com.download.video_download.base.ad.AdMgr
import com.download.video_download.base.ad.model.AdLoadState
import com.download.video_download.base.ad.model.AdPosition
import com.download.video_download.base.ad.model.AdType
import com.download.video_download.base.model.LanguageSelectData
import com.download.video_download.base.utils.AppCache
import com.download.video_download.base.utils.LanguageUtils
import com.download.video_download.base.utils.LogUtils
import kotlinx.coroutines.launch
import java.util.Locale

class LanguageViewModel : BaseViewModel() {
    private val _isAdLoaded = MutableLiveData<Boolean>(false)

    val isAdLoaded: MutableLiveData<Boolean> = _isAdLoaded
    fun getSystemLanguageCode(): String {
        val systemLocale: Locale = Resources.getSystem().configuration.locale
        val language = systemLocale.language.takeIf { it.isNotBlank() } ?: "en"
        val country = systemLocale.country.takeIf { it.isNotBlank() } ?: ""
        if (language == "zh") {
            if (country == "TW" || country == "HK") {
                return "zh-rTW"
            }
            return "en"
        }
        val supportedLanguages = setOf("en", "ja", "ko", "fr", "de", "es", "pt", "ru", "ar", "hi", "it", "in", "fil", "th", "tr", "ur", "ms")
        val curlg = if (language in supportedLanguages) language else "en"
        AppCache.switchLanguage = curlg
        LanguageUtils.setAppLanguage(App.getAppContext(),curlg)
        return curlg
    }
    fun getLanguageList(): MutableList<LanguageSelectData> {
         val lg =  mutableListOf(
            LanguageSelectData(
                language = "Use system language",
                languageCode = getSystemLanguageCode(),  // 根据支持的语言判断
                isSelected = true,
                languageIv = R.mipmap.ic_default
            ),
            LanguageSelectData(
                language = "English",
                languageCode = "en",
                isSelected = false,
                languageIv = R.mipmap.ic_en
            ),
             LanguageSelectData(
                 language = "Bahasa Indonesia",
                 languageCode = "in",
                 isSelected = false,
                 languageIv = R.mipmap.ic_in
             ),
             LanguageSelectData(
                 language = "हिन्दी",
                 languageCode = "hi",
                 isSelected = false,
                 languageIv = R.mipmap.ic_hi
             ),
             LanguageSelectData(
                 language = "العربية",
                 languageCode = "ar",
                 isSelected = false,
                 languageIv = R.mipmap.ic_ar
             ),
             LanguageSelectData(
                 language = "Türkçe",
                 languageCode = "tr",
                 isSelected = false,
                 languageIv = R.mipmap.ic_tr
             ),
             LanguageSelectData(
                 language = "Deutsch",
                 languageCode = "de",
                 isSelected = false,
                 languageIv = R.mipmap.ic_de
             ),
            LanguageSelectData(
                language = "繁體中文",
                languageCode = "zh-rTW",
                isSelected = false,
                languageIv = R.mipmap.ic_zh
            ),
             LanguageSelectData(
                 language = "Русский",
                 languageCode = "ru",
                 isSelected = false,
                 languageIv = R.mipmap.ic_ru
             ),
             LanguageSelectData(
                 language = "Bahasa Melayu",
                 languageCode = "ms",
                 isSelected = false,
                 languageIv = R.mipmap.ic_ms
             ),
            LanguageSelectData(
                language = "日本語",
                languageCode = "ja",
                isSelected = false,
                languageIv = R.mipmap.ic_ja
            ),
            LanguageSelectData(
                language = "한국어",
                languageCode = "ko",
                isSelected = false,
                languageIv = R.mipmap.ic_ko
            ),
             LanguageSelectData(
                 language = "Português",
                 languageCode = "pt",
                 isSelected = false,
                 languageIv = R.mipmap.ic_pt
             ),
             LanguageSelectData(
                 language = "Italiano",
                 languageCode = "it",
                 isSelected = false,
                 languageIv = R.mipmap.ic_it
             ),
             LanguageSelectData(
                 language = "Français",
                 languageCode = "fr",
                 isSelected = false,
                 languageIv = R.mipmap.ic_fr
             ),
            LanguageSelectData(
                language = "Español",
                languageCode = "es",
                isSelected = false,
                languageIv = R.mipmap.ic_es
            ),
             LanguageSelectData(
                 language = "ภาษาไทย",
                 languageCode = "th",
                 isSelected = false,
                 languageIv = R.mipmap.ic_th
             ),

            LanguageSelectData(
                language = "Filipino",
                languageCode = "fil",
                isSelected = false,
                languageIv = R.mipmap.ic_fil
            ),
            LanguageSelectData(
                language = "اردو",
                languageCode = "ur",
                isSelected = false,
                languageIv = R.mipmap.ic_ur
            ),
         )
        LanguageUtils.setAppLanguage(App.getAppContext(),lg.get(0).languageCode)
        return lg
    }
     fun handleNativeAd(context: Context) {
         viewModelScope.launch {
             val hasCache = AdMgr.INSTANCE.getAdLoadState(AdPosition.LANGUAGE, AdType.NATIVE) == AdLoadState.LOADED
             if (hasCache){
                 isAdLoaded.postValue( true)
                 return@launch
             }
             AdMgr.INSTANCE.preloadAd(
                 AdPosition.LANGUAGE,
                 AdType.NATIVE,
                 context,
                 onLoadStateChanged = { position, adType, loadState,error ->
                     LogUtils.d("ad:  ${error?.message}${error?.domain}")
                     if (loadState == AdLoadState.LOADED){
                         isAdLoaded.postValue( true)
                     }
                 }
             )
         }
    }
    fun handInvestAd(context: Context){
        viewModelScope.launch {
            val hasCache = AdMgr.INSTANCE.getAdLoadState(AdPosition.LANGUAGE, AdType.INTERSTITIAL) == AdLoadState.LOADED
            if (hasCache){
                return@launch
            }
            AdMgr.INSTANCE.preloadAd(
                AdPosition.LANGUAGE,
                AdType.INTERSTITIAL,
                context,
                onLoadStateChanged = { position, adType, loadState,error ->
                    LogUtils.d("ad:  ${error?.message}${error?.domain}")
                }
            )
        }
    }
}