package ru.tohaman.rg2.fragments


import android.content.res.Configuration
import android.graphics.Typeface
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.UI
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.dip
import ru.tohaman.rg2.*
import ru.tohaman.rg2.ankoconstraintlayout.constraintLayout


class FragmentTimer : Fragment(), View.OnTouchListener, SoundPool.OnLoadCompleteListener {
    private var startTime: Long = 0
    private var resetPressedTime: Long = 0
    private var leftHandDown = false
    private var rightHandDown = false
    private var isTimerReady = false
    private var isTimerStart = false

    private lateinit var leftPad: LinearLayout
    private lateinit var rightPad: LinearLayout
    private lateinit var topLayout: LinearLayout
    private lateinit var leftCircle: ImageView
    private lateinit var rightCircle: ImageView
    private lateinit var textTime: TextView

    private val MAX_STREAMS = 2
    private lateinit var spl: SoundPool
    private var soundIdTick: Int = 0

    private var oneHandToStart = true      //управление таймером одной рукой? или для старта надо положить обе
    private var metronomEnabled = true
    private var metronomTime = 60


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val sp = PreferenceManager.getDefaultSharedPreferences(ctx)
        oneHandToStart = sp.getBoolean(ONE_HAND_TO_START, false)
        metronomEnabled = sp.getBoolean(METRONOM_ENABLED, true)
        metronomTime = sp.getInt(METRONOM_TIME, 60)

        spl = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            SoundPool.Builder()
                    .setMaxStreams(2)
                    .build()
        } else {
            @Suppress("DEPRECATION")
            SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC, 0)
        }

        spl.setOnLoadCompleteListener(this)

        soundIdTick = spl.load(context, R.raw.metronom, 1)
        Log.d(DebugTag.TAG, "soundIdTick = " + soundIdTick)

        return createUI()
    }

    override fun onPause() {
        super.onPause()
        isTimerStart = false
        isTimerReady = false
    }

    override fun onResume() {
        super.onResume()
        timerReset()
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        val action = event.action
        when (v.id) {
            leftPad.id -> {
                //что-то сделали (нажали или отпустили) с левой панелью
                leftHandDown = onTouchAction(rightHandDown, action, leftCircle)
            }
            rightPad.id -> {
                //что-то сделали с правой панелью
                rightHandDown = onTouchAction(leftHandDown, action, rightCircle)
            }
            topLayout.id -> {
                //нажали или отпустили панель таймера
                if (action == MotionEvent.ACTION_DOWN) {
                    if (resetPressedTime + 300 > System.currentTimeMillis()) {
                        timerReset()
                    } else {
                        resetPressedTime = System.currentTimeMillis()
                    }
                }
            }
        }
        v.performClick()
        return true
    }

    // на входе состояние нажатий лев и прав панели, на выходе текущее состояние в зависимости от action Up или Down
    private fun onTouchAction(secondHand: Boolean, action: Int, handLight: ImageView): Boolean {
        //true если хоть что-то из этого true
        val secHand = secondHand or oneHandToStart
        //вот так в котлине when может возвращать какое-то значение, в данном случае положена или отпущена рука (true или false)
        return when (action) {
        //если что-нажато (первое прикосновение)
            MotionEvent.ACTION_DOWN -> {
                when {
                //если обе руки прикоснулись и таймер не статован, то значит таймер "готов", обнуляем время таймера
                    (secHand and !isTimerStart) -> {
                        isTimerReady = true
                        textTime.text = ctx.getString(R.string.begin_timer_text)}           //таймер готов к запуску
                //если обе руки прикоснулись, а таймер был запущен, значит его надо остановить
                    (secHand and isTimerStart) -> { stopTimer()}   //останавливаем таймер
                //в противном случае,
                    else -> { /**ничего не делаем */ }
                }
                // красим кружочек/чки
                setCircleColor(handLight, R.color.green)
                true        //вернем что текущая рука положена
            }

        // если отпустили палец
            MotionEvent.ACTION_UP -> {
                // Если таймер "готов", то запускаем таймер и
                if (isTimerReady) { startTimer() }
                // красим кружочек/чки готовности в красный если таймер не запущен
                if (!isTimerStart) { setCircleColor(handLight, R.color.red)}
                false       //вернем что текущая рука убрана
            }
        // если не отпустили, то считаем что нажата(положена)
            else -> true
        }
    }

    private fun setCircleColor(handLight: ImageView, colorId: Int) {
        val icon = ContextCompat.getDrawable(ctx, R.drawable.timer_circle)!!
        DrawableCompat.setTint(icon, getColorFromResources(colorId))
        //красим кружки, только текущий или оба в зависимости от одно- или дву-рукого управления
        if (oneHandToStart) {
            leftCircle.setImageDrawable(icon)
            rightCircle.setImageDrawable(icon)
        } else {
            handLight.setImageDrawable(icon)
        }
    }

    private fun timerReset() {
        Log.v (DebugTag.TAG, "TimerUI timerReset")
        isTimerStart = false
        isTimerReady = false
        textTime.text = ctx.getString(R.string.begin_timer_text)
    }

    private fun stopTimer() {
        Log.v (DebugTag.TAG, "TimerUI stopTimer")
        showTimerTime()
        isTimerStart = false
        isTimerReady = false
    }

    private fun startTimer() {
        Log.v (DebugTag.TAG, "TimerUI startTimer with metronomTime $metronomTime")
        isTimerReady = false                     // сняли "готовость" таймера
        startTime = System.currentTimeMillis()
        isTimerStart = true                      // поставили признак, что таймер запущен
        startShowTime()                         // запускаем фоновое отображение времени в фоне
        startMetronom()                         // запускаем метроном в фоне

    }

    private fun startShowTime () {
        //Используя корутины Котлина, отображаем время таймера, пока isTimerStart не станет false
        launch(UI) {
            do {
                showTimerTime()
                delay(30)
            } while (isTimerStart)
        }
    }

    private fun startMetronom() {
        if (metronomEnabled) {
            //Используя корутины Котлина, воспроизводим звук метронома, пока isTimerStart не станет false
            launch(CommonPool) {
                do {
                    spl.play(soundIdTick, 1F, 1F, 0, 0, 1F)
                    delay((60000/metronomTime).toLong())
                } while (isTimerStart)
            }
        }
    }

    private fun showTimerTime() {
        val curTime = System.currentTimeMillis() - startTime
        val millis = ((curTime % 1000) / 10).toInt()             // сотые доли секунды
        var seconds = (curTime / 1000).toInt()
        var minutes = seconds / 60
        seconds %= 60
        if (minutes > 9) {  //если получилось больше 10 минут, то добавляем к начальному времени 10 мин.
            startTime += 600000; minutes = 0
        }
        textTime.text = String.format("%d:%02d:%02d", minutes, seconds, millis)
    }

    private fun getColorFromResources(colorRes:Int):Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ctx.resources.getColor(colorRes,null)
        } else {
            @Suppress("DEPRECATION")
            ctx.resources.getColor(colorRes)
        }
    }

    private fun createUI(): View {
        //толщина рамки в dp
        val m = dip(10)
        //высота верхней части
        var h = dip(100)
        var w = dip (220)
        //размер контрольных кружков рядом со временем
        val circleSize = dip (20)
        //размер текста таймера
        val timerTextSize = 24F
        //размер картинок с руками
        var handSize = dip (80)

        // screenSize это размер по специальной шкале класса Configuration: в диапазоне [1..4],
        // чему соответствуют константы с наименованиями: SCREENLAYOUT_SIZE_SMALL, SCREENLAYOUT_SIZE_NORMAL,
        // SCREENLAYOUT_SIZE_LARGE и SCREENLAYOUT_SIZE_XLARGE.
        val screenSize = resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK
        when (screenSize) {
            Configuration.SCREENLAYOUT_SIZE_SMALL -> {}
            Configuration.SCREENLAYOUT_SIZE_NORMAL -> {}
            Configuration.SCREENLAYOUT_SIZE_LARGE -> {handSize = dip (120); h = dip(150); w = dip (330)}
        //SCREENLAYOUT_SIZE_XLARGE и может быть когда-то и больше
            else -> {handSize = dip (150); h = dip(220); w = dip (420)}
        }
        return UI {
            @Suppress("ClickableViewAccessibility")
            linearLayout {
                constraintLayout {
                    backgroundColorResource = R.color.blue

                    leftPad = linearLayout { backgroundColorResource = R.color.dark_gray }
                    leftPad.setOnTouchListener(this@FragmentTimer)

                    rightPad = linearLayout { backgroundColorResource = R.color.dark_gray }
                    rightPad.setOnTouchListener(this@FragmentTimer)

                    if (oneHandToStart) {
                        leftPad.lparams(0, 0) { setMargins(m, m, 0, m) }
                        rightPad.lparams(0, 0) { setMargins(0, m, m, m) }
                    } else {
                        leftPad.lparams(0, 0) { margin = m }    //{setMargins(m.dp,m.dp,m.dp,m.dp)}
                        rightPad.lparams(0, 0) { margin = m }    //{setMargins(m.dp,m.dp,m.dp,m.dp)}
                    }

                    topLayout = linearLayout {
                        backgroundColorResource = R.color.blue
                    }.lparams(w, h)
                    topLayout.setOnTouchListener(this@FragmentTimer)

                    val topInsideLayout = linearLayout {
                        backgroundColorResource = R.color.dark_gray
                    }.lparams(0, 0) { margin = m }

                    val timeLayout = linearLayout {
                        backgroundColorResource = R.color.white
                        textTime = textView {
                            text = ctx.getString(R.string.begin_timer_text)
                            textSize = timerTextSize
                            padding = m
                            typeface = Typeface.MONOSPACE
                            textColorResource = R.color.black
                        }
                    }

                    leftCircle = imageView(R.drawable.timer_circle) {
                    }.lparams(circleSize, circleSize)

                    rightCircle = imageView(R.drawable.timer_circle) {
                    }.lparams(circleSize, circleSize)

                    val leftHand = imageView(R.drawable.vtimer_1) {
                    }.lparams(handSize, handSize)

                    val rightHand = imageView(R.drawable.vtimer_2) {
                    }.lparams(handSize, handSize)

                    constraints {
                        val layouts = arrayOf(leftPad, rightPad)
                        layouts.chainSpreadInside(RIGHT of parentId, LEFT of parentId)

                        leftPad.connect(LEFTS of parentId,
                                TOPS of parentId,
                                RIGHT to LEFT of rightPad,
                                BOTTOMS of parentId
                        )
                        rightPad.connect(LEFT to RIGHT of leftPad,
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

                        leftCircle.connect(RIGHT to LEFT of timeLayout,
                                TOPS of timeLayout,
                                LEFTS of topInsideLayout,
                                BOTTOMS of timeLayout)

                        rightCircle.connect(LEFT to RIGHT of timeLayout,
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
        }.view
    }

    override fun onLoadComplete(soundPool: SoundPool?, sampleId: Int, status: Int) {
        Log.d(DebugTag.TAG, "onLoadComplete, sampleId = $sampleId, status = $status")
    }

    object TimerHandler : Handler()


}
