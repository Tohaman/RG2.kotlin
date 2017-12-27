package ru.tohaman.rg2.ui

import android.util.Log
import android.view.View
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
            val ptTextView = textView {
                text = "МиниИгра угадай PLL настройки"
            }

        }
    }

}
