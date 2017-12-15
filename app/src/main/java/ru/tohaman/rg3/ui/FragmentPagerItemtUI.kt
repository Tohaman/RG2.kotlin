package ru.tohaman.rg3.ui

import android.graphics.Typeface
import android.text.method.LinkMovementMethod
import android.view.Gravity
import android.widget.LinearLayout
import org.jetbrains.anko.*
import ru.tohaman.rg3.AnkoComponentEx
import ru.tohaman.rg3.youTubeThumbnailView

/**
 *  Класс для создания одного элемента PagerView для ViewPagerSlidingTab с помощью Anko
 */

class FragmentPagerItemtUI<Fragment> : AnkoComponentEx<Fragment>() {

    override fun create(ui: AnkoContext<Fragment>) = with(ui) {
        linearLayout {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.TOP
            scrollView{
                linearLayout {
                    orientation = LinearLayout.VERTICAL

                    linearLayout {
                        orientation = LinearLayout.HORIZONTAL
                        gravity = Gravity.CENTER_VERTICAL
//                      backgroundColor = Color.RED

                        imageView {
                            id = Ids.pagerImageView
                            padding = 8.dp
                        }.lparams(80.dp, 80.dp)

                        textView {
                            gravity = Gravity.CENTER
                            id = Ids.pagerTitleText
                            //Если текст ниже селектабельный, то и этот тоже надо делать таким,
                            //иначе текст будет автоматом прокручиваться при открытии view
//                            isSelectable = true
                            textSize = 20F
                            typeface = Typeface.DEFAULT_BOLD
                            padding = 10.dp

                        }.lparams(matchParent, wrapContent)
                    }.lparams(matchParent, wrapContent) {setMargins(16.dp, 10.dp, 16.dp, 10.dp)}

                    textView {
                        id = Ids.descriptionText
//                        isSelectable = true
                        // Делаем ссылки кликабельными
                        movementMethod = LinkMovementMethod.getInstance()
                        textSize = 15f
                    }.lparams(wrapContent, wrapContent)

                    linearLayout {
                        id = Ids.linLayout
//                        backgroundColor = Color.RED
                        gravity = Gravity.CENTER

                        youTubeThumbnailView {
                            id = Ids.youTubeView
                        }.lparams(matchParent, matchParent)

                        textView {
                            id = Ids.youTubeTextView
                            movementMethod = LinkMovementMethod.getInstance()
                        }
                    }.lparams(matchParent, 150.dp) {setMargins(0.dp, 8.dp, 0.dp, 8.dp)}

                    textView {
                        id = Ids.commentText
                        textSize = 16f
                        text = "Свой комментарий:"
                    }.lparams(matchParent, wrapContent) {setMargins(0.dp, 8.dp, 0.dp, 32.dp)}

                }.lparams(matchParent, wrapContent) {setMargins(16.dp, 0.dp, 16.dp, 0.dp)}
            }.lparams(matchParent, wrapContent)
        }
    }

    object Ids {
        val pagerTitleText = 1
        val pagerImageView = 2
        val descriptionText = 3
        val youTubeView = 5
        val commentText = 6
        val linLayout = 7
        val youTubeTextView = 8
    }
}
