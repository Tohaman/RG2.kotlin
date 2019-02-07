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
import ru.tohaman.rg2.*
import ru.tohaman.rg2.activities.OllTestGame
import ru.tohaman.rg2.activities.PllTestGame
import ru.tohaman.rg2.activities.TestGameSelectOllName
import ru.tohaman.rg2.activities.TestGameSelectPllName
import ru.tohaman.rg2.ui.AnkoComponentEx
import ru.tohaman.rg2.util.saveBoolean2SP
import ru.tohaman.rg2.util.saveInt2SP

/**
 * Фрагмент с выбором настроек для игры Угадай PLL, UI создается в [onCreateView]
 * в этом UI вся логики работы фрагмента
 * фабричный метод [newInstance] для создания фрагмента
 */

class FragmentTestGameSettings : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return TestGameUI<Fragment>().createView(AnkoContext.create(requireContext(), this))
    }

    companion object {
        fun newInstance(): FragmentTestGameSettings {
            Log.v(DebugTag.TAG, "FragmentTestGameSettings newInstance")
            return FragmentTestGameSettings()
        }
    }


}

class TestGameUI<in Fragment> : AnkoComponentEx<Fragment>() {
    private var rowCount = 6
    private var is3side = true
    private var isOllGame = false

    override fun create(ui: AnkoContext<Fragment>): View = with(ui) {
        Log.v(DebugTag.TAG, "TimerSettingsUI create start")
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        rowCount = sp.getInt(TEST_GAME_ROW_COUNT, 6)
        is3side = sp.getBoolean(PLL_TEST_3SIDE, true)
        isOllGame = sp.getBoolean(OLL_TEST_GAME, false)

        linearLayout {
            gravity = Gravity.CENTER
            orientation = LinearLayout.VERTICAL

            include<View>(R.layout.fragment_pll_test_settings) {
            }.lparams(matchParent, matchParent) {
                leftMargin = 16.dp
                rightMargin = 16.dp
            }

            ch_box_layout.clearCheck()

            if (isOllGame) {
                   rb_oll_game.isChecked = true
            } else {
                if (is3side) {
                    rb_3side.isChecked = true
                } else {
                    rb_2side.isChecked = true
                }
            }

            text_row_count.text = rowCount.toString()

            button_minus.onClick {
                rowCount -= 2
                if (rowCount < 2) {
                    rowCount = 2
                }
                saveInt2SP(rowCount, TEST_GAME_ROW_COUNT, view.context)
                text_row_count.text = rowCount.toString()
            }
            button_plus.onClick {
                rowCount += 2
                if (rowCount > 8) {
                    rowCount = 8
                }
                saveInt2SP(rowCount, TEST_GAME_ROW_COUNT, view.context)
                text_row_count.text = rowCount.toString()
            }

            ch_box_layout.onCheckedChange { group, checkedId ->
                when (checkedId) {
                    rb_2side.id ->      { is3side = false; isOllGame = false }
                    rb_3side.id ->      { is3side = true; isOllGame = false }
                    rb_oll_game.id ->   { is3side = false; isOllGame = true }
                }
                saveBoolean2SP(is3side, PLL_TEST_3SIDE, group!!.context)
                saveBoolean2SP(isOllGame, OLL_TEST_GAME, group.context)
            }

            start_game_button.onClick {
                if (isOllGame) {
                    startActivity<OllTestGame>()
                } else {
                    startActivity<PllTestGame>()
                }
            }

            button_rename.onClick {
                if (isOllGame) {
                    startActivity<TestGameSelectOllName>()
                } else {
                    startActivity<TestGameSelectPllName>()
                }
            }
        }
    }

}


