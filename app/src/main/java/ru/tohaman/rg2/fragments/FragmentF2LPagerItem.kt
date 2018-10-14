package ru.tohaman.rg2.fragments

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.youtube.player.*
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk15.coroutines.onCheckedChange
import org.jetbrains.anko.support.v4.*
import ru.tohaman.rg2.DebugTag
import ru.tohaman.rg2.R
import ru.tohaman.rg2.data.F2lPhases
import ru.tohaman.rg2.data.Favorite
import ru.tohaman.rg2.data.ListPager
import ru.tohaman.rg2.data.ListPagerLab
import ru.tohaman.rg2.ui.F2LPagerItemtUI
import ru.tohaman.rg2.ui.PagerItemtUI
import ru.tohaman.rg2.util.spannedString
import ru.tohaman.rg2.util.toEditable
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
        val subId = arguments!!.getString("subID").toInt()
        val lp = listPagerLab.getPhaseItemList(id, phase)[subId]

        val title = lp.subLongTitle
        val topImage = lp.icon
        val description = lp.description
        var comment  = lp.comment
        //url = lp.url

        val titleTextView = view.findViewById<TextView>(F2LPagerItemtUI.Ids.pagerTitleText)
        titleTextView.text = title
        //Если основной текст селектабельный, то и этот тоже надо делать таким, иначе текст будет автоматом прокручиваться при открытии view
        titleTextView.isSelectable = PreferenceManager.getDefaultSharedPreferences(activity).getBoolean("is_text_selectable", false)

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
        imageView.imageResource = topImage

        val favCheckBox = view.findViewById<CheckBox>(PagerItemtUI.Ids.checkBox)
        //Пришлось делать вот так, а не через xml, которая задает изображение в зависимости от статуса,
        //т.к. иначе при смене через избранное кэшеруется не то изображение
        var favIsChecked = false
        favCheckBox.buttonDrawableResource = R.drawable.ic_favorite
        favoritesList.indices.forEach { i ->
            if ((favoritesList.elementAt(i).phase == phase) and (favoritesList.elementAt(i).id == id)) {
                favCheckBox.buttonDrawableResource = R.drawable.ic_favorite_checked
                favIsChecked = true
            }
        }
        favCheckBox.isChecked = favIsChecked

        favCheckBox.onCheckedChange { _, isChecked ->
            //Проверяем смену через переменную favIsChecked, чтобы не зацикливаться по нажатию кнопки "Отмена"
            //favIsChecked меняется только при нажатии ОК
            if (isChecked != favIsChecked) {
                if (isChecked) {
                    alert {
                        customView {
                            linearLayout {
                                orientation = LinearLayout.VERTICAL
                                textView {
                                    textResource = R.string.favoriteSetText
                                    textSize = 18F
                                }.lparams { setMargins(dip(8), dip(8), dip(8), dip(8)) }
                                val editTxt = editText {
                                    text = comment.toEditable()
                                }

                                positiveButton("OK") {
                                    favCheckBox.buttonDrawableResource = R.drawable.ic_favorite_checked
                                    listPagerLab.addFavorite(Favorite(phase, id, editTxt.text.toString()), ctx)
                                    if (mListener != null) {
                                        mListener!!.onViewPagerCheckBoxInteraction()
                                    }
                                    favIsChecked = true
                                }
                                negativeButton("Отмена") {
                                    favCheckBox.isChecked = false
                                }
                            }
                        }
                    }.show()

                } else {
                    alert {
                        customView {
                            linearLayout {
                                orientation = LinearLayout.VERTICAL
                                textView {
                                    textResource = R.string.favoriteUnSetText
                                    textSize = 18F
                                }.lparams { setMargins(dip(8), dip(8), dip(8), dip(8)) }

                                positiveButton("OK") {
                                    favCheckBox.buttonDrawableResource = R.drawable.ic_favorite
                                    listPagerLab.removeFavorite(phase, id, ctx)
                                    if (mListener != null) {
                                        mListener!!.onViewPagerCheckBoxInteraction()
                                    }
                                    favIsChecked = false
                                }
                                negativeButton("Отмена") {
                                    favCheckBox.isChecked = true
                                }
                            }
                        }
                    }.show()

                }
            }
        }



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
        fun onViewPagerCheckBoxInteraction() {
            Log.v(DebugTag.TAG, "F2LFragmentPagerItem onViewPagerCheckBoxInteraction")
        }
    }

    companion object {
        fun newInstance(lp: ListPager): FragmentF2LPagerItem {
            return FragmentF2LPagerItem().withArguments(
                    "phase" to lp.phase,
                    "id" to lp.id,
                    "subID" to lp.subID)
        }
    }
}
