package ru.tohaman.rg3.ui

import android.graphics.Paint
import android.os.Build
import android.preference.PreferenceManager
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk15.coroutines.onClick
import org.jetbrains.anko.sdk25.coroutines.onCheckedChange
import ru.tohaman.rg3.AnkoComponentEx
import ru.tohaman.rg3.DebugTag.TAG
import ru.tohaman.rg3.R
import ru.tohaman.rg3.activitys.TimerActivity
import ru.tohaman.rg3.ankoconstraintlayout.constraintLayout


/**
 * Created by Test on 15.12.2017. Интерфейс таймера
 */
class TimerSettingsUI<Fragment> : AnkoComponentEx<Fragment>() {
    var oneHandTimer : Boolean = false

    override fun create(ui: AnkoContext<Fragment>): View = with(ui) {
        Log.v (TAG, "TimerSettingsUI create start")
        val m = 16.dp
        oneHandTimer = loadTimerOneHandSettings()
        linearLayout {
            gravity = Gravity.CENTER
            constraintLayout {
                val oneHandCheckBox = checkBox {
                    text = "Управление таймером одной рукой"
                    textSize = 16F
                    isChecked = oneHandTimer
                }.lparams(wrapContent,wrapContent)
                val metronom = checkBox {
                    text = "Метроном"
                    textSize = 24F
                }
                val metronomText = textView {
                    text = "Частота метронома (тактов в минуту)"
                }
                val linLayout = linearLayout {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER
                    val buttonMinus = button ("-")
                    val textHz = textView {
                        text = "80"
                        textSize = 24F
                    }.lparams() {margin = m}
                    val buttonPlus = button ("+")
                }.lparams(0,wrapContent)

                val startButton = button {
                    text = "Запустить таймер"
                    backgroundColor = getColorFromResources(R.color.colorAccent)
                    textSize = 30F
                    padding = 20.dp
                }.lparams(0,wrapContent)
                startButton.onClick { startActivity<TimerActivity>() }
                oneHandCheckBox.setOnCheckedChangeListener { _, isChecked ->
                    saveTimerOneHandSettings(isChecked)
                }

                constraints {
                    val layouts = arrayOf (oneHandCheckBox,metronom,metronomText,linLayout,startButton)
                    layouts.chainSpread(TOP of parentId,BOTTOM of parentId)
                    oneHandCheckBox.connect(HORIZONTAL of parentId,
                            TOPS of parentId,
                            BOTTOM to TOP of startButton with (30.dp))
                    startButton.connect(HORIZONTAL of parentId,
                            TOP to BOTTOM of oneHandCheckBox with (30.dp),
                            BOTTOM to TOP of metronom)
                    metronom.connect(HORIZONTAL of parentId,
                            TOP to BOTTOM of startButton)
                    metronomText.connect(HORIZONTAL of parentId,
                            TOP to BOTTOM of metronom,
                            BOTTOM to TOP of linLayout)
                    linLayout.connect(HORIZONTAL of parentId,
                            TOP to BOTTOM of metronomText,
                            BOTTOMS of parentId)

                }
            }.lparams(matchParent, wrapContent) {margin = m}
        }
    }

    fun saveTimerOneHandSettings(bool : Boolean) {
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = sp.edit()
        editor.putBoolean("oneHandTimer", bool)
        editor.apply() // подтверждаем изменения
    }

    fun loadTimerOneHandSettings():Boolean {
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        return sp.getBoolean("oneHandTimer", false)
    }


    private fun getColorFromResources(colorRes:Int):Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context.resources.getColor(colorRes,null)
        } else {
            @Suppress("DEPRECATION")
            context.resources.getColor(colorRes)
        }
    }

}
