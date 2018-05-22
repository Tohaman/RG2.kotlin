package ru.tohaman.rg2.ui

import android.graphics.Color
import android.graphics.Typeface
import android.support.v4.content.ContextCompat
import android.text.method.LinkMovementMethod
import android.view.Gravity
import android.widget.LinearLayout
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk15.coroutines.onClick
import ru.tohaman.rg2.R
import ru.tohaman.rg2.ankoconstraintlayout.constraintLayout

/**
 *  Класс для создания одного элемента PagerView для ViewPagerSlidingTab с помощью Anko
 */

class PagerItemtUI<in Fragment> : AnkoComponentEx<Fragment>() {

    override fun create(ui: AnkoContext<Fragment>) = with(ui) {
        linearLayout {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.TOP

            scrollView{
                linearLayout {
                    orientation = LinearLayout.VERTICAL

                    constraintLayout {
//                      backgroundColor = Color.RED

                        val imgView = imageView {
                            id = Ids.pagerImageView
//                            backgroundColor = Color.GREEN
                            padding = 8.dp
                        }.lparams(80.dp, 80.dp)

                        val txtView = textView {
                            gravity = Gravity.CENTER
                            id = Ids.pagerTitleText
                            //Если текст ниже селектабельный, то и этот тоже надо делать таким,
                            //иначе текст будет автоматом прокручиваться при открытии view
//                            isSelectable = true
                            textSize = 18F
                            typeface = Typeface.DEFAULT_BOLD
                            //padding = 10.dp

                        }.lparams(0.dp, wrapContent)

                        val chkBox = checkBox {
                            id = Ids.checkBox
//                            isChecked = true

//                            buttonDrawableResource = R.drawable.checkbox_star
                        }.lparams(0.dp, wrapContent)

                        constraints {
                            imgView.connect(
                                    TOPS of parentId,
                                    LEFTS of parentId)
                            txtView.connect(
                                    TOPS of parentId,
                                    BOTTOMS of parentId,
                                    RIGHT to LEFT of chkBox,
                                    LEFT to RIGHT of imgView)
                            chkBox.connect(
                                    TOPS of parentId,
                                    RIGHTS of parentId
                            )

                        }
                    }.lparams(matchParent, wrapContent) {setMargins(0.dp, 10.dp, 0.dp, 10.dp)}


                    textView {
                        id = Ids.descriptionText
//                        isSelectable = true
                        // Делаем ссылки кликабельными
                        movementMethod = LinkMovementMethod.getInstance()
                        textSize = 15f
                    }.lparams(wrapContent, wrapContent)

                    relativeLayout {
                        id = Ids.youTubeLayout
//                        backgroundColorResource = R.color.red

                        youTubeThumbnailView {
                            id = Ids.ytThumbnailView
                        }.lparams (matchParent, matchParent) {centerInParent()}

                        imageView (ContextCompat.getDrawable(context, R.drawable.ic_play)){
                            id = Ids.icPlayPreview

                        }.lparams(100.dp,100.dp) {centerInParent()}

                        textView {
                            id = Ids.youTubeTextView
                            movementMethod = LinkMovementMethod.getInstance()
                        }.lparams {centerInParent()}

                    }.lparams(matchParent, 160.dp) {setMargins(0.dp, 8.dp, 0.dp, 8.dp)}

                    textView {
                        id = Ids.commentText
                        textSize = 16f
                        textColorResource = R.color.colorAccent
                        text = ctx.getString(R.string.commentText)
                    }.lparams(matchParent, wrapContent) {setMargins(0.dp, 8.dp, 0.dp, 32.dp)}

                }.lparams(matchParent, wrapContent) {setMargins(16.dp, 0.dp, 16.dp, 0.dp)}
            }.lparams(matchParent, wrapContent)
        }
    }

    object Ids {
        const val pagerTitleText = 1
        const val pagerImageView = 2
        const val descriptionText = 3
        const val ytThumbnailView = 5
        const val commentText = 6
        const val youTubeLayout = 7
        const val youTubeTextView = 8
        const val icPlayPreview = 9
        const val checkBox = 10
    }
}
