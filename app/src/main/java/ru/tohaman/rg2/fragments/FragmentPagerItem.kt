package ru.tohaman.rg2.fragments

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.text.Editable
import android.text.Html
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubeStandalonePlayer
import com.google.android.youtube.player.YouTubeThumbnailLoader
import com.google.android.youtube.player.YouTubeThumbnailView
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk15.coroutines.onClick
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.support.v4.withArguments
import ru.tohaman.rg2.DebugTag
import ru.tohaman.rg2.DeveloperKey.DEVELOPER_KEY
import ru.tohaman.rg2.R
import ru.tohaman.rg2.VIDEO_PREVIEW
import ru.tohaman.rg2.data.ListPager
import ru.tohaman.rg2.data.ListPagerLab
import ru.tohaman.rg2.ui.PagerItemtUI
import ru.tohaman.rg2.util.spannedString
import ru.tohaman.rg2.util.toEditable

class FragmentPagerItem : Fragment(), YouTubeThumbnailView.OnInitializedListener {
    // Константы для YouTubePlayer
    private val REQ_START_STANDALONE_PLAYER = 101
    private val REQ_RESOLVE_SERVICE_MISSING = 2
    private val RECOVERY_DIALOG_REQUEST = 1

    var url:String = ""

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        // Создаем Вью
        val view = PagerItemtUI<Fragment>().createView(AnkoContext.create(context, this))

        //Данные во фрагмент передаются через фабричный метод newInstance данного фрагмента
        //TODO переделать на передачу через uri
        val phase = arguments.getString("phase")
        val message = arguments.getString("title")
        val topImage = arguments.getInt("topImage")
        val description = arguments.getInt("desc")
        val comment  = arguments.getString("comment")
        url = arguments.getString("url")

        var textString = "<html><body style=\"text-align:justify\"> %s </body></html>"
        val st = getString(description)
        textString = String.format(textString, st)
        val spanText = spannedString(textString, imgGetter)

        (view.findViewById(PagerItemtUI.Ids.pagerTitleText) as TextView).text = message
        (view.findViewById(PagerItemtUI.Ids.pagerImageView) as ImageView).imageResource = topImage
        (view.findViewById(PagerItemtUI.Ids.descriptionText) as TextView).text = spanText

        val ytTextView = view.findViewById(PagerItemtUI.Ids.youTubeTextView) as TextView

        // Если ссылка пустая, то вообще не отображаем видеопревью (скрываем лэйаут с текстом и превьюшкой)
        val ytViewLayout = view.findViewById(PagerItemtUI.Ids.youTubeLayout) as RelativeLayout
        if (url == "") {
            ytViewLayout.visibility = View.GONE
        } else {
            ytViewLayout.visibility = View.VISIBLE
        }

        //смотрим в настройках программы, показывать превью видео или текст
        val previewEnabled = PreferenceManager.getDefaultSharedPreferences(activity).getBoolean(VIDEO_PREVIEW, true)
        val thumbnailView = view.findViewById(PagerItemtUI.Ids.ytThumbnailView) as YouTubeThumbnailView
        val playPreviewImage = view.findViewById(PagerItemtUI.Ids.icPlayPreview) as ImageView
        if (previewEnabled and canPlayYouTubeVideo()) {
            showYouTubePreview(thumbnailView, ytTextView, playPreviewImage)
        } else {
            hideYouTubePreview(thumbnailView, ytTextView, playPreviewImage)  //скрыть превью, отобразить текстовой ссылкой
        }

        val commentText = view.findViewById<TextView>(PagerItemtUI.Ids.commentText)
        commentText.text = (ctx.getString(R.string.commentText) + " " + comment)
        commentText.onClick {
            alert {
                customView {
                    linearLayout {
                        orientation = LinearLayout.VERTICAL
                        textView {
                            textResource = R.string.commentText
                            textSize = 18F
                        }
                        val editTxt = editText {
                            inputType = InputType.TYPE_TEXT_FLAG_CAP_WORDS
                            text = comment.toEditable()
                        }

                        positiveButton("OK") {
                            val lpLab = ListPagerLab.get(ctx)
                            val lps = lpLab.getPhaseItemByTitle(phase, message)
                            val cmnt = editTxt.text.toString()
                            lps.comment = cmnt
                            lpLab.updateListPager(lps)
                            commentText.text = (ctx.getString(R.string.commentText) + " " + cmnt)
                        }
                        negativeButton("Отмена") {}
                    }
                }
            }.show()
        }

        return view
    }

    private fun hideYouTubePreview(thumbnailView: YouTubeThumbnailView, ytTextView: TextView, playPreviewImage: ImageView) {
        thumbnailView.visibility = View.GONE
        playPreviewImage.visibility = View.GONE
        ytTextView.visibility = View.VISIBLE
        var text1 = "<html><body> <a href=\"rg2://ytplay?time=0:00&link=%s\"> %s </a></body></html>"
        text1 = kotlin.String.format(text1, url, getString(R.string.pager_youtube_text))
        ytTextView.text = spannedString(text1, imgGetter)
    }

    private fun showYouTubePreview(thumbnailView: YouTubeThumbnailView, ytTextView: TextView, playPreviewImage: ImageView) {
        thumbnailView.visibility = View.VISIBLE
        playPreviewImage.visibility = View.VISIBLE
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
        var sourceString = source
        val drawable: Drawable
        sourceString = sourceString.replace(".png", "")
        sourceString = sourceString.replace(".xml", "")
        var resID = resources.getIdentifier(sourceString, "drawable", activity.packageName)
        //если картинка в drawable не найдена, то подсовываем заведомо существующую картинку
        if (resID == 0) {
            resID = resources.getIdentifier("ic_warning", "drawable", activity.packageName)
        }
        drawable = ContextCompat.getDrawable(ctx,resID)

        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        drawable
    }

    // И еще один метод для ЮТплеера = true, если есть приложение, которое может обрабоать наше намерение (интент)
    private fun canResolveIntent(intent: Intent): Boolean {
        val resolveInfo = activity.packageManager.queryIntentActivities(intent, 0)
        return resolveInfo != null && !resolveInfo.isEmpty()
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
            //TODO сделать передачу через uri
            return FragmentPagerItem().withArguments("phase" to lp.phase,
                    "title" to lp.title,
                    "topImage" to lp.icon,
                    "desc" to lp.description,
                    "url" to lp.url,
                    "comment" to lp.comment)
        }
    }
}
