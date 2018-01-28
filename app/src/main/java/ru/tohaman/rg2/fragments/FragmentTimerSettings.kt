package ru.tohaman.rg2.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.ctx
import ru.tohaman.rg2.DebugTag
import ru.tohaman.rg2.ui.TimerSettingsUI


class FragmentTimerSettings : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return TimerSettingsUI<Fragment>().createView(AnkoContext.create(ctx, this))
    }

    companion object {
        fun newInstance(): FragmentTimerSettings {
            Log.v(DebugTag.TAG, "FragmentTimerSettings newInstance")
            return FragmentTimerSettings()
        }
    }

}

