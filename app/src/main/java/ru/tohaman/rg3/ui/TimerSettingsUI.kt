package ru.tohaman.rg3.ui

import android.content.res.Configuration
import android.content.res.Configuration.*
import android.graphics.Typeface
import android.os.Build
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import org.jetbrains.anko.*
import ru.tohaman.rg3.AnkoComponentEx
import ru.tohaman.rg3.DebugTag.TAG
import ru.tohaman.rg3.R
import ru.tohaman.rg3.ankoconstraintlayout.constraintLayout
import com.google.android.youtube.player.internal.h
import com.google.android.youtube.player.internal.i


/**
 * Created by Test on 15.12.2017. Интерфейс таймера
 */
class TimerSettingsUI<Fragment> : AnkoComponentEx<Fragment>() {

    override fun create(ui: AnkoContext<Fragment>): View = with(ui) {
        //толщина рамки в dp
        val m = 10.dp
        //высота верхней части
        var h = 100.dp
        var w = 220.dp
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
            SCREENLAYOUT_SIZE_LARGE -> {handSize = 120.dp; h = 150; w = 330}
            //SCREENLAYOUT_SIZE_XLARGE и может быть когда-то и больше
            else -> {handSize = 150.dp; h = 180; w = 380}
        }

        Log.v (TAG, "TimerUI create start with ScreenSize = $screenSize")
        linearLayout {
            constraintLayout {
                backgroundColor = getColorFromResourses(R.color.dark_gray)

                val leftHand = imageView (R.drawable.vtimer_1){
                }.lparams(handSize,handSize)

                val rightHand = imageView (R.drawable.vtimer_2){
                }.lparams(handSize,handSize)

                constraints {

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
