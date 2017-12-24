package ru.tohaman.rg3.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.jetbrains.anko.*
import ru.tohaman.rg3.DebugTag
import ru.tohaman.rg3.ui.AzbukaSelectUI
import ru.tohaman.rg3.ui.TimerUI


class FragmentAzbukaSelect : Fragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return AzbukaSelectUI<Fragment>().createView(AnkoContext.create(context, this))
    }

    companion object {
        fun newInstance(): FragmentAzbukaSelect {
            Log.v(DebugTag.TAG, "FragmentAzbukaSelect newInstance")
            return FragmentAzbukaSelect()
        }
    }


}

