package ru.tohaman.rg2.fragments

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.text.Html
import android.text.InputType
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.google.android.youtube.player.*
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk15.coroutines.onCheckedChange
import org.jetbrains.anko.sdk15.coroutines.onClick
import org.jetbrains.anko.support.v4.*
import ru.tohaman.rg2.DebugTag
import ru.tohaman.rg2.DeveloperKey.DEVELOPER_KEY
import ru.tohaman.rg2.R
import ru.tohaman.rg2.VIDEO_PREVIEW
import ru.tohaman.rg2.data.Favorite
import ru.tohaman.rg2.data.ListPager
import ru.tohaman.rg2.data.ListPagerLab
import ru.tohaman.rg2.ui.OllPagerItemtUI
import ru.tohaman.rg2.ui.PagerItemtUI
import ru.tohaman.rg2.util.spannedString
import ru.tohaman.rg2.util.toEditable

class FragmentOllPagerItem : Fragment(), YouTubeThumbnailView.OnInitializedListener {
    // Константы для YouTubePlayer
    private var mListener: OnViewPagerInteractionListener? = null
    private var url:String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        // Создаем Вью
        val view = OllPagerItemtUI<Fragment>().createView(AnkoContext.create(ctx, this))
        //получаем сиглет общей базы и избранного
        val listPagerLab = ListPagerLab.get(ctx)

        val mainTextView = view.findViewById<TextView>(OllPagerItemtUI.Ids.descriptionText)

        return view
    }

    private fun canPlayYouTubeVideo():Boolean = YouTubeApiServiceUtil.isYouTubeApiServiceAvailable(ctx) == YouTubeInitializationResult.SUCCESS



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

    override fun onAttach(context: Context?) {
        Log.v (DebugTag.TAG, "FragmentOllPagerItem onAttach")
        super.onAttach(context)
        if (context is FragmentOllPagerItem.OnViewPagerInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnViewPagerInteractionListener")
        }
    }

    override fun onDetach() {
        Log.v (DebugTag.TAG, "FragmentOllPagerItem onDetach")
        super.onDetach()
        mListener = null
    }

    interface OnViewPagerInteractionListener

    companion object {
        fun newInstance(text: String): FragmentOllPagerItem {
            return FragmentOllPagerItem().withArguments("text" to text)
        }
    }
}
