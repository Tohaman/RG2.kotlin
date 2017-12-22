package ru.tohaman.rg3.fragments

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.text.Html
import android.text.Spanned
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubeStandalonePlayer
import com.google.android.youtube.player.YouTubeThumbnailLoader
import com.google.android.youtube.player.YouTubeThumbnailView
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.support.v4.withArguments
import ru.tohaman.rg3.DebugTag
import ru.tohaman.rg3.DeveloperKey.DEVELOPER_KEY
import ru.tohaman.rg3.R
import ru.tohaman.rg3.VIDEO_PREVIEW
import ru.tohaman.rg3.data.ListPager
import ru.tohaman.rg3.ui.FragmentPagerItemtUI

class FragmentPagerItem : Fragment(), YouTubeThumbnailView.OnInitializedListener {
    // Константы для YouTubePlayer
    private val REQ_START_STANDALONE_PLAYER = 101
    private val REQ_RESOLVE_SERVICE_MISSING = 2
    private val RECOVERY_DIALOG_REQUEST = 1

    var url:String = ""

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        // Создаем Вью
        val view = FragmentPagerItemtUI<Fragment>().createView(AnkoContext.create(context, this))

        //Данные во фрагмент передаются через фабричный метод newInstance данного фрагмента
        val message = arguments.getString("title")
        val topImage = arguments.getInt("topImage")
        val description = arguments.getInt("desc")
        url = arguments.getString("url")

        var text = "<html><body style=\"text-align:justify\"> %s </body></html>"
        val st = getString(description)
        text = String.format(text, st)
        val spantext = spannedString(text)

        (view.findViewById(FragmentPagerItemtUI.Ids.pagerTitleText) as TextView).text = message
        (view.findViewById(FragmentPagerItemtUI.Ids.pagerImageView) as ImageView).imageResource = topImage
        (view.findViewById(FragmentPagerItemtUI.Ids.descriptionText) as TextView).text = spantext

        val ytTextView = view.findViewById(FragmentPagerItemtUI.Ids.youTubeTextView) as TextView

        // Если ссылка пустая, то вообще не отображаем видеопревью (скрываем лэйаут с текстом и превьюшкой)
        val ytViewLayout = view.findViewById(FragmentPagerItemtUI.Ids.youTubeLayout) as RelativeLayout
        if (url == "") {
            ytViewLayout.visibility = View.GONE
        } else {
            ytViewLayout.visibility = View.VISIBLE
        }

        //смотрим в настройках программы, показывать превью видео или текст
        val previewEnabled = PreferenceManager.getDefaultSharedPreferences(activity).getBoolean(VIDEO_PREVIEW, true)
        val thumbnailView = view.findViewById(FragmentPagerItemtUI.Ids.ytThumbnailView) as YouTubeThumbnailView
        val playPreviewImage = view.findViewById(FragmentPagerItemtUI.Ids.icPlayPreview) as ImageView
        if (previewEnabled and canPlayYouTubeVideo()) {
            showYouTubePreview(thumbnailView, ytTextView, playPreviewImage)
        } else {
            hideYouTubePreview(thumbnailView, ytTextView, playPreviewImage, text)  //скрыть превью, отобразить текстовой ссылкой
        }

        return view
    }

    private fun hideYouTubePreview(thumbnailView: YouTubeThumbnailView, ytTextView: TextView, playPreviewImage: ImageView, text: String) {
        var text1 = text
        thumbnailView.visibility = View.GONE
        playPreviewImage.visibility = View.GONE
        ytTextView.visibility = View.VISIBLE
        text1 = "<html><body> <a href=\"rg2://ytplay?time=0:00&link=%s\"> %s </a></body></html>"
        text1 = kotlin.String.format(text1, url, getString(R.string.pager_youtubetext))
        ytTextView.text = spannedString(text1)
    }

    private fun showYouTubePreview(thumbnailView: YouTubeThumbnailView, ytTextView: TextView, playPreviewImage: ImageView) {
        thumbnailView.visibility = View.VISIBLE
        playPreviewImage.visibility = View.GONE
        ytTextView.visibility = View.GONE
        thumbnailView.initialize(DEVELOPER_KEY, this)
        thumbnailView.setOnClickListener {
            playYouTubeVideo(true, url)
        }
    }


    private fun canPlayYouTubeVideo():Boolean = playYouTubeVideo(false)

    private fun playYouTubeVideo(needPlaying:Boolean, urlToPlay: String = "0TvO_rpG_aM"): Boolean {
        val intent: Intent? = YouTubeStandalonePlayer.createVideoIntent(activity, DEVELOPER_KEY, urlToPlay, 1000, true, true)
        if (intent != null) return when {
                (canResolveIntent(intent)) and (needPlaying) -> {
                    startActivityForResult(intent, REQ_START_STANDALONE_PLAYER)
                    true
                }
                (!canResolveIntent(intent)) and (needPlaying) -> {
                    YouTubeInitializationResult.SERVICE_MISSING.getErrorDialog(activity, REQ_RESOLVE_SERVICE_MISSING).show()
                    false
                }
                !needPlaying -> {canResolveIntent(intent)}  //true or false
                else -> {false}
            } else return false
    }

    @Suppress("DEPRECATION")
    private val imgGetter = Html.ImageGetter { source ->
        var source = source
        val drawable: Drawable
        source = source.replace(".png", "")
        source = source.replace(".xml", "")
        var resID = resources.getIdentifier(source, "drawable", activity.packageName)
        //если картинка в drawable не найдена, то подсовываем заведомо существующую картинку
        if (resID == 0) {
            resID = resources.getIdentifier("ic_warning", "drawable", activity.packageName)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            drawable = resources.getDrawable(resID, null)
        } else {
            drawable = resources.getDrawable(resID)
        }

        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)

        drawable
    }

    // И еще один метод для ЮТплеера = true, если есть приложение, которое может обрабоать наше намерение (интент)
    private fun canResolveIntent(intent: Intent): Boolean {
        val resolveInfo = activity.packageManager.queryIntentActivities(intent, 0)
        return resolveInfo != null && !resolveInfo.isEmpty()
    }

    private fun spannedString(desc:String): Spanned {
        // Немного преобразуем текст для корректного отображения.
        val desc = desc.replace("%%", "%25")

        // Android 7.0 ака N (Nougat) = API 24, начиная с версии Андроид 7.0 вместо HTML.fromHtml (String)
        // лучше использовать HTML.fromHtml (String, int), где int различные флаги, влияющие на отображение html
        // аналогично для метода HTML.fromHtml (String, ImageGetter, TagHandler) -> HTML.fromHtml (String, int, ImageGetter, TagHandler)
        // поэтому используем @SuppressWarnings("deprecation") перед объявлением метода и вот такую конструкцию
        // для преобразования String в Spanned. В принципе использование старой конструкции равноценно использованию
        // новой с флагом Html.FROM_HTML_MODE_LEGACY... подробнее о флагах-модификаторах на developer.android.com
        // В методе Html.fromHtml(String, imgGetter, tagHandler) - tagHandler - это метод, который вызывется, если
        // в строке встречается тэг, который не распознан, т.е. тут можно обрабатывать свои тэги
        // пока не используется (null), но все воозможно :)

        val spanresult: Spanned
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            spanresult = Html.fromHtml(desc, Html.FROM_HTML_MODE_LEGACY, imgGetter, null)
        } else {
            @Suppress("DEPRECATION")
            spanresult = Html.fromHtml(desc, imgGetter, null)
        }
        return spanresult
    }


    //Два обязательных переопределяемых метода для имплементного YouTubeThumbnailView.OnInitializedListener
    override fun onInitializationSuccess(p0: YouTubeThumbnailView?, p1: YouTubeThumbnailLoader?) {
        //если удачно инициализировали, то передаем краткий url видео, для автосоздания превью видео
        p1?.setVideo(url)
    }

    // если не удалось инициализировать youTubeThumbnailView
    override fun onInitializationFailure(p0: YouTubeThumbnailView?, errorReason: YouTubeInitializationResult?) {
        if (errorReason!!.isUserRecoverableError) {
            Log.v(DebugTag.TAG, "YouTube onInitializationFailure errorReason.isUserRecoverableError")

        } else {
            Log.v(DebugTag.TAG, "YouTube onInitializationFailure Ошибка инициализации YouTubePlayer")
            val errorMessage = "Ошибка инициализации YouTubePlayer"
            toast(errorMessage)
        }
    }

    companion object {
        fun newInstance(lp: ListPager): FragmentPagerItem {
            return FragmentPagerItem().withArguments("title" to lp.title,
                    "topImage" to lp.icon,
                    "desc" to lp.description,
                    "url" to lp.url)
        }
    }
}
