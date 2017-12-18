package ru.tohaman.rg3.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.jetbrains.anko.*
import ru.tohaman.rg3.ui.TimerSettingsUI


class FragmentTimerSettings : Fragment() {


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return TimerSettingsUI<Fragment>().createView(AnkoContext.create(context, this))
    }

}

