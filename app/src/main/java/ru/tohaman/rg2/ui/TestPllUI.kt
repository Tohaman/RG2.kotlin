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

        linearLayout {
            gravity = Gravity.CENTER
            orientation = LinearLayout.VERTICAL
            val rowCountText =  textView {
                text = "Количество вариантов ответа"
                textSize = 20F
            }.lparams() {margin = 16.dp}
            val rowCountLinLay = linearLayout {
                gravity = Gravity.CENTER
                orientation = LinearLayout.HORIZONTAL

                val rowCountButtonMinus = button("-")
                val countRowCount = textView("4")
                val rowCountButtonPlus = button("+")
            }.lparams(matchParent, wrapContent) {margin = 8.dp}
            val ptTextView = textView {
                text = "МиниИгра угадай PLL настройки"
            }.lparams() {margin = 16.dp}

        }
    }

}
