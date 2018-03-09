package ru.tohaman.rg2

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import org.jetbrains.anko.*
import ru.tohaman.rg2.data.ListPagerLab
import ru.tohaman.rg2.DebugTag.TAG
import ru.tohaman.rg2.activities.SlidingTabsActivity
import ru.tohaman.rg2.fragments.*
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ListView
import ru.tohaman.rg2.DeveloperKey.base64EncodedPublicKey
import ru.tohaman.rg2.activities.OllTestGame
import ru.tohaman.rg2.adapters.MyListAdapter
import ru.tohaman.rg2.util.*


// Статические переменные (верхнего уровня). Котлин в действии стр.77-78
const val EXTRA_ID = "ru.tohaman.rubicsguide.PHASE_ID"
const val RUBIC_PHASE = "ru.tohaman.rubicsguide.PHASE"
const val VIDEO_PREVIEW = "video_preview"   //наименования ключа для сохранения/извлечения значения из файла настроек
const val IS_VIDEO_SCREEN_ON = "videoscreen_on"  //ключ для гашения/не гашения экрана когда видео на паузе
const val IS_SCREEN_ALWAYS_ON = "screen_always_on"  //ключ для гашения/не гашения экрана когда видео на паузе
const val ONE_HAND_TO_START = "oneHandToStart"
const val METRONOM_ENABLED = "metronomEnabled"
const val METRONOM_TIME = "metronomTime"
const val TEST_GAME_ROW_COUNT = "pllTestRowCount"
const val BLIND_ROW_COUNT = "blindRowCount"
const val PLL_TEST_3SIDE = "isPllTest3Side"
const val OLL_TEST_GAME = "isOllGame"
const val BLIND_IS_EDGE_CHECKED = "isBlindEdgeChecked"
const val BLIND_IS_CORNER_CHECKED = "isBlindCornerChecked"
const val FAVORITES = "favorites"
const val DEFAULT_DRAWABLE_SIZE = 1

// SKUs для продуктов: при изменении не забыть поправить в sayThanks
const val BIG_DONATION = "big_donation"
const val MEDIUM_DONATION = "medium_donation"
const val SMALL_DONATION = "small_donation"

class MainActivity : MyDefaultActivity(),
        NavigationView.OnNavigationItemSelectedListener,
        FragmentListView.OnListViewInteractionListener,
        FragmentScrambleGen.OnSrambleGenInteractionListener,
        SharedPreferences.OnSharedPreferenceChangeListener,
        FragmentBlindGameSettings.OnBlindGameInteractionListener,
        IabBroadcastReceiver.IabBroadcastListener {

    // Пробуем добавить платежи внутри программы https://xakep.ru/2017/05/23/android-in-apps/
    // Пользователь уже платил?
    private var mIsPremium = false

    // (arbitrary) request code for the purchase flow
    private val RC_REQUEST = 10001
    // Тут будем подсчитывать сколько пользователь уже заплатил, пока не знаю для чего
    private var mCoins: Int = 0
    // Все ли хрошо с запуском GooglePlay, если нет, то оплату не запускаем
    private var mGooglePlayOK: Boolean = true
    // The helper object
    private var mHelper: IabHelper? = null
    // Provides purchase notification while this app is running
    private var mBroadcastReceiver: IabBroadcastReceiver? = null

    private lateinit var fragListView: FragmentListView
    private var backPressedTime: Long = 0
    private lateinit var mListPagerLab: ListPagerLab
    private var curPhase = "BEGIN"
    private var changedPhase = "BEGIN"
    private var changedId = 0
    private val listOfGo2Fridrich = listOf("ACCEL", "CROSS", "F2L", "ADVF2L", "OLL", "PLL", "RECOMEND")
    private val listOfOtherPuzzle = listOf("BEGIN4X4","BEGIN5X5","PYRAMINX", "MEGAMINX", "SKEWB")
    private val listOfBasic = listOf("BASIC3X3", "BASIC_PYR", "BASIC_SKEWB", "BASIC4X4", "BASIC5X5")

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        Log.v (TAG, "MainActivity ListPagerLab init")
        mListPagerLab = ListPagerLab.get(ctx)

        // Регистрируем слушатель OnSharedPreferenceChangeListener (Изменеия в настройках)
        val sp = PreferenceManager.getDefaultSharedPreferences(ctx)
        sp.registerOnSharedPreferenceChangeListener(this)

        //Если повернули экран или вернулись в активность, то открываем ту фазу, которая была, иначе - берем данные из SharedPreference
        curPhase = if (savedInstanceState != null) {
            savedInstanceState.getString("phase")
        } else {
            loadStartPhase()
        }
        //Чтобы при запуске активности в onResume не пришлось менять фазу
        changedPhase = curPhase

        Log.v (TAG, "MainActivity CreateView")
        setContentView(R.layout.activity_main)

        //номер текущей версии программы
        var version = sp.getInt("version", 1)
        val curVersion = BuildConfig.VERSION_CODE

        //Увеличиваем счетчик запусков программы
        var count = sp.getInt("startcount", 0)
        // Увеличиваем число запусков программы на 1 и сохраняем результат.
        count++
        //если это первый запуск
        if (count == 1) {
            //выводим окно с приветствием
            alert(getString(R.string.first_start)) { okButton { } }.show()
            //и отменяем вывод окна что нового в данной версии
            version = curVersion
            curPhase = "BEGIN"
            saveInt2SP(curVersion,"version",ctx)
        }
        saveInt2SP(count,"startcount",ctx)

        // проверяем версию программы в файле настроек, если она отлична от текущей, то выводим окно с описанием обновлений
        if (curVersion != version) { //если версии разные
            alert(getString(R.string.whatsnew)) { okButton { } }.show()
            saveInt2SP(curVersion,"version",ctx)
        }


        fragListView = FragmentListView.newInstance("BEGIN")
        when (curPhase) {
            "TIMER" -> {setFragment(FragmentTimerSettings.newInstance())}
            "SCRAMBLEGEN" -> {setFragment(FragmentScrambleGen.newInstance())}
            "TESTGAME" -> {setFragment(FragmentTestGameSettings.newInstance())}
            "BLINDGAME" -> {setFragment(FragmentBlindGameSettings.newInstance())}
            "SETTINGS" -> {setFragment(FragmentSettings.newInstance())}
            "ABOUT" -> {setFragment(FragmentAbout.newInstance())}
            "AZBUKA","AZBUKA2" -> {setFragment(FragmentAzbukaSelect.newInstance())}
            in listOfGo2Fridrich -> {
                fab.setImageResource(R.drawable.ic_fab_backward)
                setListFragmentPhase(curPhase)
            }
            in listOfOtherPuzzle -> {
                fab.setImageResource(R.drawable.ic_fab_backward)
                setListFragmentPhase(curPhase)
            }
            in listOfBasic -> {
                fab.setImageResource(R.drawable.ic_fab_backward)
                setListFragmentPhase(curPhase)
            }
            else -> { setListFragmentPhase(curPhase) }
        }

        nav_view.setNavigationItemSelectedListener(this)
        if (!sp.getBoolean("fab_on", true)) {
            fab.visibility = View.GONE
        } else {
            fab.visibility = View.VISIBLE
        }
        fab.setOnClickListener { _ ->
            when (curPhase) {
                in listOfGo2Fridrich -> {
                    curPhase = "G2F"
                    setListFragmentPhase(curPhase)
                    fab.setImageResource(R.drawable.ic_fab_forward)
                }
                in listOfOtherPuzzle -> {
                    curPhase = "OTHER"
                    setListFragmentPhase(curPhase)
                    fab.setImageResource(R.drawable.ic_fab_forward)
                }
                in listOfBasic -> {
                    curPhase = "BASIC"
                    setListFragmentPhase(curPhase)
                    fab.setImageResource(R.drawable.ic_fab_forward)
                }
                "AZBUKA" -> {
                    curPhase = "SCRAMBLEGEN"
                    setFragment(FragmentScrambleGen.newInstance())
                    fab.setImageResource(R.drawable.ic_fab_forward)
                }
                "AZBUKA2" -> {
                    curPhase = "BLINDGAME"
                    setFragment(FragmentScrambleGen.newInstance())
                    fab.setImageResource(R.drawable.ic_fab_forward)
                }

                else -> {
                    drawer_layout.openDrawer(GravityCompat.START)
                }
            }
        }
        setSupportActionBar(maintoolbar)
        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, maintoolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        val favList = mListPagerLab.getPhaseList("FAVORITES")
        val rightDrawerAdapter = MyListAdapter(favList,1.3F)

        // подключим адаптер для выезжающего справа списка
        val rightDrawerListView  = findViewById<ListView>(R.id.main_right_drawer)
        rightDrawerListView.adapter = rightDrawerAdapter
        rightDrawerListView.setOnItemClickListener { _, _, i, _ ->
            val changedId = favList[i].id
            changedPhase = favList[i].url
            setListFragmentPhase(changedPhase)
            startActivity<SlidingTabsActivity>(RUBIC_PHASE to changedPhase, EXTRA_ID to changedId)
            drawer_layout.closeDrawer(GravityCompat.END)
        }

        //TODO проверить payCoins, и кол-во запусков программы. Если не платил, то
        //вывести предложение заплатить.

        //Для данной программы не актуально, т.к. пользователь ничего в программе по сути не покупает
        //но если бы нужно было отключение рекламы, то данный вызов обязателен
        loadDataFromPlayMarket()
    }

    private fun setFragment (fragment: Fragment) {
        val transaction : FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame_container, fragment).commit()
    }

    override fun onBackPressed() {
        when (curPhase) {
            in listOfGo2Fridrich -> {
                curPhase = "G2F"
                setListFragmentPhase(curPhase)
                fab.setImageResource(R.drawable.ic_fab_forward)
            }
            in listOfOtherPuzzle -> {
                curPhase = "OTHER"
                setListFragmentPhase(curPhase)
                fab.setImageResource(R.drawable.ic_fab_forward)
            }
            in listOfBasic -> {
                curPhase = "BASIC"
                setListFragmentPhase(curPhase)
                fab.setImageResource(R.drawable.ic_fab_forward)
            }
            "AZBUKA" -> {
                curPhase = "SCRAMBLEGEN"
                setFragment(FragmentScrambleGen.newInstance())
                fab.setImageResource(R.drawable.ic_fab_forward)
            }
            "AZBUKA2" -> {
                curPhase = "BLINDGAME"
                setFragment(FragmentBlindGameSettings.newInstance())
                fab.setImageResource(R.drawable.ic_fab_forward)
            }

            else -> {
                if (drawer_layout.isDrawerOpen(GravityCompat.END)) {
                    drawer_layout.closeDrawer(GravityCompat.END)
                } else {
                    if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
                        if (backPressedTime + 500 > System.currentTimeMillis()) {
                            super.onBackPressed()
                        } else {
                            toast("Нажмите еще раз для выхода")
                            backPressedTime = System.currentTimeMillis()
                        }
                    } else {
                        drawer_layout.openDrawer(GravityCompat.START)
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        savedInstanceState.putString("phase", curPhase)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        Log.v (TAG, "onOptionsItemSelected - curPhase - $curPhase")
        when (item.itemId) {
            R.id.action_help -> {
                when (curPhase) {
                    "BEGIN2X2" -> {
                        alert(getString(R.string.help_begin2x2)) { okButton { } }.show()
                    }
                    "ADV2X2" -> {
                        alert(getString(R.string.help_adv2x2)) { okButton { } }.show()
                    }
                    "BEGIN" -> {
                        alert(getString(R.string.help_begin)) { okButton { } }.show()
                    }
                    "G2F" -> {
                        alert(getString(R.string.help_g2f)) { okButton { } }.show()
                    }
                    "ACCEL" -> {
                        alert(getString(R.string.help_accel)) { okButton { } }.show()
                    }
                    "CROSS" -> {
                        alert(getString(R.string.help_cross)) { okButton { } }.show()
                    }
                    "F2L" -> {
                        alert(getString(R.string.help_f2l)) { okButton { } }.show()
                    }
                    "ADVF2L" -> {
                        alert(getString(R.string.help_advf2l)) { okButton { } }.show()
                    }
                    "OLL" -> {
                        alert(getString(R.string.help_oll)) { okButton { } }.show()
                    }
                    "PLL" -> {
                        alert(getString(R.string.help_pll)) { okButton { } }.show()
                    }
                    "BLIND" -> {
                        alert(getString(R.string.help_blind)) { okButton { } }.show()
                    }
//                    "BLINDACC" -> {
//                        alert(getString(R.string.help_blind_acc)) { okButton { } }.show()
//                    }
                    "BEGIN4X4" -> {
                        alert(getString(R.string.help_begin4x4)) { okButton { } }.show()
                    }
                    "BEGIN5X5" -> {
                        alert(getString(R.string.help_begin5x5)) { okButton { } }.show()
                    }

                    "PYRAMINX" -> {
                        alert(getString(R.string.help_pyraminx)) { okButton { } }.show()
                    }
                    "MEGAMINX" -> {
                        alert(getString(R.string.help_megaminx)) { okButton { } }.show()
                    }
                    "SKEWB" -> {
                        alert(getString(R.string.help_skewb)) { okButton { } }.show()
                    }

                    "TIMER" -> {
                        alert(getString(R.string.help_timer)) { okButton { } }.show()
                    }
                    "SCRAMBLEGEN" -> {
                        alert(getString(R.string.help_scramble_gen)) { okButton { } }.show()
                    }
                    "TESTGAME" -> {
                        alert(getString(R.string.help_test_game)) { okButton { } }.show()
                    }
                    "BASIC" -> {
                        alert(getString(R.string.help_basic)) { okButton { } }.show()
                    }
                    "BLINDGAME" -> {
                        alert(getString(R.string.help_blind_game)) { okButton { } }.show()
                    }
                    "SETTINGS" -> {
                        alert(getString(R.string.help_settings)) { okButton { } }.show()
                    }
                    "THANKS" -> {
                        alert(getString(R.string.help_thanks)) { okButton { } }.show()
                    }
                    "ABOUT" -> {
                        alert {
                            customView {
                                scrollView {
                                    textView {
                                        text = getString(R.string.history)
                                        textSize = 12F
                                    }.lparams {margin = dip (8)}
                                }
                            }
                            okButton { }
                        }.show()
                    }
                }
                return true
            }
            R.id.main_favorite -> {
                drawer_layout.openDrawer(GravityCompat.END)
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        // Handle navigation view item clicks here.
        Log.v(DebugTag.TAG, "NavigationItemSelected $item.itemId")
        when (item.itemId) {
            R.id.begin2x2 -> { setListFragmentPhase("BEGIN2X2") }

            R.id.adv2x2 -> { setListFragmentPhase("ADV2X2") }

            R.id.begin -> { setListFragmentPhase("BEGIN") }

            R.id.g2f -> { setListFragmentPhase("G2F") }

            R.id.blind -> { setListFragmentPhase("BLIND") }

//            R.id.blind_acc -> { setListFragmentPhase("BLINDACC") }

//            R.id.begin4x4 -> { setListFragmentPhase("BEGIN4X4")}

            R.id.other_puzzle -> {setListFragmentPhase("OTHER")}

            R.id.timer -> {
                setFragment(FragmentTimerSettings.newInstance())
                saveStartPhase("TIMER")
            }
            R.id.scramble -> {
                setFragment(FragmentScrambleGen.newInstance())
                saveStartPhase("SCRAMBLEGEN")
            }
            R.id.pll_game -> {
                setFragment(FragmentTestGameSettings.newInstance())
                saveStartPhase("TESTGAME")
            }

            R.id.blind_game -> {
                setFragment(FragmentBlindGameSettings.newInstance())
                saveStartPhase("BLINDGAME")
            }

            R.id.basic_move -> { setListFragmentPhase("BASIC") }

            R.id.settings -> {
                setFragment(FragmentSettings.newInstance())
                curPhase = "SETTINGS"
            }

            R.id.thanks -> {
                setListFragmentPhase("THANKS")
                curPhase = "THANKS"
            }

            R.id.about -> {
                setFragment(FragmentAbout.newInstance())
                curPhase = "ABOUT"
            }

            R.id.exit -> {
                finish()
            }
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onPause() {
        super.onPause()
        Log.v (TAG, "onPause - curPhase - $curPhase")
        changedPhase = curPhase
    }

    override fun onResume() {
        super.onResume()
        Log.v (TAG, "onResume - curPhase - $curPhase")
        if (changedPhase != curPhase) {
            Log.v(DebugTag.TAG, "Change Phase from $curPhase to $changedPhase")
            setListFragmentPhase(changedPhase)
            startActivity<SlidingTabsActivity>(RUBIC_PHASE to changedPhase, EXTRA_ID to changedId)
        }
    }


    private fun loadStartPhase():String {
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        return sp.getString("startPhase", "BEGIN")
    }

    private fun saveStartPhase(phase:String) {
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = sp.edit()
        editor.putString("startPhase", phase)
        editor.apply() // подтверждаем изменения
        curPhase = phase
    }

    private fun setListFragmentPhase(phase: String){

        curPhase = phase
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame_container, FragmentListView.newInstance(curPhase)).commit()
        saveStartPhase(curPhase)
    }

    //Обработка выбора пункта меню в листвью.
    override fun onListViewInteraction(phase:String, id:Int) {
        //Обработка событий из FragmentListView
        Log.v(DebugTag.TAG, "onListViewInteraction Start, $phase, $id")
        val lp = mListPagerLab.getPhaseItem(id,phase)
        when (phase) {
            //Если в листвью "Основные движения", то показать "тост",
            "BASIC3X3", "BASIC_PYR", "BASIC_SKEWB", "BASIC4X4", "BASIC5X5" -> {
                toast(getString(lp.description))
            }
            //Если меню "переходим на Фридрих" или "Другие головоломки", то меняем текст листвью на соответствующую фазу
            "BASIC","OTHER","G2F" -> {
                setListFragmentPhase(getString(lp.description))
                fab.setImageResource(R.drawable.ic_fab_backward)
            }
            //Если выбрали какое-то "Спасибо" автору
            "THANKS" -> {
                sayThanks(id)
            }
            //в других случаях запустить SlideTab с просмотром этапов
            else -> { startActivity<SlidingTabsActivity>(RUBIC_PHASE to phase, EXTRA_ID to id)}
        }
    }

    override fun onScrambleGenInteraction(button: String) {
        super.onScrambleGenInteraction(button)
        if (button == "AZBUKA") {
            setFragment(FragmentAzbukaSelect.newInstance())
            fab.setImageResource(R.drawable.ic_fab_backward)
            curPhase = "AZBUKA"
        }
    }

    override fun onBlindGameInteraction(button: String) {
        super.onBlindGameInteraction(button)
        if (button == "AZBUKA2") {
            setFragment(FragmentAzbukaSelect.newInstance())
            fab.setImageResource(R.drawable.ic_fab_backward)
            curPhase = "AZBUKA2"
        }
    }


    // Слушаем изменения в настройках программы
    override fun onSharedPreferenceChanged(sp: SharedPreferences, key: String?) {
        //Если изменилась тема в настройках, то меняем ее в программе
        when (key) {
            "theme" -> {
                Log.v(DebugTag.TAG, "Theme set to - ${sp.getString(key, "AppTheme")}")
                this.recreate()
            }
            "fab_on" -> {
                if (!sp.getBoolean(key, true)) {
                    fab.visibility = View.GONE
                } else {
                    fab.visibility = View.VISIBLE
                }
            }
            "screen_always_on" -> {
                setScreenOn(IS_SCREEN_ALWAYS_ON, ctx)
            }
            "startPhase" -> {
                val phase = sp.getString(key, "BEGIN")
                val id = sp.getInt("startId", 0)
                if (curPhase !=  phase) {
                    changedPhase = phase
                    changedId = id
                }
            }
        }
    }


    private fun setScreenOn (parameterName: String, context: Context){
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        val isScreenAlwaysOn = sp.getBoolean(parameterName, false)
        if (isScreenAlwaysOn) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    /**
     *  Далее все для покупок внутри приложения
     *  основано на гугловском стандартном приложении TrivialDrive
     *  File - New - ImportSamle - TrivialDrive
     */

    private fun sayThanks( donationNumber : Int ) {
        val donationString = when (donationNumber) {
            1 -> { MEDIUM_DONATION }
            2 -> { BIG_DONATION }
            else -> {SMALL_DONATION }
        }

        Log.d(TAG, "Pay button clicked; launching purchase flow for pay.")
        setWaitScreen(true)
        /* TO DO: for security, generate your payload here for verification. See the comments on
                 *        verifyDeveloperPayload() for more info. Since this is a SAMPLE, we just use
                 *        an empty string, but on a production app you should carefully generate this. */
        val payload2 = ""

        if (mGooglePlayOK) {
            try {
                mHelper!!.launchPurchaseFlow(this, donationString, RC_REQUEST,
                        mPurchaseFinishedListener, payload2)
            } catch (e: IabHelper.IabAsyncInProgressException) {
                complain("Ошибка запуска потока оплаты. Другая асинхронная операция запущена.")
                setWaitScreen(false)
            }
        } else {
            complain("Пожалуйста обновите Google Play App до последней версии ")
            setWaitScreen(false)
        }
    }

    private fun loadDataFromPlayMarket() {
        // load game data from PlayMarket
        loadData()
        // Создаем helper, передаем context и public key to verify signatures with
        Log.d(TAG, "Creating IAB helper.")
        mHelper = IabHelper(ctx, base64EncodedPublicKey)

        //TO DO enable debug logging (Для полноценной версии надо поставить в false).
        mHelper!!.enableDebugLogging(false)

        // Start setup. This is asynchronous and the specified listener
        // will be called once setup completes.
        Log.d(TAG, "Starting setup.")
        mGooglePlayOK = true // изначально считаем что все ОК
        mHelper!!.startSetup(IabHelper.OnIabSetupFinishedListener { result ->
            Log.d(TAG, "Setup finished.")

            if (!result.isSuccess) {
                // Хьюстон, у нас проблемы с Google Play
                //complain("Пожалуйста обновите Google Play App до последней версии "); // + result
                mGooglePlayOK = false
                return@OnIabSetupFinishedListener
            }

            // Have we been disposed of in the meantime? If so, quit.
            if (mHelper == null) return@OnIabSetupFinishedListener

            // Важно: Динамически сгенерированный слушатель броадкастовых сообщений о покупках.
            // Создаем его динамически, а не через <receiver> in the Manifest
            // потому что мы всегда вызываем getPurchases() при старте программы, этим мы можем
            // игнорировать любые броадкасты пока приложение не запущено.
            // Note: registering this listener in an Activity is a bad idea, but is done here
            // because this is a SAMPLE. Regardless, the receiver must be registered after
            // IabHelper is setup, but before first call to getPurchases().
            mBroadcastReceiver = IabBroadcastReceiver(this@MainActivity)
            val broadcastFilter = IntentFilter(IabBroadcastReceiver.ACTION)
            registerReceiver(mBroadcastReceiver, broadcastFilter)

            // IAB is fully set up. Now, let's get an inventory of stuff we own.
            Log.d(TAG, "Setup successful. Querying inventory.")
            try {
                mHelper!!.queryInventoryAsync(mGotInventoryListener)
            } catch (e: IabHelper.IabAsyncInProgressException) {
                complain("Error querying inventory. Another async operation in progress.")
            }
        })
    }

    // We're being destroyed. It's important to dispose of the helper here!
    override fun onDestroy() {
        super.onDestroy()

        // very important:
        if (mBroadcastReceiver != null) {
            unregisterReceiver(mBroadcastReceiver)
        }

        // very important:
        Log.d(TAG, "Destroying helper.")
        if (mHelper != null) {
            mHelper!!.disposeWhenFinished()
            mHelper = null
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        Log.d(TAG, "onActivityResult($requestCode,$resultCode,$data")
        if (mHelper == null) return

        // Pass on the activity result to the helper for handling
        if (!mHelper!!.handleActivityResult(requestCode, resultCode, data)) {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
            super.onActivityResult(requestCode, resultCode, data)
        } else {
            Log.d(TAG, "onActivityResult handled by IABUtil.")
        }
    }

    override fun receivedBroadcast() {
        // Received a broadcast notification that the inventory of items has changed
        Log.d(TAG, "Received broadcast notification. Querying inventory.")
        try {
            mHelper!!.queryInventoryAsync(mGotInventoryListener)
        } catch (e: IabHelper.IabAsyncInProgressException) {
            complain("Error querying inventory. Another async operation in progress.")
        }

    }

    /** Verifies the developer payload of a purchase.  */
    private fun verifyDeveloperPayload(p: Purchase): Boolean {
        //val payload = p.developerPayload
        // тут можно проверить ответ от гугла, но нам в данном случае фиолетово,
        // ибо пользователь за свою покупку ничего в приложении получить не должен
        return true
    }

    // Callback for when a purchase is finished
    private var mPurchaseFinishedListener: IabHelper.OnIabPurchaseFinishedListener = IabHelper.OnIabPurchaseFinishedListener { result, purchase ->
        Log.d(TAG, "Покупка завершена: $result, куплено: $purchase")

        // if we were disposed of in the meantime, quit.
        if (mHelper == null) return@OnIabPurchaseFinishedListener

        if (result.isFailure) {
            complain("Ошибка покупки: " + result)
            setWaitScreen(false)
            return@OnIabPurchaseFinishedListener
        }
        if (!verifyDeveloperPayload(purchase)) {
            complain("Ошибка покупки. Ошибка авторизации.")
            setWaitScreen(false)
            return@OnIabPurchaseFinishedListener
        }

        Log.d(TAG, "Покупка прошла успешно.")

        when {
            purchase.sku == SMALL_DONATION -> {
                // Пользователь задонатил 50 руб.
                Log.d(TAG, "Покупка = донат 50 руб.")
                alert("Спасибо за поддержку")
                mCoins += 50
            }
            purchase.sku == MEDIUM_DONATION -> {
                // Пользователь задонатил 100 руб.
                Log.d(TAG, "Покупка = донат 100 руб.")
                alert("Большое спасибо за поддержку")
                mIsPremium = true
                mCoins += 100
            }
            purchase.sku == BIG_DONATION -> {
                // Пользователь задонатил 200 руб.
                Log.d(TAG, "Покупка = донат 200 руб.")
                alert("Огромное спасибо за поддержку")
                mIsPremium = true
                mCoins += 200
            }
        }
        saveData()
        setWaitScreen(false)
    }

    // Called when consumption is complete
    private var mConsumeFinishedListener: IabHelper.OnConsumeFinishedListener = IabHelper.OnConsumeFinishedListener { purchase, result ->
        Log.d(TAG, "Consumption finished. Purchase: $purchase, result: $result")

        // if we were disposed of in the meantime, quit.
        if (mHelper == null) return@OnConsumeFinishedListener

        setWaitScreen(false)
        Log.d(TAG, "End consumption flow.")
    }


    // Слушатель, который вызывается, когда мы законичили запрос к серверу о купленных товарах
    private var mGotInventoryListener: IabHelper.QueryInventoryFinishedListener = IabHelper.QueryInventoryFinishedListener { result, inventory ->
        Log.d(TAG, "Query inventory finished.")

        // Хелпер был ликвидирован? If so, выходим.
        if (mHelper == null) return@QueryInventoryFinishedListener

        // Не получилось?
        if (result.isFailure) {
            complain("Failed to query inventory: " + result)
            return@QueryInventoryFinishedListener
        }

        Log.d(TAG, "Query inventory was successful.")

        /*
         * Проверяем купленные товары. Обратите внимание, что проверяем для каждой покупки
         * to see if it's correct! See verifyDeveloperPayload().
         */

        // Пример пока нам ненужного функционала, поэтому закоментировал
        // Проверяем, платил ли пользователь уже 100руб.
//        val premiumPurchase = inventory.getPurchase(MEDIUM_DONATION)
//        mIsPremium = premiumPurchase != null && verifyDeveloperPayload(premiumPurchase)
//        Log.d(TAG, "User is " + if (mIsPremium) "PREMIUM" else "NOT PREMIUM")
//
//        // Проверяем платил ли 50 руб
//        val gasPurchase = inventory.getPurchase(SMALL_DONATION)

        setWaitScreen(false)
        Log.d(TAG, "Initial inventory query finished; enabling main UI.")
    }

    // Enables or disables the "please wait" screen.
    private fun setWaitScreen(set: Boolean) {
        findViewById<FrameLayout>(R.id.frame_container).visibility = if (set) View.GONE else View.VISIBLE
        findViewById<ImageView>(R.id.screen_wait).visibility = if (set) View.VISIBLE else View.GONE
    }

    private fun complain(message: String) {
        Log.e(TAG, "**** TrivialDrive Error: " + message)
        alert("Error: " + message)
    }

    private fun alert(message: String) {
        val bld = AlertDialog.Builder(this)
        bld.setMessage(message)
        bld.setNeutralButton("OK", null)
        Log.d(TAG, "Showing alert dialog: " + message)
        bld.create().show()
    }

    private fun saveData() {
        val spe = getPreferences(MODE_PRIVATE).edit()
        spe.putInt("payCoins", mCoins)
        spe.apply()
        Log.d(TAG, "Saved data: tank = " + mCoins.toString())
    }

    private fun loadData() {
        val sp = getPreferences(MODE_PRIVATE)
        mCoins = sp.getInt("payCoins", 0)
        Log.d(TAG, "Пользователь уже оплатил = " + mCoins.toString())
    }

}
