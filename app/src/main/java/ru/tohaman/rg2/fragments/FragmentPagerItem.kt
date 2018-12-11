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
import ru.tohaman.rg2.ui.PagerItemUI
import ru.tohaman.rg2.util.spannedString
import ru.tohaman.rg2.util.toEditable

class FragmentPagerItem : Fragment(), YouTubeThumbnailView.OnInitializedListener {
    // Константы для YouTubePlayer
    private val REQ_START_STANDALONE_PLAYER = 101
    private val REQ_RESOLVE_SERVICE_MISSING = 2
    private var mListener: OnViewPagerInteractionListener? = null

    private var url:String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        // Создаем Вью
        val view = PagerItemUI<Fragment>().createView(AnkoContext.create(ctx, this))
        //получаем сиглет общей базы и избранного
        val listPagerLab = ListPagerLab.get(ctx)
        val favoritesList = listPagerLab.favorites

        //Данные во фрагмент передаются через фабричный метод newInstance данного фрагмента

        val phase = arguments!!.getString("phase")
        val id = arguments!!.getInt("id")
        val lp = listPagerLab.getPhaseItem(id, phase)
        val title = lp.title
        val topImage = lp.icon
        val description = lp.description
        var comment  = lp.comment
        url = lp.url

        var textString = "<html><body style=\"text-align:justify\"> %s </body></html>"
        var st = ""
        if (description != 0) {st = getString(description)}
        textString = String.format(textString, st)
        val spanText = spannedString(textString, imgGetter, tagHandler)
//        val spanText = Html.fromHtml("<br><p><strike>Test Strike</strike></p>", imgGetter, tagHandler)

        val mainTextView = view.findViewById<TextView>(PagerItemUI.Ids.descriptionText)

        val favCheckBox = view.findViewById<CheckBox>(PagerItemUI.Ids.checkBox)
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

        val isTextSelectable = PreferenceManager.getDefaultSharedPreferences(activity).getBoolean("is_text_selectable", false)

        (view.findViewById(PagerItemUI.Ids.pagerTitleText) as TextView).text = title
        //Если основной текст селектабельный, то и этот тоже надо делать таким, иначе текст будет автоматом прокручиваться при открытии view
        (view.findViewById(PagerItemUI.Ids.pagerTitleText) as TextView).isSelectable = isTextSelectable

        (view.findViewById(PagerItemUI.Ids.pagerImageView) as ImageView).imageResource = topImage

        val sp = PreferenceManager.getDefaultSharedPreferences(ctx)
        val textSize = sp.getString("text_size", "15").toFloat()
        mainTextView.textSize = textSize
        mainTextView.text = spanText
        mainTextView.isSelectable = isTextSelectable
        mainTextView.movementMethod = LinkMovementMethod.getInstance()


        val ytTextView = view.findViewById(PagerItemUI.Ids.youTubeTextView) as TextView

        // Если ссылка пустая, то вообще не отображаем видеопревью (скрываем лэйаут с текстом и превьюшкой)
        val ytViewLayout = view.findViewById(PagerItemUI.Ids.youTubeLayout) as RelativeLayout
        if (url == "") {
            ytViewLayout.visibility = View.GONE
        } else {
            ytViewLayout.visibility = View.VISIBLE
        }

        //смотрим в настройках программы, показывать превью видео или текст
        val previewEnabled = PreferenceManager.getDefaultSharedPreferences(activity).getBoolean(VIDEO_PREVIEW, true)
        val thumbnailView = view.findViewById(PagerItemUI.Ids.ytThumbnailView) as YouTubeThumbnailView
        val playPreviewImage = view.findViewById(PagerItemUI.Ids.icPlayPreview) as ImageView
        if (previewEnabled and canPlayYouTubeVideo()) {
            showYouTubePreview(thumbnailView, ytTextView, playPreviewImage)
        } else {
            hideYouTubePreview(thumbnailView, ytTextView, playPreviewImage)  //скрыть превью, отобразить текстовой ссылкой
        }

        //Выводим коммент, и делаем обработчик нажатия на него (вызваем окно редактирования)
        val commentText = view.findViewById<TextView>(PagerItemUI.Ids.commentText)
        commentText.text = (ctx.getString(R.string.commentText) + "\n" + comment)
        commentText.onClick { it ->
            alert(R.string.commentText) {
                customView {
                    val imm = ctx.inputMethodManager
                    val eText = editText {
                        inputType = InputType.TYPE_TEXT_FLAG_CAP_WORDS
                        text = comment.toEditable()
                    }
                    imm.toggleSoftInput(InputMethodManager.RESULT_SHOWN,0)

                    positiveButton("OK") {
                        imm.hideSoftInputFromWindow(eText.windowToken, 0)
                        val lps = listPagerLab.getPhaseItemByTitle(phase, title)
                        comment = eText.text.toString()
                        lps.comment = comment
                        listPagerLab.updateListPager(lps)
                        commentText.text = (ctx.getString(R.string.commentText) + "\n" + comment)
                    }
                    negativeButton("Отмена") {
                        imm.hideSoftInputFromWindow(eText.windowToken, 0)
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
        var text1 = if (canPlayYouTubeVideo()) {
            "<html><body> <a href=\"rg2://ytplay?time=0:00&link=%s\"> %s </a></body></html>"
        } else {
            "<html><body> <a href=\"https://www.youtube.com/watch?v=%s\"> %s </a></body></html>"
        }
        //https://www.youtube.com/watch?v=ENLnPS2eqPg&t=20s
        text1 = kotlin.String.format(text1, url, getString(R.string.pager_youtube_text))
        ytTextView.text = spannedString(text1, imgGetter, tagHandler)
//        ytTextView.text = Html.fromHtml("<br><mytag phase=1>Test Tag</mytag>", imgGetter, tagHandler)
    }

    private fun showYouTubePreview(thumbnailView: YouTubeThumbnailView, ytTextView: TextView, playPreviewImage: ImageView) {
        thumbnailView.visibility = View.VISIBLE
        playPreviewImage.visibility = View.VISIBLE
        ytTextView.visibility = View.GONE
        thumbnailView.initialize(DEVELOPER_KEY, this)
        thumbnailView.setOnClickListener {
            browse("rg2://ytplay?time=0:00&link=$url")
        }
    }

    private fun canPlayYouTubeVideo():Boolean = YouTubeApiServiceUtil.isYouTubeApiServiceAvailable(ctx) == YouTubeInitializationResult.SUCCESS

//    private fun playYouTubeVideo(needPlaying:Boolean, urlToPlay: String = "0TvO_rpG_aM"): Boolean {
//        val intent: Intent? = YouTubeStandalonePlayer.createVideoIntent(activity, DEVELOPER_KEY, urlToPlay, 1000, true, true)
//        return if (intent != null) when {
//            (canResolveIntent(intent)) and (needPlaying) -> {
//                startActivityForResult(intent, REQ_START_STANDALONE_PLAYER)
//                true
//            }
//            (!canResolveIntent(intent)) and (needPlaying) -> {
//                YouTubeInitializationResult.SERVICE_MISSING.getErrorDialog(activity, REQ_RESOLVE_SERVICE_MISSING).show()
//                false
//            }
//            !needPlaying -> {canResolveIntent(intent)}  //true or false
//            else -> {false}
//        } else false
//    }

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

    // И еще один метод для ЮТплеера = true, если есть приложение, которое может обрабоать наше намерение (интент)
    private fun canResolveIntent(intent: Intent): Boolean {
        val resolveInfo = activity!!.packageManager.queryIntentActivities(intent, 0)
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

    override fun onAttach(context: Context?) {
        Log.v (DebugTag.TAG, "FragmenPagerItem onAttach")
        super.onAttach(context)
        if (context is FragmentPagerItem.OnViewPagerInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnViewPagerInteractionListener")
        }
    }

    override fun onDetach() {
        Log.v (DebugTag.TAG, "FragmentPagerItem onDetach")
        super.onDetach()
        mListener = null
    }

    interface OnViewPagerInteractionListener {
        fun onViewPagerCheckBoxInteraction() {
            Log.v(DebugTag.TAG, "FragmentPagerItem onViewPagerCheckBoxInteraction")
        }
    }

    companion object {
        fun newInstance(lp: ListPager): FragmentPagerItem {
            return FragmentPagerItem().withArguments("phase" to lp.phase,
                    "id" to lp.id)
        }
    }
}
