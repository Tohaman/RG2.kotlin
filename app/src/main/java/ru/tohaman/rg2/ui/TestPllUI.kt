package ru.tohaman.rg2.ui

import android.preference.PreferenceManager
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk15.coroutines.onClick
import ru.tohaman.rg2.DebugTag.TAG
import ru.tohaman.rg2.R
import ru.tohaman.rg2.data.ListPagerLab


/**
 * Created by Test on 27.12.2017. Миниигра угадай PLL
 */
class TestPllUI<in Fragment> : AnkoComponentEx<Fragment>() {
    val PLL_TEST_ROW_COUNT = "pllTestRowCount"

    override fun create(ui: AnkoContext<Fragment>): View = with(ui) {
        Log.v(TAG, "TimerSettingsUI create start")
        val listPagerLab = ListPagerLab.get(ctx)
        val m = 16.dp
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        var rowCount = sp.getInt(PLL_TEST_ROW_COUNT, 6)

        linearLayout {
            gravity = Gravity.CENTER
            orientation = LinearLayout.VERTICAL

            val ptTextView = textView {
                text = "Определи PLL:"
                textSize = 20F
            }.lparams() { margin = 8.dp }
            radioGroup  {
                backgroundColor = 0x333333.opaque
//                id = Ids.radioGroup
                orientation = LinearLayout.VERTICAL
                radioButton {
                    text = "по двум сторонам"
                    textSize = 18F
//                    id = Ids.radio_2d
                }.lparams(wrapContent, wrapContent) {horizontalMargin = m}
                radioButton {
                    text = "по трем сторонам"
                    textSize = 18F
//                    id = Ids.radio_3d
                    isChecked = true
                }.lparams(wrapContent, wrapContent) {horizontalMargin = m}
            }.lparams(wrapContent, wrapContent)

            val startButton = styledButton(R.style.Widget_AppCompat_Button_Colored) {
                text = "Начать игру"
                textSize = 16F
                padding = 20.dp
            }.lparams (matchParent,wrapContent) {
                horizontalMargin = m
                topMargin = 32.dp
            }

            textView {
                text = "Количество вариантов ответа"
                textSize = 18F
            }.lparams(wrapContent, wrapContent) {
                horizontalMargin = m
                topMargin = 32.dp
            }

            val rowCountLinLay = linearLayout {
                gravity = Gravity.CENTER
                orientation = LinearLayout.HORIZONTAL

                val rowCountButtonMinus = button("-")

                val countRowCount = textView {
                    text = rowCount.toString()
                    textSize = 24F
                }.lparams() {
                    horizontalMargin = m
                }
                val rowCountButtonPlus = button("+")

                rowCountButtonMinus.onClick {
                    rowCount -= 2
                    if (rowCount < 2) { rowCount = 2 }
                    countRowCount.text = rowCount.toString()
                }
                rowCountButtonPlus.onClick {
                    rowCount += 2
                    if (rowCount > 8) { rowCount = 8 }
                    countRowCount.text = rowCount.toString()
                }
            }.lparams(matchParent, wrapContent) { margin = 8.dp }

            val changePllNameButton = styledButton(R.style.Widget_AppCompat_Button) {
                text = "Задать свои названия для этапов"
                textSize = 16F
                padding = 20.dp
                allCaps = false
            }.lparams (matchParent,wrapContent) {horizontalMargin = m}

        }

    }

//
//    private object Ids {
//        val radioGroup = 1
//        val radio_3d = 2
//        val radio_2d = 3
//    }
}
