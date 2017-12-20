package ru.tohaman.rg3.ui

import android.preference.PreferenceManager
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import org.jetbrains.anko.*
import ru.tohaman.rg3.*
import ru.tohaman.rg3.DebugTag.TAG
import ru.tohaman.rg3.ankoconstraintlayout.constraintLayout


/**
 * Created by Test on 15.12.2017. Интерфейс таймера
 */
class ScrambleGenUI<in Fragment> : AnkoComponentEx<Fragment>() {

    override fun create(ui: AnkoContext<Fragment>): View = with(ui) {
        Log.v (TAG, "ScrambleGenUI create start")
        var scramble = "U2 F2 L\' D2 R U F\' L2 B2 R L2 B2 U R"
        var scrambleLength = 14
        var chkBufRebro = true
        var chkBufUgol = false
        var chkSolve = true
        val m = 16.dp
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        linearLayout {
            gravity = Gravity.CENTER
            constraintLayout {
                backgroundColorResource = R.color.blue
                val topButton = constraintLayout {
                    backgroundColorResource = R.color.red
                }.lparams(matchConstraint,wrapContent)
                val azbukaButton = button {
                    textResource = R.string.azbuka_button
                    textSize = 16F
                }.lparams(matchConstraint, wrapContent)
                val generateButton = button {
                    textResource = R.string.generate_button
                    textSize = 16F
                }.lparams(matchConstraint, wrapContent)

                constraints {
                    val layouts = arrayOf (azbukaButton,generateButton)
                    layouts.chainSpread(LEFT of topButton,RIGHT of topButton)

                    topButton.connect(HORIZONTAL of parentId,
                            TOPS of parentId,
                            BOTTOMS of generateButton)
                    azbukaButton.connect(TOPS of parentId,
                            BOTTOMS of parentId,
                            RIGHTS of parentId,
                            LEFTS of generateButton)
                    generateButton.connect(TOPS of parentId,
                            BOTTOMS of parentId,
                            LEFT to RIGHT of azbukaButton,
                            RIGHTS of parentId)
                }
            }.lparams(matchParent, matchParent)
        }
    }

}
