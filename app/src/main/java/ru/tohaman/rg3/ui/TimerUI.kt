package ru.tohaman.rg3.ui

import android.content.res.Configuration
import android.content.res.Configuration.*
import android.graphics.Typeface
import android.os.Build
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


/**
 * Created by Test on 15.12.2017. Интерфейс таймера
 */
class TimerUI<Fragment> : AnkoComponentEx<Fragment>() , View.OnTouchListener{
    private lateinit var leftPad: LinearLayout
    private lateinit var rightPad: LinearLayout
    private lateinit var topLayout: LinearLayout
    private lateinit var leftCircle: ImageView
    private lateinit var rightCircle: ImageView
    private lateinit var textTime: TextView

    private var startTime: Long = 0
    private var reset_pressed_time: Long = 0
    private var leftHandDown = false
    private var rightHandDown = false
    private var timerReady = false
    private var timerStart = false
    private var oneHandToStart = true       //управление таймером одной рукой? или для старта надо положить обе

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
                backgroundColor = getColorFromResourses(R.color.blue)

                leftPad = linearLayout {
                    backgroundColor = getColorFromResourses(R.color.dark_gray)
                }.lparams(0,0) {margin = m}    //{setMargins(m.dp,m.dp,m.dp,m.dp)}
                leftPad.setOnTouchListener(this@TimerUI)

                rightPad = linearLayout {
                    backgroundColor = getColorFromResourses(R.color.dark_gray)
                }.lparams(0,0) {margin = m}    //{setMargins(m.dp,m.dp,m.dp,m.dp)}
                rightPad.setOnTouchListener(this@TimerUI)

                if (oneHandToStart) {
                    leftPad.lparams(0,0) {setMargins(m,m,0,m)}
                    rightPad.lparams(0,0) {setMargins(0,m,m,m)}
                }

                topLayout = linearLayout {
                    backgroundColor = getColorFromResourses(R.color.blue)
                }.lparams(w,h)
                topLayout.setOnTouchListener(this@TimerUI)

                val topInsideLayout = linearLayout {
                    backgroundColor = getColorFromResourses(R.color.dark_gray)
                }.lparams(0,0) {margin = m}

                val timeLayout = linearLayout {
                    backgroundColor = getColorFromResourses(R.color.white)
                    textTime = textView {
                        id = Ids.textTimer
                        text = "0:00:00"
                        textSize = timerTextSize
                        padding = m
                        typeface = Typeface.MONOSPACE
                        textColor = getColorFromResourses(R.color.black)
                    }
                }

                leftCircle = imageView (R.drawable.timer_circle){
                }.lparams(circleSize,circleSize)

                rightCircle = imageView (R.drawable.timer_circle){
                }.lparams(circleSize,circleSize)

                val leftHand = imageView (R.drawable.vtimer_1){
                }.lparams(handSize,handSize)

                val rightHand = imageView (R.drawable.vtimer_2){
                }.lparams(handSize,handSize)

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

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        v?.performClick()
        val action = event!!.action
        when (v?.id) {
            leftPad.id -> {
//                Log.v (TAG, "TimerUI leftPad.action = $action TimerReady = $timerReady TimerStart = $timerStart")
                val icon = ContextCompat.getDrawable(context, R.drawable.timer_circle)
                leftHandDown = onTouchAction(leftHandDown, rightHandDown, action, leftCircle)
            }
            rightPad.id -> {
//                Log.v (TAG, "TimerUI rightPad.action = $action TimerReady = $timerReady TimerStart = $timerStart")
                rightHandDown = onTouchAction(rightHandDown, leftHandDown, action, rightCircle)
            }
            topLayout.id -> {
//                Log.v (TAG, "TimerUI topLayout.action = $action TimerReady = $timerReady TimerStart = $timerStart")
                if (action == MotionEvent.ACTION_DOWN) {
                    if (reset_pressed_time + 300 > System.currentTimeMillis()) {
                        TimerReset()
                    } else {
                        reset_pressed_time = System.currentTimeMillis()
                    }
                }
            }
        }
        return true
    }

    private fun onTouchAction(first_hand: Boolean, second_hand: Boolean, action: Int, handLight: ImageView): Boolean {
        //true если хоть что-то из этого true
        val sec_hand = second_hand or oneHandToStart
        //вот так в котлине when может возвращать какое-то значение, в данном случае положена или отпущена рука (true или false)
        return when (action) {
            //если что-нажато (первое прикосновение)
            MotionEvent.ACTION_DOWN -> {
                when {
                    //если обе руки прикоснулись и таймер не статован, то значит таймер "готов"
                    (sec_hand and !timerStart) -> {timerReady = true}           //таймер готов к запуску
                    //если обе руки прикоснулись, а таймер был запущен, значит его надо остановить
                    (sec_hand and timerStart) -> { StopTimer()}   //останавливаем таймер
                    //в противном случае,
                    else -> { /**ничего не делаем */ }
                }
                // красим кружочек/чки
                setCircleColor(handLight,R.color.green)
                true        //вернем что текущая рука положена
            }

            // если отпустили палец
            MotionEvent.ACTION_UP -> {
                // Если таймер "готов", то запускаем таймер и
                if (timerReady) { StartTimer() }
                // красим кружочек/чки готовности в красный
                setCircleColor(handLight, R.color.red)
                false       //вернем что текущая рука убрана
            }
            // если не отпустили, то считаем что нажата(положена)
            else -> true
        }
    }

    fun setCircleColor(handLight: ImageView, colorId: Int) {
        val icon = ContextCompat.getDrawable(context, R.drawable.timer_circle)
        DrawableCompat.setTint(icon, getColorFromResourses(colorId))
        //красим кружки, только текущий или оба в зависимости от одно или двурукого управления
        if (oneHandToStart) {
            leftCircle.setImageDrawable(icon)
            rightCircle.setImageDrawable(icon)
        } else {
            handLight.setImageDrawable(icon)
        }
    }

    private fun TimerReset() {
        Log.v (TAG, "TimerUI TimerReset")
        StopTimer()
        textTime.text = "0:00:00"
    }

    fun StopTimer() {
        Log.v (TAG, "TimerUI StopTimer")
        timerStart = false
        timerReady = false
    }

    fun StartTimer() {
        Log.v (TAG, "TimerUI StartTimer")
        timerStart = true                      // поставили признак, что таймер запущен
        timerReady = false                     // сняли "готовость" таймера
    }

    fun getColorFromResourses (colorRes:Int):Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context.resources.getColor(colorRes,null)
        } else {
            @Suppress("DEPRECATION")
            context.resources.getColor(colorRes)
        }
    }

    object Ids {
        val textTimer = 1
    }

}
