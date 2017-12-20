package ru.tohaman.rg3.ui

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
class TimerSettingsUI<Fragment> : AnkoComponentEx<Fragment>() {

    override fun create(ui: AnkoContext<Fragment>): View = with(ui) {
        Log.v (TAG, "TimerSettingsUI create start")
        val m = 16.dp
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        val oneHandToStart = sp.getBoolean(ONE_HAND_TO_START, false)
        val metronomEnabled = sp.getBoolean(METRONOM_ENABLED, true)
        var metronomTime = sp.getInt(METRONOM_TIME, 80)

        linearLayout {
            gravity = Gravity.CENTER
            constraintLayout {
                val oneHandCheckBox = checkBox {
                    text = "Управление таймером одной рукой"
                    textSize = 16F
                    isChecked = oneHandToStart
                }.lparams(wrapContent,wrapContent)
                val metronom = checkBox {
                    text = "Метроном"
                    textSize = 24F
                    isChecked = metronomEnabled
                }
                val metronomText = textView {
                    text = "Частота метронома (тактов в минуту)"
                }
                val linLayout = linearLayout {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER
                    val buttonMinus = button ("-")
                    val textHz = textView {
                        text = metronomTime.toString()
                        textSize = 24F
                    }.lparams {margin = m}
                    val buttonPlus = button ("+")

                    buttonMinus.onClick {
                        metronomTime--
                        if (metronomTime < 1) {metronomTime = 1}
                        saveInt2SP(metronomTime,METRONOM_TIME)
                        textHz.text = metronomTime.toString()
                    }
                    buttonPlus.onClick {
                        metronomTime++
                        if (metronomTime > 240) {metronomTime = 240}
                        saveInt2SP(metronomTime,METRONOM_TIME)
                        textHz.text = metronomTime.toString()
                    }
                }.lparams(0,wrapContent)

                val startButton = button {
                    text = "Запустить таймер"
                    backgroundColorResource = R.color.colorAccent
                    textSize = 30F
                    padding = 20.dp
                }.lparams(0,wrapContent)

                startButton.onClick { startActivity<TimerActivity>() }

                oneHandCheckBox.setOnCheckedChangeListener { _, isChecked ->
                    saveBoolean2SP(isChecked, ONE_HAND_TO_START)
                }

                metronom.setOnCheckedChangeListener { _, isChecked ->
                    saveBoolean2SP(isChecked, METRONOM_ENABLED)
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

    fun saveBoolean2SP(bool : Boolean, st : String) {
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = sp.edit()
        editor.putBoolean(st, bool)
        editor.apply() // подтверждаем изменения
    }

    fun saveInt2SP(int : Int, st : String) {
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = sp.edit()
        editor.putInt(st, int)
        editor.apply() // подтверждаем изменения
    }

}
