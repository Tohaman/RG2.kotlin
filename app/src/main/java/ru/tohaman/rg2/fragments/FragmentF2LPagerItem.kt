package ru.tohaman.rg2.fragments

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.google.android.youtube.player.*
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.*
import ru.tohaman.rg2.DebugTag
import ru.tohaman.rg2.data.F2lPhases
import ru.tohaman.rg2.data.ListPager
import ru.tohaman.rg2.data.ListPagerLab
import ru.tohaman.rg2.ui.F2LPagerItemtUI
import ru.tohaman.rg2.ui.PagerItemtUI
import ru.tohaman.rg2.util.cubeColor
import ru.tohaman.rg2.util.spannedString
import java.util.ArrayList

class FragmentF2LPagerItem : Fragment(), YouTubeThumbnailView.OnInitializedListener {
    private var mListener: OnViewPagerInteractionListener? = null

    private var url:String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        // Создаем Вью
        val view = F2LPagerItemtUI<Fragment>().createView(AnkoContext.create(ctx, this))
        //получаем сиглет общей базы и избранного
        val listPagerLab = ListPagerLab.get(ctx)
        val favoritesList = listPagerLab.favorites

        //Данные во фрагмент передаются через фабричный метод newInstance данного фрагмента

        val phase = arguments!!.getString("phase")
        val id = arguments!!.getInt("id")
        val subId = arguments!!.getString("subId").toInt()
        val lp = listPagerLab.getPhaseItemList(id, phase)[subId]

        val title = lp.slot
        val topImage = lp.icon
        val description = lp.description
        var comment  = lp.comment
        //url = lp.url

        val titleTextView = view.findViewById<TextView>(F2LPagerItemtUI.Ids.pagerTitleText)
        titleTextView.text = title


        val gson = GsonBuilder().create()
        val itemsListType = object : TypeToken<ArrayList<F2lPhases>>() {}.type
        var textString = "<html><body style=\"text-align:justify\"> %s </body></html>"
        val st = getString(description)
        val listOfTexts : ArrayList<F2lPhases> = gson.fromJson(st, itemsListType)
        textString = String.format(textString, listOfTexts[subId].text)
        val spanText = spannedString(textString, imgGetter, tagHandler)

        val mainTextView = view.findViewById<TextView>(F2LPagerItemtUI.Ids.descriptionText)
        mainTextView.text = spanText

        val imageView = view.findViewById<ImageView>(F2LPagerItemtUI.Ids.pagerImageView)

//        return LayerDrawable( Array(28) { i ->
//            //получаем drawable по имени "z_2s_0$i"
//            val drw = ContextCompat.getDrawable(ctx, resources.getIdentifier("z_2s_0$i", "drawable", this.packageName))
//            //раскрашиваем цветом кубика
//            DrawableCompat.setTint(drw!!, ContextCompat.getColor(ctx, cubeColor[genScrambleCube[27-i]]))
//            drw
//        })
        //val layerDrawable = LayerDrawable (ContextCompat.getDrawable(ctx,topImage)!!)
        imageView.image = ContextCompat.getDrawable(ctx,topImage)!!

        return view
    }


    @Suppress("DEPRECATION")
    private val imgGetter = Html.ImageGetter { source ->
        var sourceString = source
        val drawable: Drawable
        sourceString = sourceString.replace(".png", "")
        sourceString = sourceString.replace(".xml", "")
        var resID = resources.getIdentifier(sourceString, "drawable", activity?.packageName)
        //если картинка в drawable не найдена, то подсовываем заведомо существующую картинку
        if (resID == 0) {
            resID = resources.getIdentifier("ic_warning", "drawable", activity?.packageName)
        }
        drawable = ContextCompat.getDrawable(ctx,resID)!!

        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        drawable
    }

    private val tagHandler = Html.TagHandler { opening, tag, output, xmlReader ->
        //Тут можно обрабатывать свои тэги
        if (tag.equals("mytag", true)) {
            val open = opening
            val tag1 = tag
            val out = output
            val xml = xmlReader

        }
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

    override fun onAttach(context: Context?) {
        Log.v (DebugTag.TAG, "F2LFragmentPagerItem onAttach")
        super.onAttach(context)
        if (context is FragmentF2LPagerItem.OnViewPagerInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnViewPagerInteractionListener")
        }
    }

    override fun onDetach() {
        Log.v (DebugTag.TAG, "F2LFragmentPagerItem onDetach")
        super.onDetach()
        mListener = null
    }

    interface OnViewPagerInteractionListener {
//        fun onViewPagerCheckBoxInteraction() {
//            Log.v(DebugTag.TAG, "F2LFragmentPagerItem onViewPagerCheckBoxInteraction")
//        }
    }

    companion object {
        fun newInstance(lp: ListPager): FragmentF2LPagerItem {
            return FragmentF2LPagerItem().withArguments(
                    "phase" to lp.phase,
                    "id" to lp.id,
                    "subId" to lp.subID)
        }
    }
}
