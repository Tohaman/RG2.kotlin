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
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_pll_test_settings.view.*
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk15.coroutines.onCheckedChange
import org.jetbrains.anko.sdk15.coroutines.onClick
import org.jetbrains.anko.support.v4.ctx
import ru.tohaman.rg2.*

import ru.tohaman.rg2.activities.BlindGameActivity
import ru.tohaman.rg2.activities.PllTestGame
import ru.tohaman.rg2.activities.PllTestSelect
import ru.tohaman.rg2.ui.AnkoComponentEx
import ru.tohaman.rg2.util.saveBoolean2SP
import ru.tohaman.rg2.util.saveInt2SP

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentBlindGameSettings.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentBlindGameSettings : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return BlindGameSttingsUI<Fragment>().createView(AnkoContext.create(ctx, this))
    }



    companion object {
        fun newInstance(): FragmentBlindGameSettings {
            Log.v(DebugTag.TAG, "FragmentBlindGameSettings newInstance")
            return FragmentBlindGameSettings()
        }
    }

}


class BlindGameSttingsUI<in Fragment> : AnkoComponentEx<Fragment>() {

    override fun create(ui: AnkoContext<Fragment>): View = with(ui) {
        Log.v(DebugTag.TAG, "BlindGameSettingsUI create start")
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        var blindRowCount = sp.getInt(BLIND_ROW_COUNT, 6)

        linearLayout {
            gravity = Gravity.CENTER
            orientation = LinearLayout.VERTICAL

            include<View>(R.layout.fragment_blind_game_settings) {

            }.lparams(matchParent, matchParent) {
                leftMargin = 16.dp
                rightMargin = 16.dp
            }

            text_row_count.text = blindRowCount.toString()

            button_minus.onClick {
                blindRowCount -= 2
                if (blindRowCount < 2) {
                    blindRowCount = 2
                }
                saveInt2SP(blindRowCount, BLIND_ROW_COUNT, view.context)
                text_row_count.text = blindRowCount.toString()
            }
            button_plus.onClick {
                blindRowCount += 2
                if (blindRowCount > 8) {
                    blindRowCount = 8
                }
                saveInt2SP(blindRowCount, BLIND_ROW_COUNT, view.context)
                text_row_count.text = blindRowCount.toString()
            }

//            ch_box_corner.onCheckedChange { group, checkedId ->
//                when (checkedId) {
//                    rb_2side.id -> { is3side = false }
//                    rb_3side.id -> { is3side = true }
//                }
//                saveBoolean2SP(is3side, PLL_TEST_3SIDE,group!!.context)
//            }

            start_game_button.onClick { startActivity<BlindGameActivity>()}
        }
    }

}
