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

        }

    }

//
//    private object Ids {
//        val radioGroup = 1
//        val radio_3d = 2
//        val radio_2d = 3
//    }
}
