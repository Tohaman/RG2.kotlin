package ru.tohaman.rg2.fragments


import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.fragment_pll_test_settings.view.*
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk15.coroutines.onCheckedChange
import org.jetbrains.anko.sdk15.coroutines.onClick
import org.jetbrains.anko.support.v4.ctx
import ru.tohaman.rg2.DebugTag
import ru.tohaman.rg2.PLL_TEST_3SIDE
import ru.tohaman.rg2.PLL_TEST_ROW_COUNT
import ru.tohaman.rg2.R
import ru.tohaman.rg2.activities.PllTestGame
import ru.tohaman.rg2.activities.PllTestSelectPllName
import ru.tohaman.rg2.ui.AnkoComponentEx
import ru.tohaman.rg2.util.saveBoolean2SP
import ru.tohaman.rg2.util.saveInt2SP

/**
 * Фрагмент с выбором настроек для игры Угадай PLL, UI создается в [onCreateView]
 * в этом UI вся логики работы фрагмента
 * фабричный метод [newInstance] для создания фрагмента
 */

class FragmentTestPLLSettings : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return TestPllUI<Fragment>().createView(AnkoContext.create(ctx, this))
    }

    companion object {
        fun newInstance(): FragmentTestPLLSettings {
            Log.v(DebugTag.TAG, "FragmentTestPLLSettings newInstance")
            return FragmentTestPLLSettings()
        }
    }


}

class TestPllUI<in Fragment> : AnkoComponentEx<Fragment>() {

    override fun create(ui: AnkoContext<Fragment>): View = with(ui) {
        Log.v(DebugTag.TAG, "TimerSettingsUI create start")
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

            ch_box_layout.onCheckedChange { group, checkedId ->
                when (checkedId) {
                    rb_2side.id -> { is3side = false }
                    rb_3side.id -> { is3side = true }
                }
                saveBoolean2SP(is3side, PLL_TEST_3SIDE,group!!.context)
            }

            start_game_button.onClick { startActivity<PllTestGame>()}

            button_rename.onClick { startActivity<PllTestSelectPllName>() }
        }
    }

}


