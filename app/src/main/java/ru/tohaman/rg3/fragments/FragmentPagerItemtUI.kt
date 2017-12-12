package ru.tohaman.rg3.fragments

import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.widget.LinearLayout
import org.jetbrains.anko.*

class FragmentPagerItemtUI<Fragment> : AnkoComponent<Fragment> {

    override fun createView(ui: AnkoContext<Fragment>) = with(ui) {
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
                            id = Ids.pager_imageView
                            padding = dip(8)
                        }.lparams(dip(80), dip(80))

                        textView {
                            gravity = Gravity.CENTER
                            id = Ids.textViewFragmentMessage
                            //Если текст ниже селектабельный, то и этот тоже надо делать таким,
                            //иначе текст будет автоматом прокручиваться при открытии view
                            isSelectable = true
                            textSize = 20f
                            typeface = Typeface.DEFAULT_BOLD
                            padding = dip(10)

                        }.lparams(matchParent, wrapContent)
                    }.lparams(matchParent, wrapContent) {setMargins(dip(16), dip(10), dip(16), dip(10))}

                    textView {
                        id = Ids.description_text
                        isSelectable = true
                        textSize = 15f
                    }.lparams(wrapContent, wrapContent)
                }.lparams(matchParent, wrapContent) {setMargins(dip(16), dip(0), dip(16), dip(0))}
            }.lparams(matchParent, wrapContent)
        }
    }

    object Ids {
        val textViewFragmentMessage = 1
        val pager_imageView = 2
        val description_text = 3
        val description_text2 = 4
    }
}
