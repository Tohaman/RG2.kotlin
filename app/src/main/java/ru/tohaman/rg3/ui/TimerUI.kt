package ru.tohaman.rg3.ui

import android.content.res.Configuration
import android.content.res.Configuration.*
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onTouch
import ru.tohaman.rg3.AnkoComponentEx
import ru.tohaman.rg3.DebugTag.TAG
import ru.tohaman.rg3.R
import ru.tohaman.rg3.ankoconstraintlayout.constraintLayout


/**
 * Created by Test on 15.12.2017. Интерфейс таймера
 */
class TimerUI<Fragment> : AnkoComponentEx<Fragment>() {

    override fun create(ui: AnkoContext<Fragment>): View = with(ui) {
        //толщина рамки в dp
        val m = 10.dp
        //высота верхней части
        val h = 100.dp
        val w = 220.dp
        //размер контрольных кружков рядом со временем
        val circleSize = 20.dp
        //размер текста таймера
        val timerTextSize = 24F
        //размер картинок с руками
        var handSize = 80.dp

        // screenSize это размер по специальной шкале класса Configuration: в диапазоне [1; 4],
        // чему соответствуют константы с наименованиями: SCREENLAYOUT_SIZE_SMALL, SCREENLAYOUT_SIZE_NORMAL,
        // SCREENLAYOUT_SIZE_LARGE и SCREENLAYOUT_SIZE_XLARGE.
        val screenSize = resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK
        when (screenSize) {
            SCREENLAYOUT_SIZE_SMALL -> {}
            SCREENLAYOUT_SIZE_NORMAL -> {}
            SCREENLAYOUT_SIZE_LARGE -> {}
            //SCREENLAYOUT_SIZE_XLARGE
            else -> {handSize = 120.dp}
        }
        Log.v (TAG, "TimerUI create start with ScreenSize = $screenSize")
        linearLayout {
            constraintLayout {
                backgroundColor = getColorFromResourses(R.color.blue)

                val leftPad = linearLayout {
                    backgroundColor = getColorFromResourses(R.color.dark_gray)
                }.lparams(0,0) {margin = m}    //{setMargins(m.dp,m.dp,m.dp,m.dp)}
                val rightPad = linearLayout {
                    backgroundColor = getColorFromResourses(R.color.dark_gray)
                }.lparams(0,0) {margin = m}    //{setMargins(m.dp,m.dp,m.dp,m.dp)}

                val topLayout = linearLayout {
                    backgroundColor = getColorFromResourses(R.color.blue)
                }.lparams(w,h)

                val topInsideLayout = linearLayout {
                    backgroundColor = getColorFromResourses(R.color.dark_gray)
                }.lparams(0,0) {margin = m}

                val timeLayout = linearLayout {
                    backgroundColor = getColorFromResourses(R.color.white)
                    textView {
                        text = "0:00:00"
                        textSize = timerTextSize
                        padding = m
                        typeface = Typeface.MONOSPACE
                        textColor = getColorFromResourses(R.color.black)
                    }
                }

                val leftCircle = imageView (R.drawable.timer_circle){
                }.lparams(circleSize,circleSize)

                val rightCircle = imageView (R.drawable.timer_circle){
                }.lparams(circleSize,circleSize)

                val leftHand = imageView (R.drawable.vtimer_1){
                }.lparams(handSize,handSize)

                val rightHand = imageView (R.drawable.vtimer_2){
                }.lparams(handSize,handSize)

                leftPad.setOnTouchListener() {v, event ->
                    Log.v (TAG, "TimerUI create onTouchListner.event.action = ${event.action}")
                    true
                }

//                leftPad.onTouch { v, event ->
//                    Log.v (TAG, "TimerUI create onTouchListner.event.action = ${event.action}")
//                    v.performClick()
//                }

                constraints {
                    val layouts = arrayOf (leftPad,rightPad)
                    layouts.chainSpreadInside(RIGHT of parentId,LEFT of parentId)

                    leftPad.connect(LEFTS of parentId,
                            TOPS of parentId,
                            RIGHT to LEFT of rightPad,
                            BOTTOMS of parentId
                    )
                    rightPad.connect( LEFT to RIGHT of leftPad,
                            TOPS of parentId,
                            RIGHTS of parentId,
                            BOTTOMS of parentId
                    )
                    topLayout.connect(RIGHTS of parentId,
                            TOPS of parentId,
                            LEFTS of parentId)

                    topInsideLayout.connect(RIGHTS of topLayout,
                            TOPS of topLayout,
                            LEFTS of topLayout,
                            BOTTOMS of topLayout)

                    timeLayout.connect(RIGHTS of topLayout,
                            TOPS of topLayout,
                            LEFTS of topLayout,
                            BOTTOMS of topLayout)

                    leftCircle.connect( RIGHT to LEFT of timeLayout,
                            TOPS of timeLayout,
                            LEFTS of topInsideLayout,
                            BOTTOMS of timeLayout)

                    rightCircle.connect( LEFT to RIGHT of timeLayout,
                            TOPS of timeLayout,
                            RIGHTS of topInsideLayout,
                            BOTTOMS of timeLayout)

                    leftHand.connect(HORIZONTAL of leftPad,
                            BOTTOMS of leftPad,
                            TOP to BOTTOM of topLayout)

                    rightHand.connect(HORIZONTAL of rightPad,
                            BOTTOMS of rightPad,
                            TOP to BOTTOM of topLayout)

                }
            }.lparams(matchParent, matchParent)
        }
    }


    fun getColorFromResourses (colorRes:Int):Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context.resources.getColor(colorRes,null)
        } else {
            @Suppress("DEPRECATION")
            context.resources.getColor(colorRes)
        }
    }

}
