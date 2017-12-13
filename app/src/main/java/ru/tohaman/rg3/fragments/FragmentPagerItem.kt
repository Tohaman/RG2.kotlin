package ru.tohaman.rg3.fragments

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Html
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubeStandalonePlayer
import com.google.android.youtube.player.YouTubeThumbnailLoader
import com.google.android.youtube.player.YouTubeThumbnailView
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.image
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.support.v4.withArguments
import ru.tohaman.rg3.DebugTag
import ru.tohaman.rg3.DeveloperKey.DEVELOPER_KEY
import ru.tohaman.rg3.listpager.ListPager

class FragmentPagerItem : Fragment(), YouTubeThumbnailView.OnInitializedListener {
    var url:String = ""
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = FragmentPagerItemtUI<Fragment>().createView(AnkoContext.create(context, this))
        val message = arguments.getString("message")
        val topImage = arguments.getInt("topImage")
        val description = arguments.getInt("desc")
        url = arguments.getString("url")

        // Немного преобразуем текст для корректного отображения.
        val text = "<html><body style=\"text-align:justify\"> %s </body></html>"
        var desc = String.format(text, getString(description))
        desc = desc.replace("%%", "%25")

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

        (view.findViewById(FragmentPagerItemtUI.Ids.textViewFragmentMessage) as TextView).text = message
        (view.findViewById(FragmentPagerItemtUI.Ids.pager_imageView) as ImageView).imageResource = topImage
        (view.findViewById(FragmentPagerItemtUI.Ids.description_text) as TextView).text = spanresult

        val thumbnailView = view.findViewById(FragmentPagerItemtUI.Ids.youTubeView) as YouTubeThumbnailView
        if (url == "") {
            thumbnailView.visibility = View.INVISIBLE
        } else {
            thumbnailView.visibility = View.VISIBLE
        }

        thumbnailView.visibility = View.VISIBLE
        thumbnailView.initialize(DEVELOPER_KEY, this )

        return view
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

    override fun onInitializationSuccess(p0: YouTubeThumbnailView?, p1: YouTubeThumbnailLoader?) {
        p1?.setVideo(url)
    }

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
            return FragmentPagerItem().withArguments("message" to lp.title,
                    "topImage" to lp.icon,
                    "desc" to lp.description,
                    "url" to lp.url)
        }
    }
}
