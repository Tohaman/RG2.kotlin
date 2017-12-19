package ru.tohaman.rg3.ui

import android.content.res.Configuration
import android.content.res.Configuration.*
import android.graphics.Typeface
import android.os.Build
import android.os.Handler
import android.preference.PreferenceManager
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import org.jetbrains.anko.*
import ru.tohaman.rg3.DebugTag.TAG
import ru.tohaman.rg3.ankoconstraintlayout.constraintLayout
import android.media.SoundPool
import android.media.AudioManager
import ru.tohaman.rg3.*


/**
 * Created by Test on 15.12.2017. Интерфейс таймера
 */
class TimerUI<Fragment> : AnkoComponentEx<Fragment>() , View.OnTouchListener, SoundPool.OnLoadCompleteListener {

    private lateinit var leftPad: LinearLayout
    private lateinit var rightPad: LinearLayout
    private lateinit var topLayout: LinearLayout
    private lateinit var leftCircle: ImageView
    private lateinit var rightCircle: ImageView
    private lateinit var textTime: TextView

    val MAX_STREAMS = 2
    lateinit var spl: SoundPool
    var soundIdTick: Int = 0


    private var startTime: Long = 0
    private var reset_pressed_time: Long = 0
    private var leftHandDown = false
    private var rightHandDown = false
    private var timerReady = false
    private var timerStart = false
    private var oneHandToStart = true      //управление таймером одной рукой? или для старта надо положить обе
    private var metronomEnabled = true
    private var metronomTime = 80

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
            else -> {handSize = 150.dp; h = 220; w = 420}
        }

        var sp = PreferenceManager.getDefaultSharedPreferences(context)
        oneHandToStart = sp.getBoolean(ONE_HAND_TO_START, false)
        metronomEnabled = sp.getBoolean(METRONOM_ENABLED, true)
        metronomTime = sp.getInt(METRONOM_TIME, 80)

        spl = SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC, 0)
        spl.setOnLoadCompleteListener(this@TimerUI)

        soundIdTick = spl.load(context, R.raw.metronom, 1)
        Log.d(TAG, "soundIdTick = " + soundIdTick)

        Log.v (TAG, "TimerUI create start with ScreenSize = $screenSize")
        linearLayout {
            constraintLayout {
                backgroundColorResource = R.color.blue

                leftPad = linearLayout { backgroundColorResource = R.color.dark_gray }
                leftPad.setOnTouchListener(this@TimerUI)

                rightPad = linearLayout { backgroundColorResource = R.color.dark_gray }
                rightPad.setOnTouchListener(this@TimerUI)

                if (oneHandToStart) {
                    leftPad.lparams(0,0) {setMargins(m,m,0,m)}
                    rightPad.lparams(0,0) {setMargins(0,m,m,m)}
                } else {
                    leftPad.lparams(0,0) {margin = m}    //{setMargins(m.dp,m.dp,m.dp,m.dp)}
                    rightPad.lparams(0,0) {margin = m}    //{setMargins(m.dp,m.dp,m.dp,m.dp)}
                }

                topLayout = linearLayout {
                    backgroundColorResource = R.color.blue
                }.lparams(w,h)
                topLayout.setOnTouchListener(this@TimerUI)

                val topInsideLayout = linearLayout {
                    backgroundColorResource = R.color.dark_gray
                }.lparams(0,0) {margin = m}

                val timeLayout = linearLayout {
                    backgroundColorResource = R.color.white
                    textTime = textView {
                        text = context.getString(R.string.begin_timer_text)
                        textSize = timerTextSize
                        padding = m
                        typeface = Typeface.MONOSPACE
                        textColorResource = R.color.black
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
                //что-то сделали с левой панелью
//                Log.v (TAG, "TimerUI leftPad.action = $action TimerReady = $timerReady TimerStart = $timerStart")
                val icon = ContextCompat.getDrawable(context, R.drawable.timer_circle)
                leftHandDown = onTouchAction(leftHandDown, rightHandDown, action, leftCircle)
            }
            rightPad.id -> {
                //что-то сделали с правой панелью
//                Log.v (TAG, "TimerUI rightPad.action = $action TimerReady = $timerReady TimerStart = $timerStart")
                rightHandDown = onTouchAction(rightHandDown, leftHandDown, action, rightCircle)
            }
            topLayout.id -> {
                //нажали или отпустили панель таймера
//                Log.v (TAG, "TimerUI topLayout.action = $action TimerReady = $timerReady TimerStart = $timerStart")
                if (action == MotionEvent.ACTION_DOWN) {
                    if (reset_pressed_time + 300 > System.currentTimeMillis()) {
                        timerReset()
                    } else {
                        reset_pressed_time = System.currentTimeMillis()
                    }
                }
            }
        }
        return true
    }

    // на входе состояние нажатий лев и прав панели, на выходе текущее состояние в зависимости от action Up или Down
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
                    (sec_hand and timerStart) -> { stopTimer()}   //останавливаем таймер
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
                if (timerReady) { startTimer() }
                // красим кружочек/чки готовности в красный
                setCircleColor(handLight, R.color.red)
                false       //вернем что текущая рука убрана
            }
            // если не отпустили, то считаем что нажата(положена)
            else -> true
        }
    }

    private fun setCircleColor(handLight: ImageView, colorId: Int) {
        val icon = ContextCompat.getDrawable(context, R.drawable.timer_circle)
        DrawableCompat.setTint(icon, getColorFromResources(colorId))
        //красим кружки, только текущий или оба в зависимости от одно- или дву-рукого управления
        if (oneHandToStart) {
            leftCircle.setImageDrawable(icon)
            rightCircle.setImageDrawable(icon)
        } else {
            handLight.setImageDrawable(icon)
        }
    }

    private var timerRunnable: Runnable = object : Runnable {
        override fun run() {
            //выводим время на экран
            showTimerTime()
            //и планируем зауск себя через 30 милисекунд
            TimerHandler.postDelayed(this, 30)
        }
    }

    private var soundRunnable: Runnable = object : Runnable {
        override fun run() {
            spl.play(soundIdTick, 1F, 1F, 0, 0, 1F)
            TimerHandler.postDelayed(this, (60000/metronomTime).toLong())
        }
    }

    private fun timerReset() {
        Log.v (TAG, "TimerUI timerReset")
        stopTimer()
        textTime.text = context.getString(R.string.begin_timer_text)
    }

    fun stopTimer() {
        Log.v (TAG, "TimerUI stopTimer")
        showTimerTime()
        TimerHandler.removeCallbacks(timerRunnable)
        TimerHandler.removeCallbacks(soundRunnable)
        timerStart = false
        timerReady = false
    }

    fun startTimer() {
        Log.v (TAG, "TimerUI startTimer")
        timerStart = true                      // поставили признак, что таймер запущен
        timerReady = false                     // сняли "готовость" таймера
        startTime = System.currentTimeMillis()
        TimerHandler.post(timerRunnable)  //запускам хэндлер для отбражения времени, аналогично .postDelayed(timerRunnable, 0)
        if (metronomEnabled) {TimerHandler.post(soundRunnable)}  //запускам хэндлер для метронома
    }

    private fun showTimerTime() {
        val curtime = System.currentTimeMillis() - startTime
        val millis = ((curtime % 1000) / 10).toInt()             // сотые доли секунды
        var seconds = (curtime / 1000).toInt()
        var minutes = seconds / 60
        seconds %= 60
        if (minutes > 9) {  //если получилось больше 10 минут, то добавляем к начальному времени 10 мин.
            startTime += 600000; minutes = 0
        }
        textTime.text = String.format("%d:%02d:%02d", minutes, seconds, millis)
    }


    private fun getColorFromResources(colorRes:Int):Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context.resources.getColor(colorRes,null)
        } else {
            @Suppress("DEPRECATION")
            context.resources.getColor(colorRes)
        }
    }

    override fun onLoadComplete(soundPool: SoundPool?, sampleId: Int, status: Int) {
        Log.d(TAG, "onLoadComplete, sampleId = $sampleId, status = $status")
    }


    object TimerHandler : Handler()
}
