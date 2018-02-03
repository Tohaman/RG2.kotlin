package ru.tohaman.rg2.fragments

import android.content.Context
import android.net.Uri
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
import ru.tohaman.rg2.DebugTag
import ru.tohaman.rg2.PLL_TEST_3SIDE
import ru.tohaman.rg2.PLL_TEST_ROW_COUNT

import ru.tohaman.rg2.R
import ru.tohaman.rg2.activities.BlindGameActivity
import ru.tohaman.rg2.activities.PllTestGame
import ru.tohaman.rg2.activities.PllTestSelect
import ru.tohaman.rg2.ui.AnkoComponentEx
import ru.tohaman.rg2.util.saveBoolean2SP
import ru.tohaman.rg2.util.saveInt2SP

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [FragmentBlindGameSettings.OnFragmentInteractionListener] interface
 * to handle interaction events.
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
//        var rowCount = sp.getInt(PLL_TEST_ROW_COUNT, 6)
//        var is3side = sp.getBoolean(PLL_TEST_3SIDE, true)

        linearLayout {
            gravity = Gravity.CENTER
            orientation = LinearLayout.VERTICAL

            include<View>(R.layout.fragment_blind_game_settings) {

            }.lparams(matchParent, matchParent) {
                leftMargin = 16.dp
                rightMargin = 16.dp
            }

            start_game_button.onClick { startActivity<BlindGameActivity>()}
        }
    }

}
