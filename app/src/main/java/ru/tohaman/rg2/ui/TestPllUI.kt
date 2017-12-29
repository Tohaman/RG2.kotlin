package ru.tohaman.rg2.ui

import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import org.jetbrains.anko.*
import ru.tohaman.rg2.DebugTag.TAG
import ru.tohaman.rg2.data.ListPagerLab


/**
 * Created by Test on 27.12.2017. Миниигра угадай PLL
 */
class TestPllUI<in Fragment> : AnkoComponentEx<Fragment>() {

    override fun create(ui: AnkoContext<Fragment>): View = with(ui) {
        Log.v(TAG, "TimerSettingsUI create start")
        val listPagerLab = ListPagerLab.get(ctx)
        val m = 16.dp

        linearLayout {
            gravity = Gravity.CENTER
            orientation = LinearLayout.VERTICAL

            val ptTextView = textView {
                text = "Сложность:"
                textSize = 24F
            }.lparams() { margin = 8.dp }
            radioGroup  {
                backgroundColor = 0x333333.opaque
                id = Ids.radioGroup
                orientation = LinearLayout.VERTICAL
                radioButton {
                    text = "по двум сторонам"
                    textSize = 18F
                    id = Ids.radio_2d
                }.lparams(wrapContent, wrapContent) {horizontalMargin = m}
                radioButton {
                    text = "по трем сторонам"
                    textSize = 18F
                    id = Ids.radio_3d
                    isChecked = true
                }.lparams(wrapContent, wrapContent) {horizontalMargin = m}
            }.lparams(wrapContent, wrapContent)

            val rowCountText = textView {
                text = "Количество вариантов ответа"
                textSize = 24F
            }.lparams() { margin = 16.dp }
            val rowCountLinLay = linearLayout {
                gravity = Gravity.CENTER
                orientation = LinearLayout.HORIZONTAL

                val rowCountButtonMinus = button("-")
                val countRowCount = textView {
                    text = "6"
                    textSize = 24F
                }.lparams() {
                    horizontalMargin = m
                }
                val rowCountButtonPlus = button("+")
            }.lparams(matchParent, wrapContent) { margin = 8.dp }

        }
    }


    private object Ids {
        val radioGroup = 1
        val radio_3d = 2
        val radio_2d = 5
    }
}
