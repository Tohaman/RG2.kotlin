package ru.tohaman.rg3.ui

import android.os.Build
import android.preference.PreferenceManager
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk15.coroutines.onClick
import ru.tohaman.rg3.*
import ru.tohaman.rg3.DebugTag.TAG
import ru.tohaman.rg3.activitys.TimerActivity
import ru.tohaman.rg3.ankoconstraintlayout.constraintLayout


/**
 * Created by Test on 15.12.2017. Интерфейс таймера
 */
class ScrambleGenUI<Fragment> : AnkoComponentEx<Fragment>() {

    override fun create(ui: AnkoContext<Fragment>): View = with(ui) {
        Log.v (TAG, "ScrambleGenUI create start")
        val m = 16.dp
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        linearLayout {
            gravity = Gravity.CENTER
            constraintLayout {

                constraints {

                }
            }.lparams(matchParent, wrapContent) {margin = m}
        }
    }

}
