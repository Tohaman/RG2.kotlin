package ru.tohaman.rg2.ui

import android.preference.PreferenceManager
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.fragment_pll_test_settings.view.*
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk15.coroutines.onCheckedChange
import org.jetbrains.anko.sdk15.coroutines.onClick
import ru.tohaman.rg2.DebugTag.TAG
import ru.tohaman.rg2.PLL_TEST_3SIDE
import ru.tohaman.rg2.PLL_TEST_ROW_COUNT
import ru.tohaman.rg2.R
import ru.tohaman.rg2.activitys.PllTestGame
import ru.tohaman.rg2.util.saveBoolean2SP
import ru.tohaman.rg2.util.saveInt2SP

/**
 * Created by Test on 27.12.2017. Настройки миниигры угадай PLL
 */

class TestPllUI<in Fragment> : AnkoComponentEx<Fragment>() {

    override fun create(ui: AnkoContext<Fragment>): View = with(ui) {
        Log.v(TAG, "TimerSettingsUI create start")
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        var rowCount = sp.getInt(PLL_TEST_ROW_COUNT, 6)
        var is3side = sp.getBoolean(PLL_TEST_3SIDE, true)

        linearLayout {
            gravity = Gravity.CENTER
            orientation = LinearLayout.VERTICAL

            include<View>(R.layout.fragment_pll_test_settings) {
            }.lparams(matchParent, matchParent) {
                leftMargin = 16.dp
                rightMargin = 16.dp
            }

            text_row_count.text = rowCount.toString()

            button_minus.onClick {
                rowCount -= 2
                if (rowCount < 2) {
                    rowCount = 2
                }
                saveInt2SP(rowCount, PLL_TEST_ROW_COUNT, view.context)
                text_row_count.text = rowCount.toString()
            }
            button_plus.onClick {
                rowCount += 2
                if (rowCount > 8) {
                    rowCount = 8
                }
                saveInt2SP(rowCount, PLL_TEST_ROW_COUNT, view.context)
                text_row_count.text = rowCount.toString()
            }

            radio_group_difficulty.onCheckedChange { group, checkedId ->
                when (checkedId) {
                    rb_2side.id -> { is3side = false }
                    rb_3side.id -> { is3side = true }
                }
                saveBoolean2SP(is3side,PLL_TEST_3SIDE,group!!.context)
            }

            start_game_button.onClick { startActivity<PllTestGame>()}
        }
    }

//    private object Ids {
//        val radioGroup = 1
//        val radio_3d = 2
//        val radio_2d = 3
//    }
}
