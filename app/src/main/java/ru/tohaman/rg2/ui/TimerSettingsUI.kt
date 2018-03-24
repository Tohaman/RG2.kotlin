package ru.tohaman.rg2.ui

import android.preference.PreferenceManager
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.button_colored.view.*
import kotlinx.android.synthetic.main.check_box.view.*
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk15.coroutines.onClick
import ru.tohaman.rg2.*
import ru.tohaman.rg2.DebugTag.TAG
import ru.tohaman.rg2.activities.TimerActivity
import ru.tohaman.rg2.ankoconstraintlayout.constraintLayout
import ru.tohaman.rg2.util.saveBoolean2SP
import ru.tohaman.rg2.util.saveInt2SP


/**
 * Created by Test on 15.12.2017. Интерфейс таймера
 */
class TimerSettingsUI<in Fragment> : AnkoComponentEx<Fragment>() {

    override fun create(ui: AnkoContext<Fragment>): View = with(ui) {
        Log.v (TAG, "TimerSettingsUI create start")
        val m = 16.dp
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        val oneHandToStart = sp.getBoolean(ONE_HAND_TO_START, false)
        val metronomEnabled = sp.getBoolean(METRONOM_ENABLED, true)
        var metronomTime = sp.getInt(METRONOM_TIME, 60)
        var delayMills = sp.getInt(DELAY_MILLS, 500)
        val isDelayed = if (delayMills == 0) { false } else { true }
        val isScrambleVisible = sp.getBoolean(IS_SCRAMBLE_VISIBLE, true)


        linearLayout {
            gravity = Gravity.CENTER
            constraintLayout {

                val delay500CheckBox = include<CheckBox>(R.layout.check_box) {
                    text = "Удерживать 0.5 сек для старта"
                    textSize = 16F
                    isChecked = isDelayed
                }

                val oneHandCheckBox = include<CheckBox>(R.layout.check_box) {
                    text = "Управление таймером одной рукой"
                    textSize = 16F
                    isChecked = oneHandToStart
                }

                val scrambleCheckBox = include<CheckBox>(R.layout.check_box) {
                    text = "Генерировать скрамбл"
                    textSize = 16F
                    isChecked = isScrambleVisible
                }

                val metronom = include<CheckBox>(R.layout.check_box) {
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

                    buttonMinus.onClick { view ->
                        metronomTime--
                        if (metronomTime < 1) {metronomTime = 1}
                        saveInt2SP(metronomTime, METRONOM_TIME, view!!.context)
                        textHz.text = metronomTime.toString()
                    }
                    buttonPlus.onClick { view ->
                        metronomTime++
                        if (metronomTime > 240) {metronomTime = 240}
                        saveInt2SP(metronomTime, METRONOM_TIME, view!!.context)
                        textHz.text = metronomTime.toString()
                    }

                    //TODO Сделать обработчик долгого нажатия на кнопку, что-то на базе этого
//                    buttonPlus.setOnTouchListener(object : View.OnTouchListener {
//
//                        internal var startTime: Long = 0
//
//                        override fun onTouch(v: View, event: MotionEvent): Boolean {
//                            when (event.action) {
//                                MotionEvent.ACTION_DOWN // нажатие
//                                -> startTime = System.currentTimeInMills()
//                                MotionEvent.ACTION_MOVE // движение
//                                -> {
//                                }
//                                MotionEvent.ACTION_UP // отпускание
//                                    , MotionEvent.ACTION_CANCEL -> {
//                                    val totalTime = System.currentTimeInMills() - startTime
//                                    val totalSecunds = totalTime / 1000
//                                    if (totalSecunds >= 3) {
//                                        //ВОТ тут прошло 3 или больше секунды с начала нажатия
//                                        //можно что-то запустить
//                                        println("Три секунды прошло с нажатия!")
//                                    }
//                                }
//                            }
//                            return true
//                        }
//                    })
//
                }.lparams(0,wrapContent)



                include<Button>(R.layout.button_colored) {
                    text = "Запустить таймер"
                    textSize = 16F
                    padding = 20.dp
                }
                val startButton = btn_colored


//              //TODO Раскоментировать когда `themed-` is fixed.
//                val startButton = themedButton(theme = R.style.Widget_AppCompat_Button_Borderless_Colored) {
//                    text = "Запустить таймер"
//                    textSize = 16F
//                    padding = 20.dp
//                }.lparams (0,wrapContent)

                startButton.onClick { startActivity<TimerActivity>() }

                delay500CheckBox.setOnCheckedChangeListener { _, isChecked ->
                    delayMills = if (isChecked) {500} else {0}
                    saveInt2SP(delayMills, DELAY_MILLS, context)
                }
                oneHandCheckBox.setOnCheckedChangeListener { _, isChecked ->
                    saveBoolean2SP(isChecked, ONE_HAND_TO_START, context)
                }

                metronom.setOnCheckedChangeListener { _, isChecked ->
                    saveBoolean2SP(isChecked, METRONOM_ENABLED, context)
                }

                constraints {
                    val layouts = arrayOf (delay500CheckBox, oneHandCheckBox,startButton,metronom,metronomText,linLayout,scrambleCheckBox)
                    layouts.chainSpread(TOP of parentId,BOTTOM of parentId)

                    delay500CheckBox.connect(HORIZONTAL of parentId,
                            TOPS of parentId,
                            BOTTOM to TOP of oneHandCheckBox)
                    oneHandCheckBox.connect(HORIZONTAL of parentId,
                            TOP to BOTTOM of delay500CheckBox,
                            BOTTOM to TOP of startButton with (10.dp))
                    startButton.connect(HORIZONTAL of parentId,
                            TOP to BOTTOM of oneHandCheckBox with (10.dp),
                            BOTTOM to TOP of metronom)
                    metronom.connect(HORIZONTAL of parentId,
                            TOP to BOTTOM of startButton)
                    metronomText.connect(HORIZONTAL of parentId,
                            TOP to BOTTOM of metronom,
                            BOTTOM to TOP of linLayout)
                    linLayout.connect(HORIZONTAL of parentId,
                            TOP to BOTTOM of metronomText,
                            BOTTOM to TOP of scrambleCheckBox)
                    scrambleCheckBox.connect(HORIZONTAL of parentId,
                            TOP to BOTTOM of linLayout,
                            BOTTOMS of parentId)
                }
            }.lparams(matchParent, wrapContent) {margin = m}
        }
    }

}
