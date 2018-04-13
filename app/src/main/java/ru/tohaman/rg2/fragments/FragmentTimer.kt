package ru.tohaman.rg2.fragments


import android.content.Context
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
import android.view.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk15.coroutines.onClick
import org.jetbrains.anko.sdk25.coroutines.onItemClick
import org.jetbrains.anko.support.v4.UI
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.dip
import ru.tohaman.rg2.*
import ru.tohaman.rg2.adapters.TimeListAdapter
import ru.tohaman.rg2.ankoconstraintlayout.constraintLayout
import ru.tohaman.rg2.data.TimeNote
import ru.tohaman.rg2.data.database
import ru.tohaman.rg2.fragments.FragmentScrambleGen.Companion.generateScrambleWithParam
import ru.tohaman.rg2.util.saveString2SP
import java.text.SimpleDateFormat
import java.util.*
import android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT
import android.content.Context.INPUT_METHOD_SERVICE
import android.print.PrintAttributes
import android.view.inputmethod.InputMethodManager
import android.widget.*


class FragmentTimer : Fragment(), View.OnTouchListener, SoundPool.OnLoadCompleteListener {
    private var startTime: Long = 0
    private var resetPressedTime: Long = 0
    private var leftHandDown = false
    private var rightHandDown = false
    private var isTimerReady = false
    private var isTimerStart = false
    private var isScrambleVisible = true
    private var chkEdgesBuffer = true
    private var chkCornersBuffer = false
    private var scrambleLength = 14

    private lateinit var leftPad: LinearLayout
    private lateinit var rightPad: LinearLayout
    private lateinit var oneHandPad: LinearLayout
    private lateinit var centerLayout: LinearLayout
    private lateinit var topLayout: LinearLayout
    private lateinit var saveResultLayout: LinearLayout
    private lateinit var leftCircle: ImageView
    private lateinit var rightCircle: ImageView
    private lateinit var textTime: TextView
    private lateinit var scrambleTextView : TextView

    private val maxStreams = 2
    private lateinit var spl: SoundPool
    private lateinit var timeNoteList: List<TimeNote>
    private var soundIdTick: Int = 0

    private var oneHandTimer = true      //управление таймером одной рукой? или для старта надо положить обе
    private var metronomEnabled = true
    private var metronomTime = 60
    private var text4Scramble = ""
    private var curTime = ""
    private var delayMills = 500L

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.v (DebugTag.TAG, "FragmentTimer onCreate")
        super.onCreate(savedInstanceState)
        retainInstance = true
        val sp = PreferenceManager.getDefaultSharedPreferences(ctx)
        oneHandTimer = sp.getBoolean(ONE_HAND_TO_START, false)
        metronomEnabled = sp.getBoolean(METRONOM_ENABLED, true)
        metronomTime = sp.getInt(METRONOM_TIME, 60)
        text4Scramble = sp.getString(SCRAMBLE, "U2 F2 L\' D2 R U F\' L2 B2 R L2 B2 U R")
        delayMills = sp.getInt(DELAY_MILLS, 500).toLong()
        isScrambleVisible = sp.getBoolean(IS_SCRAMBLE_VISIBLE, true)
        chkEdgesBuffer = sp.getBoolean(CHK_BUF_EDGES, true)
        chkCornersBuffer = sp.getBoolean(CHK_BUF_CORNERS, false)
        scrambleLength = sp.getInt(SCRAMBLE_LEN, 14)

        curTime = ctx.getString(R.string.begin_timer_text)


//      Примеры работы с базой времени
//        val time = SimpleDateFormat("mm:ss.SS", Locale.US).format(Calendar.getInstance().time)
//        val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
//        val now = df.format(Calendar.getInstance().time)
//        val comment = ""
//        val scramble = generateScramble(14)
//        var tm = TimeNote (time, now, scramble, comment)
//        //Добавляем запись в базу
//        ctx.database.addTimeNote2Base(tm)
//        tm.uuid = "3"
//        tm.comment = "dfhsidhfkj"
//        //Обновляем запись в базе
//        ctx.database.updateTimeNoteInBase(tm)
//        //Удаляем запись из базы по uuid
//        ctx.database.deleteTimeNoteInBase(2)

    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.v (DebugTag.TAG, "FragmentTimer onCreateView")
        spl = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            SoundPool.Builder()
                    .setMaxStreams(2)
                    .build()
        } else {
            @Suppress("DEPRECATION")
            SoundPool(maxStreams, AudioManager.STREAM_MUSIC, 0)
        }

        spl.setOnLoadCompleteListener(this)

        soundIdTick = spl.load(context, R.raw.metronom, 1)
        Log.d(DebugTag.TAG, "soundIdTick = $soundIdTick")

        return createUI()
    }

    override fun onPause() {
        super.onPause()
        Log.v (DebugTag.TAG, "FragmentTimer onPause")
        //Останавливаем таймер, если фрагмент перестал быть в фокусе (при поворете, переключении программы)
//        isTimerStart = false
//        isTimerReady = false
    }

    fun backButtonWasPressed () {
        //Останавливаем таймер, если в активности нажали кнопку back
        isTimerStart = false
        isTimerReady = false
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        Log.v (DebugTag.TAG, "FragmentTimer onSaveInstanceState $curTime")
        super.onSaveInstanceState(savedInstanceState)
        savedInstanceState.putString("time", curTime)
    }

    override fun onResume() {
        super.onResume()
        Log.v (DebugTag.TAG, "FragmentTimer onResume")
        //timerReset()
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
            oneHandPad.id -> {
                //что-то сделали с панелью для управления одной рукой
                rightHandDown = onTouchAction(leftHandDown, action, rightCircle)
            }
            centerLayout.id -> {
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
        val secHand = secondHand or oneHandTimer
        //вот так в котлине when может возвращать какое-то значение, в данном случае положена или отпущена рука (true или false)

        return when (action) {
        //если что-нажато (первое прикосновение)
            MotionEvent.ACTION_DOWN -> {
                when {
                    //если обе руки прикоснулись и таймер не статован, то значит таймер "готов", обнуляем время таймера
                    (secHand and !isTimerStart) -> {
                        isTimerReady = false
                        resetPressedTime = System.currentTimeMillis()
                        launch(UI) {
                            delay (delayMills)
                            if (resetPressedTime + delayMills - 1 < System.currentTimeMillis()) {
                                isTimerReady = true             //таймер готов к запуску, значит красим оба кужка зеленым
                                setCircleColor(leftCircle, R.color.green)
                                setCircleColor(rightCircle, R.color.green)
                                saveResultLayout.visibility = View.GONE
                            }
                        }
                        textTime.text = ctx.getString(R.string.begin_timer_text)
                    }
                    //если обе руки прикоснулись, а таймер был запущен, значит его надо остановить
                    (secHand and isTimerStart) -> {
                        stopTimer()                     //останавливаем таймер
                    }
                    //в противном случае,
                    else -> {
                        /**ничего не делаем */
                    }
                }
                // красим кружочек/чки
                setCircleColor(handLight, R.color.yellow)
                true        //вернем что текущая рука положена
            }

        // если отпустили палец
            MotionEvent.ACTION_UP -> {
                //Если что-то отпустили, сбрасываем таймер задежки для TimerReady
                resetPressedTime = System.currentTimeMillis()
                // Если таймер "готов", то запускаем таймер и
                if (isTimerReady) {
                    startTimer()
                }
                // теперь красим кружочек/чки готовности в красный или зеленый в зависимости, от того запущен ли таймер
                if (!isTimerStart) {
                    setCircleColor(handLight, R.color.red)
                } else {
                    setCircleColor(handLight, R.color.green)
                }
                false       //вернем что текущая рука убрана
            }
        // если не отпустили, то считаем что нажата(положена)
            else -> true
        }
    }

    private fun setCircleColor(handLight: ImageView, colorId: Int) {
        val icon = ContextCompat.getDrawable(ctx, R.drawable.timer_circle)
        DrawableCompat.setTint(icon!!, ContextCompat.getColor(ctx, colorId))
        //красим кружки, только текущий или оба в зависимости от одно- или дву-рукого управления

        if (oneHandTimer) {
            leftCircle.image = icon
            rightCircle.image = icon
        } else {
            handLight.image = icon
        }
    }

    private fun timerReset() {
        Log.v (DebugTag.TAG, "TimerUI timerReset")
        isTimerStart = false
        isTimerReady = false
        curTime = ctx.getString(R.string.begin_timer_text)
        textTime.text = curTime
        saveResultLayout.visibility = View.GONE
    }

    private fun stopTimer() {
        Log.v (DebugTag.TAG, "TimerUI stopTimer")
        showTimerTime()
        isTimerStart = false
        isTimerReady = false
        if (isScrambleVisible) { scrambleTextView.visibility = View.VISIBLE }
        topLayout.visibility = View.VISIBLE
        saveResultLayout.visibility = View.VISIBLE
    }

    private fun startTimer() {
        Log.v (DebugTag.TAG, "TimerUI startTimer with metronomTime $metronomTime")
        isTimerReady = false                     // сняли "готовость" таймера
        startTime = System.currentTimeMillis()
        isTimerStart = true                      // поставили признак, что таймер запущен
        startShowTime()                         // запускаем фоновое отображение времени в фоне
        startMetronom()                         // запускаем метроном в фоне
        scrambleTextView.visibility = View.GONE
        topLayout.visibility = View.GONE
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
        val currentTime = System.currentTimeMillis() - startTime
        val millis = ((currentTime % 1000) / 10).toInt()             // сотые доли секунды
        var seconds = (currentTime / 1000).toInt()
        var minutes = seconds / 60
        seconds %= 60
        if (minutes > 9) {  //если получилось больше 10 минут, то добавляем к начальному времени 10 мин.
            startTime += 600000; minutes = 0
        }
        curTime = String.format("%d:%02d.%02d", minutes, seconds, millis)
        textTime.text = curTime
    }

//    private fun getColorFromResources(colorRes:Int):Int {
//        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            ctx.resources.getColor(colorRes,null)
//        } else {
//            @Suppress("DEPRECATION")
//            ctx.resources.getColor(colorRes)
//        }
//    }

//    override fun onSaveInstanceState(savedInstanceState: Bundle) {
//        Log.v (DebugTag.TAG, "FragmentTimer onSaveInstanceState")
//        super.onSaveInstanceState(savedInstanceState)
//        savedInstanceState.putString("time", textTime.text.toString())
//    }

    private fun createUI(): View {
        Log.v (DebugTag.TAG, "FragmentTimer onCreateUI")
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

                    topLayout = linearLayout {
                        gravity = Gravity.CENTER
                        padding = m
                        backgroundColorResource = R.color.dark_gray
                        imageView (R.drawable.ic_list)
                        textView ("Результаты:") {
                            textSize = 18F
                            textColorResource = R.color.white
                            leftPadding = m
                        }
                    }.lparams(matchConstraint,wrapContent)

                    topLayout.onClick {
                        showListOfTimes()
                    }

                    leftPad = linearLayout {
                        backgroundColorResource = R.color.dark_gray
                    }.lparams(0, 0) { margin = m }
                    leftPad.setOnTouchListener(this@FragmentTimer)

                    rightPad = linearLayout {
                        backgroundColorResource = R.color.dark_gray
                    }.lparams(0, 0) { margin = m }
                    rightPad.setOnTouchListener(this@FragmentTimer)

                    oneHandPad = linearLayout {
                        backgroundColorResource = R.color.dark_gray
                    }.lparams(0, 0) { margin = m }
                    oneHandPad.setOnTouchListener(this@FragmentTimer)

                    if (oneHandTimer) {
                        leftPad.visibility = View.INVISIBLE
                        rightPad.visibility = View.INVISIBLE
                        oneHandPad.visibility = View.VISIBLE
                    } else {
                        leftPad.visibility = View.VISIBLE
                        rightPad.visibility = View.VISIBLE
                        oneHandPad.visibility = View.INVISIBLE
                    }

                    centerLayout = linearLayout {
                        backgroundColorResource = R.color.blue
                    }.lparams(w, h)
                    centerLayout.setOnTouchListener(this@FragmentTimer)

                    val topInsideLayout = linearLayout {
                        backgroundColorResource = R.color.dark_gray
                    }.lparams(0, 0) { margin = m }

                    val timeLayout = linearLayout {
                        backgroundColorResource = R.color.white
                        textTime = textView {
                            text = curTime
                            textSize = timerTextSize
                            horizontalPadding = m
                            verticalPadding = m/2
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

                    scrambleTextView = textView {
                        text = text4Scramble
                        textSize = 16F
                        gravity = Gravity.CENTER
                        textColorResource = R.color.white
                        backgroundColorResource = R.color.dark_gray
                        padding = m
                        visibility = if (isScrambleVisible) {View.VISIBLE} else {View.GONE}
                    }.lparams(0, wrapContent)

                    scrambleTextView.onClick {
                        newScramble()
                    }

                    saveResultLayout = verticalLayout {
                        visibility = View.GONE
                        textView {
                            text = "Сохранить результат?"
                            textColorResource = R.color.white
                            gravity = Gravity.CENTER
                            backgroundColorResource = R.color.dark_gray
                            padding = m
                        }
                        linearLayout {
                            padding = m
                            backgroundColorResource = R.color.dark_gray
                            val cancelButton = imageButton {
                                imageResource = R.drawable.ic_delete
                                padding = m
                            }
                            val withCommentButton = imageButton {
                                imageResource = R.drawable.ic_comment2
                                padding = m
                            }

                            val okButton = imageButton {
                                imageResource = R.drawable.ic_ok
                                padding = m
                            }

                            val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
                            val now = df.format(Calendar.getInstance().time)

                            cancelButton.onClick { saveResultLayout.visibility = View.GONE }

                            withCommentButton.onClick {
                                val imm = ctx.inputMethodManager
                                alert("Сохранить результат с комментарием:") {
                                    customView {
                                        val eText = editText().lparams {margin = m}
                                        imm.toggleSoftInput(InputMethodManager.RESULT_SHOWN,0)
                                        positiveButton("OK") {
                                            saveTimeResult(now, eText.text.toString())
                                            imm.hideSoftInputFromWindow(eText.windowToken, 0)
                                        }
                                        negativeButton("Отмена") {
                                            imm.hideSoftInputFromWindow(eText.windowToken, 0)
                                        }
                                    }
                                }.show()
                            }

                            okButton.onClick {
                                val comment = ""
                                saveTimeResult(now, comment)
                            }
                        }
                    }.lparams(wrapContent,wrapContent)

                    constraints {
                        val layouts = arrayOf(leftPad, rightPad)
                        layouts.chainSpreadInside(RIGHT of parentId, LEFT of parentId)

                        topLayout.connect(HORIZONTAL of parentId,
                                TOPS of parentId)

                        scrambleTextView.connect(LEFTS of parentId,
                                RIGHTS of parentId,
                                BOTTOMS of parentId
                                )

                        saveResultLayout.connect( LEFTS of parentId,
                                RIGHTS of parentId,
                                BOTTOMS of leftPad)

                        leftPad.connect(LEFTS of parentId,
                                TOP to BOTTOM of topLayout,
                                RIGHT to LEFT of rightPad,
                                BOTTOM to TOP of scrambleTextView
                        )

                        rightPad.connect(LEFT to RIGHT of leftPad,
                                TOP to BOTTOM of topLayout,
                                RIGHTS of parentId,
                                BOTTOM to TOP of scrambleTextView
                        )

                        oneHandPad.connect(LEFTS of parentId,
                                TOP to BOTTOM of topLayout,
                                RIGHTS of parentId,
                                BOTTOM to TOP of scrambleTextView
                        )

                        centerLayout.connect(RIGHTS of parentId,
                                TOP to BOTTOM of topLayout,
                                LEFTS of parentId)

                        topInsideLayout.connect(RIGHTS of centerLayout,
                                TOPS of centerLayout,
                                LEFTS of centerLayout,
                                BOTTOMS of centerLayout)

                        timeLayout.connect(RIGHTS of centerLayout,
                                TOPS of centerLayout,
                                LEFTS of centerLayout,
                                BOTTOMS of centerLayout)

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
                                TOP to BOTTOM of centerLayout)

                        rightHand.connect(HORIZONTAL of rightPad,
                                BOTTOMS of rightPad,
                                TOP to BOTTOM of centerLayout)

                    }
                }.lparams(matchParent, matchParent)
            }
        }.view
    }

    private fun AnkoContext<Fragment>.saveTimeResult(now: String, comment: String) {
        val time = textTime.text.toString()
        val scramble = if (isScrambleVisible) { scrambleTextView.text.toString()} else {"Случайный скрамбл"}
        val tm = TimeNote(time, now, scramble, comment)
        //Добавляем запись в базу
        ctx.database.addTimeNote2Base(tm)
        saveResultLayout.visibility = View.GONE
        newScramble()
    }

    private fun AnkoContext<Fragment>.showListOfTimes() {
        timeNoteList = getSortedTimeNoteList()
        alert ("Список результатов:"){
            customView {
                positiveButton("Закрыть окно") {
                }
                verticalLayout {
                    val lstView = listView {
                        adapter = TimeListAdapter(timeNoteList)
                    }
                    lstView.onItemClick { _, _, i, _ ->
                        showTimeResultScreen(i, lstView)
                    }
                }
            }
        }.show()
    }

    private fun AnkoContext<Fragment>.getSortedTimeNoteList():List<TimeNote> {
        val tnList = ctx.database.getTimeNoteFromBase()
        return tnList.sortedWith(compareBy(TimeNote::time))
    }

    private fun AnkoContext<Fragment>.showTimeResultScreen(i: Int, lstView: ListView) {
        alert {
            customView {
                positiveButton("OK") {

                }
                negativeButton("Удалить запись") {
                    //Удаляем запись из базы по uuid
                    val uuid = timeNoteList[i].uuid.toInt()
                    ctx.database.deleteTimeNoteInBase(uuid)
                    timeNoteList = getSortedTimeNoteList()
                    lstView.adapter = TimeListAdapter(timeNoteList)
                }
                constraintLayout {
                    val timeTextView = textView {
                        text = timeNoteList[i].time
                        textSize = 28f
                        padding = dip(10)
                        typeface = Typeface.DEFAULT_BOLD
                    }

                    val dateTextView = textView {
                        var df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
                        val date = df.parse(timeNoteList[i].dateTime)
                        df = SimpleDateFormat("dd MMMM yyyy HH:mm", Locale.getDefault())
                        text = df.format(date)
                        textSize = 12f
                        padding = dip(10)
                    }

                    val divider = view {
                        backgroundColorResource = R.color.c_lgr
                    }.lparams(matchConstraint, dip(2)) { horizontalMargin = dip(10)}

                    val imgComment = imageView {
                        imageResource = R.drawable.ic_comment
                    }.lparams(dip(24), dip(24)) { topMargin = dip(16); leftMargin = dip(10)}

                    val textComment = textView {
                        text = if (timeNoteList[i].comment == "") {
                            "Нажмите сюда, чтобы добавить свой комментарий."
                        } else {
                            timeNoteList[i].comment
                        }
                        textSize = 16F
                    }.lparams (0){ topMargin = dip(16); leftMargin = dip(10); rightMargin = dip(10)}

                    textComment.onClick {
                        showEditCommentWindows(i, textComment)
                    }

                    val scrambleImage = imageView {
                        imageResource = R.drawable.ic_scramble
                    }.lparams(dip(24), dip(24)) { setMargins(dip(10),dip(16),0,0) }

                    val textScramble = textView {
                        text = timeNoteList[i].scramble
                        textSize = 16F
                    }.lparams (0){ setMargins(dip(10),dip(16),dip(10),0) }

                    constraints {
                        timeTextView.connect(TOPS of parentId,
                                LEFTS of parentId)

                        dateTextView.connect(TOPS of parentId,
                                RIGHTS of parentId)

                        divider.connect(TOP to BOTTOM of timeTextView,
                                HORIZONTAL of parentId)

                        imgComment.connect(TOP to BOTTOM of divider,
                                BOTTOMS of textComment,
                                LEFTS of parentId)

                        textComment.connect(TOP to BOTTOM of divider,
                                RIGHTS of parentId,
                                LEFT to RIGHT of imgComment)

                        scrambleImage.connect(TOP to BOTTOM of textComment,
                                BOTTOMS of parentId,
                                LEFTS of parentId)

                        textScramble.connect(TOP to BOTTOM of textComment,
                                RIGHTS of parentId,
                                LEFT to RIGHT of scrambleImage)
                    }
                }
            }
        }.show()
    }

    private fun AnkoContext<Fragment>.showEditCommentWindows(i: Int, textComment: TextView) {
        alert ("Введите свой комментарий"){
            customView {
                val imm = ctx.inputMethodManager
                verticalLayout {
                    val eText = editText(timeNoteList[i].comment) {
                    }
                    imm.toggleSoftInput(InputMethodManager.RESULT_SHOWN,0)

                    positiveButton("OK") {
                        imm.hideSoftInputFromWindow(eText.windowToken, 0)
                        val commentSt = eText.text.toString()
                        textComment.text = commentSt
                        timeNoteList[i].comment = commentSt
                        //Обновляем запись в базе
                        ctx.database.updateTimeNoteInBase(timeNoteList[i])
                    }
                    negativeButton("Отмена") {
                        imm.hideSoftInputFromWindow(eText.windowToken, 0)
                    }
                }
            }
        }.show()
    }

    private fun newScramble() {
        text4Scramble = generateScrambleWithParam(chkEdgesBuffer, chkCornersBuffer, scrambleLength, ctx)
        scrambleTextView.text = text4Scramble
        saveString2SP(text4Scramble, SCRAMBLE, ctx)
        timerReset()
    }

    override fun onLoadComplete(soundPool: SoundPool?, sampleId: Int, status: Int) {
        Log.d(DebugTag.TAG, "onLoadComplete, sampleId = $sampleId, status = $status")
    }

    object TimerHandler : Handler()


}
