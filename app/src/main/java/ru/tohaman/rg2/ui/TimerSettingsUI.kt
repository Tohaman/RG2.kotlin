package ru.tohaman.rg2.ui

import android.preference.PreferenceManager
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.button_colored.view.*
import kotlinx.android.synthetic.main.check_box.view.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk15.coroutines.onClick
import org.jetbrains.anko.sdk15.coroutines.onTouch
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
        var touchTime: Long


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

                val linLayout = linearLayout {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER

                    constraintLayout {

                        val metronom = include<CheckBox>(R.layout.check_box) {
                            text = "Метроном"
                            textSize = 16F

                            isChecked = metronomEnabled
                        }.lparams { rightMargin = 20.dp }

                        val buttonMinus = button("-") {
                        }.lparams(50.dp)

                        val textHz = textView {
                            text = metronomTime.toString()
                            gravity = Gravity.CENTER
                            textSize = 20F
                        }

                        val buttonPlus = button("+") {}.lparams(50.dp)

                        buttonPlus.onTouch { v, event ->
                            val action = event.action
                            when (action) {
                                MotionEvent.ACTION_DOWN -> {
                                    touchTime = System.currentTimeMillis()
                                    launch(UI) {
                                        do {
                                            metronomTime = metronomTimePlusOne(metronomTime, v, textHz)
                                            delay(150)
                                        } while (touchTime != 0L)
                                    }
                                }
                                MotionEvent.ACTION_UP -> {
                                    touchTime = 0
                                }
                            }
                        }

                        buttonMinus.onTouch { v, event ->
                            val action = event.action
                            when (action) {
                                MotionEvent.ACTION_DOWN -> {
                                    touchTime = System.currentTimeMillis()
                                    launch(UI) {
                                        do {
                                            metronomTime = metronomTimeMinusOne(metronomTime, v, textHz)
                                            delay(150)
                                        } while (touchTime != 0L)
                                    }
                                }
                                MotionEvent.ACTION_UP -> {
                                    touchTime = 0
                                }
                            }
                        }


                        val metronomText = textView {
                            text = "(тактов в минуту)"
                        }

//                        buttonMinus.onClick { view ->
//                            metronomTime = metronomTimeMinusOne(metronomTime, view, textHz)
//                        }
//                        buttonPlus.onClick { view ->
//                            metronomTime = metronomTimePlusOne(metronomTime, view, textHz)
//                        }

                        //TODO Сделать обработчик долгого нажатия на кнопку, что-то на базе этого

                        metronom.setOnCheckedChangeListener { _, isChecked ->
                            saveBoolean2SP(isChecked, METRONOM_ENABLED, context)
                        }

                        constraints {
                            buttonPlus.connect( RIGHTS of parentId,
                                    TOPS of parentId,
                                    BOTTOM to TOP of metronomText)
                            textHz.connect(RIGHT to LEFT of buttonPlus,
                                    TOPS of parentId,
                                    BOTTOM to TOP of metronomText)
                            buttonMinus.connect( RIGHT to LEFT of textHz,
                                    TOPS of parentId,
                                    BOTTOM to TOP of metronomText)
                            metronom.connect(TOPS of parentId,
                                    BOTTOMS of parentId,
                                    LEFTS of parentId,
                                    RIGHT to LEFT of buttonMinus)
                            metronomText.connect( RIGHTS of buttonPlus,
                                    LEFTS of buttonMinus,
                                    BOTTOMS of parentId)
                        }

                    }
                }.lparams(wrapContent,wrapContent)

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
                    saveInt2SP(delayMills, DELAY_MILLS, ctx)
                }
                oneHandCheckBox.setOnCheckedChangeListener { _, isChecked ->
                    saveBoolean2SP(isChecked, ONE_HAND_TO_START, ctx)
                }

                scrambleCheckBox.setOnCheckedChangeListener { _, isChecked ->
                    saveBoolean2SP(isChecked, IS_SCRAMBLE_VISIBLE, ctx)
                }

                constraints {
                    val layouts = arrayOf (delay500CheckBox, oneHandCheckBox,startButton,linLayout,scrambleCheckBox)
                    layouts.chainSpread(TOP of parentId,BOTTOM of parentId)

                    delay500CheckBox.connect(HORIZONTAL of parentId,
                            TOPS of parentId,
                            BOTTOM to TOP of oneHandCheckBox)
                    oneHandCheckBox.connect(HORIZONTAL of parentId,
                            TOP to BOTTOM of delay500CheckBox,
                            BOTTOM to TOP of startButton with (10.dp))
                    startButton.connect(HORIZONTAL of parentId,
                            TOP to BOTTOM of oneHandCheckBox with (10.dp),
                            BOTTOM to TOP of linLayout)
//                    metronom.connect(HORIZONTAL of parentId,
//                            TOP to BOTTOM of startButton)
                    linLayout.connect(HORIZONTAL of parentId,
                            TOP to BOTTOM of startButton,
                            BOTTOM to TOP of scrambleCheckBox)
                    scrambleCheckBox.connect(HORIZONTAL of parentId,
                            TOP to BOTTOM of linLayout,
                            BOTTOMS of parentId)


                }
            }.lparams(matchParent, wrapContent) {margin = m}
        }
    }

    private fun metronomTimeMinusOne(metronomTime: Int, view: View?, textHz: TextView) : Int {
        var metronomTime1 = metronomTime
        metronomTime1--
        if (metronomTime1 < 1) {
            metronomTime1 = 1
        }
        saveInt2SP(metronomTime1, METRONOM_TIME, view!!.context)
        textHz.text = metronomTime1.toString()
        return metronomTime1
    }

    private fun metronomTimePlusOne(metronomTime: Int, view: View?, textHz: TextView):Int {
        var metronomTime1 = metronomTime
        metronomTime1++
        if (metronomTime1 > 240) {
            metronomTime1 = 240
        }
        saveInt2SP(metronomTime1, METRONOM_TIME, view!!.context)
        textHz.text = metronomTime1.toString()
        return metronomTime1
    }
}
