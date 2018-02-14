package ru.tohaman.rg2.fragments

import android.content.Context
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.fragment_blind_game_settings.view.*
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk15.coroutines.onClick
import org.jetbrains.anko.sdk25.coroutines.onCheckedChange
import org.jetbrains.anko.support.v4.ctx
import ru.tohaman.rg2.*
import ru.tohaman.rg2.activities.BlindGameActivity

import ru.tohaman.rg2.ui.AnkoComponentEx
import ru.tohaman.rg2.util.saveBoolean2SP
import ru.tohaman.rg2.util.saveInt2SP

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentBlindGameSettings.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentBlindGameSettings : Fragment() {
    private var mListener: OnBlindGameInteractionListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = BlindGameSettingsUI<Fragment>().createView(AnkoContext.create(ctx, this))
        val azbukaButton = view.findViewById<Button>(R.id.azbuka_select_button)
        azbukaButton.onClick {
            Log.v (DebugTag.TAG, "AzbukaButton Click")
            if (mListener != null) {
                mListener!!.onBlindGameInteraction("AZBUKA2")
            }
        }
        return view
    }

    override fun onAttach(context: Context?) {
        Log.v (DebugTag.TAG, "FragmentBlindGameSettings onAttach")
        super.onAttach(context)
        if (context is OnBlindGameInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnBlindGameInteractionListener")
        }
    }

    override fun onDetach() {
        Log.v (DebugTag.TAG, "FragmentBlindGameSettings onDetach")
        super.onDetach()
        mListener = null
    }

    interface OnBlindGameInteractionListener {
        fun onBlindGameInteraction(button: String) {
            Log.v(DebugTag.TAG, "FragmentBlindGameSettings onListViewInteraction")
        }
    }

    companion object {
        fun newInstance(): FragmentBlindGameSettings {
            Log.v(DebugTag.TAG, "FragmentBlindGameSettings newInstance")
            return FragmentBlindGameSettings()
        }
    }

}


class BlindGameSettingsUI<in Fragment> : AnkoComponentEx<Fragment>() {

    override fun create(ui: AnkoContext<Fragment>): View = with(ui) {
        Log.v(DebugTag.TAG, "BlindGameSettingsUI create start")
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        var blindRowCount = sp.getInt(BLIND_ROW_COUNT, 6)
        var isCornerChecked = sp.getBoolean(BLIND_IS_CORNER_CHECKED, true)
        var isEdgeChecked = sp.getBoolean(BLIND_IS_EDGE_CHECKED, true)

        linearLayout {
            gravity = Gravity.CENTER
            orientation = LinearLayout.VERTICAL

            include<View>(R.layout.fragment_blind_game_settings) {

            }.lparams(matchParent, matchParent) {
                leftMargin = 16.dp
                rightMargin = 16.dp
            }

            text_blind_row_count.text = blindRowCount.toString()
            ch_box_edge.isChecked = isEdgeChecked
            ch_box_corner.isChecked = isCornerChecked

            button_blind_minus.onClick {
                blindRowCount -= 2
                if (blindRowCount < 2) {
                    blindRowCount = 2
                }
                saveInt2SP(blindRowCount, BLIND_ROW_COUNT, view.context)
                text_blind_row_count.text = blindRowCount.toString()
            }
            button_blind_plus.onClick {
                blindRowCount += 2
                if (blindRowCount > 8) {
                    blindRowCount = 8
                }
                saveInt2SP(blindRowCount, BLIND_ROW_COUNT, view.context)
                text_blind_row_count.text = blindRowCount.toString()
            }

            ch_box_edge.onCheckedChange { _, isChecked ->
                isEdgeChecked = isChecked
                if (!isEdgeChecked and !isCornerChecked) {
                    isCornerChecked = true
                    ch_box_corner.isChecked = isCornerChecked
                }
                saveBoolean2SP(isEdgeChecked, BLIND_IS_EDGE_CHECKED, ctx)
                saveBoolean2SP(isCornerChecked, BLIND_IS_CORNER_CHECKED, ctx)
            }

            ch_box_corner.onCheckedChange { _, isChecked ->
                isCornerChecked = isChecked
                if (!isEdgeChecked and !isCornerChecked) {
                    isEdgeChecked = true
                    ch_box_edge.isChecked = isEdgeChecked
                }
                saveBoolean2SP(isEdgeChecked, BLIND_IS_EDGE_CHECKED, ctx)
                saveBoolean2SP(isCornerChecked, BLIND_IS_CORNER_CHECKED, ctx)
            }

            start_blind_game_button.onClick { startActivity<BlindGameActivity>()}

        }
    }

}
