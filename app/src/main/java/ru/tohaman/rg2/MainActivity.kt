/*
 * Copyright (C) 2019 Rozov Anton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.tohaman.rg2

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.graphics.Typeface
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
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import ru.tohaman.rg2.DeveloperKey.base64EncodedPublicKey
import ru.tohaman.rg2.activities.F2LPagerActivity
import ru.tohaman.rg2.activities.MyDefaultActivity
import ru.tohaman.rg2.adapters.MyListAdapter
import ru.tohaman.rg2.data.ListPager
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
const val DELAY_MILLS = "delayMills"                //время в милисек. до возможности старта таймера, с момента прикосновения
const val IS_SCRAMBLE_VISIBLE = "isScrambleOnTimerVisible"
const val TEST_GAME_ROW_COUNT = "pllTestRowCount"
const val BLIND_ROW_COUNT = "blindRowCount"
const val PLL_TEST_3SIDE = "isPllTest3Side"
const val OLL_TEST_GAME = "isOllGame"
const val BLIND_IS_EDGE_CHECKED = "isBlindEdgeChecked"
const val BLIND_IS_CORNER_CHECKED = "isBlindCornerChecked"
const val FAVORITES = "favorites"
const val DEFAULT_DRAWABLE_SIZE = 1
const val SCRAMBLE = "scramble"
const val SCRAMBLE_LEN = "scrambleLength"
const val CHK_BUF_EDGES = "checkEdgesBuffer"
const val CHK_BUF_CORNERS = "checkCornersBuffer"
const val CHK_SHOW_SOLVE = "checkShowSolve"

// SKUs для продуктов: при изменении не забыть поправить в sayThanks
const val BIG_DONATION = "big_donation"
const val MEDIUM_DONATION = "medium_donation"
const val SMALL_DONATION = "small_donation"

class MainActivity : MyDefaultActivity(),
        NavigationView.OnNavigationItemSelectedListener,
        FragmentListView.OnListViewInteractionListener,
        FragmentScrambleGen.OnScrambleGenInteractionListener,
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
    private lateinit var favList: ArrayList<ListPager>
    private var listOfSubmenu = arrayListOf<String>()
    private var listOfOllMenu = arrayListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        //Сохраним в переменную 50 руб, чтобы каждые 30 запусков не выводилось окно "оплатить приложение"
        //а то разработчик, даже если хочет, то не может этого сделать. Нужно запустить только 1 раз.
        //mCoins = 50
        //saveData()

        //Для данной программы не актуально, т.к. пользователь ничего в программе по сути не покупает
        //но если бы нужно было отключение рекламы, то данный вызов обязателен
        loadDataFromPlayMarket()

        Log.v (TAG, "MainActivity ListPagerLab init.")
        mListPagerLab = ListPagerLab.get(this)

        //получаем список фаз submenu, чтобы корректно отрабатывать нажатие кнопки назад
        listOfSubmenu = mListPagerLab.getSubmenu(this)
        listOfOllMenu = mListPagerLab.getOllMenu(this)


        // Регистрируем слушатель OnSharedPreferenceChangeListener (Изменеия в настройках)
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        sp.registerOnSharedPreferenceChangeListener(this)

        //Если повернули экран или вернулись в активность, то открываем ту фазу, которая была, иначе - берем данные из SharedPreference
        curPhase = if (savedInstanceState != null) {
            savedInstanceState.getString("phase").let { it!! }
        } else {
            loadStartPhase()
        }

        Log.v (TAG, "MainActivity CreateView")
        setContentView(R.layout.activity_main)

        //номер текущей версии программы
        val version = sp.getInt("version", BuildConfig.VERSION_CODE)
        val curVersion = BuildConfig.VERSION_CODE

        //Увеличиваем счетчик запусков программы
        var startCount = sp.getInt("startcount", 0)
        // Увеличиваем число запусков программы на 1 и сохраняем результат.
        startCount++
        //если это первый запуск
        if (startCount == 1) {
            //выводим окно с приветствием
            alert(getString(R.string.first_start)) { okButton { } }.show()
            //и отменяем вывод окна что нового в данной версии
            curPhase = "MAIN3X3"
            saveInt2SP(curVersion,"version",this)
        }
        saveInt2SP(startCount,"startcount",this)

        // проверяем версию программы в файле настроек, если она отлична от текущей, то выводим окно с описанием обновлений
        if (curVersion != version) { //если версии разные
            updateVersion(version, curVersion)
        } else {
            //Проверяем платил ли уже пользователь, если не платил, то каждый 25-ый вход
            //напоминаем сказать спасибо.
            if ((mCoins == 0) and (startCount % 25 == 0)) {
                curPhase = "THANKS"
                alert(getString(R.string.help_thanks)) { okButton { } }.show()
            } else {
                //Если не подписан на канал, то выводим окно с просьбой подписаться на канал.
                val subscribe = sp.getBoolean("subscribe", false)
                if ((!subscribe) and (startCount % 9 == 0)) {
                    alert( R.string.subscibeText) {
                        positiveButton(R.string.subscibeOK) {
                            browse("https://www.youtube.com/channel/UCpSUF7w376aCRRvzkoNoAfQ")
                            saveBoolean2SP(true,"subscribe", baseContext)
                        }
                        negativeButton(R.string.subscibeNotOK) {
                            saveBoolean2SP(true,"subscribe", baseContext)
                        }
                        neutralPressed (R.string.remindMeLater) {
                        }
                    }.show()
                }
            }
        }

        //Чтобы при запуске активности в onResume не пришлось менять фазу
        changedPhase = curPhase

        fragListView = FragmentListView.newInstance("BEGIN")
        when (curPhase) {
            "TIMER" -> {setFragment(FragmentTimerSettings.newInstance())}
            "SCRAMBLEGEN" -> {setFragment(FragmentScrambleGen.newInstance())}
            "TESTGAME" -> {setFragment(FragmentTestGameSettings.newInstance())}
            "BLINDGAME" -> {setFragment(FragmentBlindGameSettings.newInstance())}
            "SETTINGS" -> {setFragment(FragmentSettings.newInstance())}
            "ABOUT" -> {setFragment(FragmentAbout.newInstance())}
            "AZBUKA","AZBUKA2" -> {setFragment(FragmentAzbukaSelect.newInstance())}
            in listOfSubmenu -> {
                fab.setImageResource(R.drawable.ic_fab_backward)
                setListFragmentPhase(curPhase)
            }
            in listOfOllMenu -> {
                curPhase = mListPagerLab.getBackPhase(curPhase, this)
                changedPhase = curPhase
            }
            else -> { setListFragmentPhase(curPhase) }
        }

        nav_view.setNavigationItemSelectedListener(this)
        if (!sp.getBoolean("fab_on", false)) {
            fab.hide()
        } else {
            fab.show()
        }
        fab.setOnClickListener {
            val backPhase = mListPagerLab.getBackPhase(curPhase, this)
            if (backPhase == "") {
                when (curPhase) {
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
            } else {
                curPhase = backPhase
                setListFragmentPhase(curPhase)
                fab.setImageResource(R.drawable.ic_fab_forward)
            }

        }
        setSupportActionBar(maintoolbar)
        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, maintoolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        favList = mListPagerLab.getPhaseList("FAVORITES","0")
        val rightDrawerAdapter = MyListAdapter(favList,1.3F)

        // подключим адаптер для выезжающего справа списка
        val rightDrawerListView= findViewById<ListView>(R.id.main_right_drawer)
        rightDrawerListView.adapter = rightDrawerAdapter
        rightDrawerListView.setOnItemClickListener { _, _, i, _ ->
            val changedId = favList[i].id
            changedPhase = favList[i].url
            if (changedPhase in listOfOllMenu) {
                startActivity<F2LPagerActivity>(RUBIC_PHASE to changedPhase, EXTRA_ID to changedId)
                setListFragmentPhase(mListPagerLab.getBackPhase(changedPhase, this))
            } else {
                setListFragmentPhase(changedPhase)
                startActivity<SlidingTabsActivity>(RUBIC_PHASE to changedPhase, EXTRA_ID to changedId)
            }
            drawer_layout.closeDrawer(GravityCompat.END)
        }

    }

    private fun updateVersion(fromVersion: Int, toVersion: Int) {
        alert(getString(R.string.whatsnew)) { okButton { } }.show()
        saveInt2SP(toVersion, "version", this)
        //Тут можно указать фазу новинки, чтобы после обновления программы, открылась новинка.
        curPhase = "KEYHOLE"
        if (fromVersion < 68) { updateComment68()}
        if (fromVersion < 79) { updateComment79()}
        if (fromVersion < 86) { update86() }
     }

    private fun updateComment68() {
        //Исправление ошибки в комментах к узорам
        val pattern = mListPagerLab.getPhaseItem(1, "PATTERNS")
        if (pattern.comment == "S' M' S M") {
            pattern.comment = "M2 S2 E2"
            mListPagerLab.updateListPager(pattern)
        }
    }

    private fun updateComment79() {
        //Обновляем комментарии к узорам, т.к. в 79 версии меняем порядок следования узоров
        //Меняем коммент для 7 узора с (U F B\' L2 U2 L2 F\' B U2 L2 U) на (F2 R2 D R2 D U F2 D\' R\' D\' F L2 F\' D R U\')
        val pattern = mListPagerLab.getPhaseItem(6, "PATTERNS")
        if (pattern.comment == "U F B' L2 U2 L2 F' B U2 L2 U") {
            pattern.comment = "F2 R2 D R2 D U F2 D' R' D' F L2 F' D R U'"
            mListPagerLab.updateListPager(pattern)
        }
    }

    private fun update86() {
        alert("Дорогие друзья, проекту очень требуется ваша помощь. Если вы умеете " +
                "программировать на Kotlin или Swift и у вас есть желание помочь - пожалуйста, " +
                "напишите мне на почту") {
            positiveButton("OK") {
            }
            negativeButton("Написать") {
                email("rubicsguide@yandex.ru", "Помощь проекту", "Добрый день, Антон.\n")
            }
        }.show()
    }

    private fun setFragment (fragment: Fragment) {
        val transaction : FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame_container, fragment).commit()
    }

    override fun onBackPressed() {
        val backPhase = mListPagerLab.getBackPhase(curPhase, this)

        if (backPhase == "") {
            when (curPhase) {
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
        } else {
            curPhase = backPhase
            setListFragmentPhase(curPhase)
            fab.setImageResource(R.drawable.ic_fab_forward)
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
                    "MAIN3X3" -> {
                        alert(getString(R.string.help_main3x3)) { okButton { } }.show()
                    }
                    "BEGIN_BNDR" -> {
                        alert(getString(R.string.help_bondarenko)) { okButton { } }.show()
                    }
                    "MAIN_F2L" -> {
                        alert(getString(R.string.help_mainF2L)) { okButton { } }.show()
                    }
                    "MAIN2X2" -> {
                        alert(getString(R.string.help_main2x2)) { okButton { } }.show()
                    }
                    "OTHER3X3" -> {
                        alert(getString(R.string.help_other3x3)) { okButton { } }.show()
                    }
                    "MIRROR" -> {
                        alert(getString(R.string.help_mirror)) { okButton { } }.show()
                    }
                    "AXIS" -> {
                        alert(getString(R.string.help_axis)) { okButton { } }.show()
                    }
                    "PYRAMORPHIX" -> {
                        alert(getString(R.string.help_pyramorphix)) { okButton { } }.show()
                    }
                    "SUDOKU" -> {
                        alert(getString(R.string.help_sudoku)) { okButton { } }.show()
                    }
                    "WINDMILL" -> {
                        alert(getString(R.string.help_windmill)) { okButton { } }.show()
                    }
                    "FISHER" -> {
                        alert(getString(R.string.help_fisher)) { okButton { } }.show()
                    }
                    "PRISMA" -> {
                        alert(getString(R.string.help_prisma)) { okButton { } }.show()
                    }
                    "CYLINDER" -> {
                        alert(getString(R.string.help_cylinder)) { okButton { } }.show()
                    }
                    "GEAR" -> {
                        alert(getString(R.string.help_gear)) { okButton { } }.show()
                    }

                    "ADV2X2" -> {
                        alert(getString(R.string.help_adv2x2)) { okButton { } }.show()
                    }
                    "CLL" -> {
                        alert(getString(R.string.help_cll)) { okButton { } }.show()
                    }
                    "ORTEGA" -> {
                        alert(getString(R.string.help_ortega)) { okButton { } }.show()
                    }

                    "BEGIN" -> {
                        alert(getString(R.string.help_begin)) { okButton { } }.show()
                    }
                    "ROZOV" -> {
                        alert(getString(R.string.help_begin_rozov)) { okButton { } }.show()
                    }

                    "G2F" -> {
                        alert(getString(R.string.help_g2f)) { okButton { } }.show()
                    }
                    "RECOMEND" -> {
                        alert(getString(R.string.help_recomend)) { okButton { } }.show()
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
                    "COLL" -> {
                        alert(getString(R.string.help_coll)) { okButton { } }.show()
                    }
                    "ROUX" -> {
                        alert(getString(R.string.help_roux)) { okButton { } }.show()
                    }
                    "PATTERNS" -> {
                        alert(getString(R.string.help_patterns)) { okButton { } }.show()
                    }
                    "BEGIN4X4" -> {
                        alert(getString(R.string.help_begin4x4)) { okButton { } }.show()
                    }
                    "BIG_MAIN" -> {
                        alert(getString(R.string.help_big_main)) { okButton { } }.show()
                    }
                    "YAU4X4" -> {
                        alert(getString(R.string.help_yau4x4)) { okButton { } }.show()
                    }
                    "BIG_CUBES" -> {
                        alert(getString(R.string.help_big_cubes)) { okButton { } }.show()
                    }

                    "OTHER" -> {
                        alert(getString(R.string.help_other)) { okButton { } }.show()
                    }
                    "BEGIN5X5" -> {
                        alert(getString(R.string.help_begin5x5)) { okButton { } }.show()
                    }

                    "MAIN_PYRAMINX" -> {
                        alert(getString(R.string.help_main_pyraminx)) { okButton { } }.show()
                    }

                    "PYRAMINX" -> {
                        alert(getString(R.string.help_pyraminx)) { okButton { } }.show()
                    }
                    "KEYHOLE" -> {
                        alert(getString(R.string.help_keyhole)) { okButton { } }.show()
                    }

                    "MEGAMINX" -> {
                        alert(getString(R.string.help_megaminx)) { okButton { } }.show()
                    }
                    "MAIN_SKEWB" -> {
                        alert(getString(R.string.help_main_skewb)) { okButton { } }.show()
                    }

                    "SKEWB" -> {
                        alert(getString(R.string.help_skewb)) { okButton { } }.show()
                    }
                    "TW_SKEWB" -> {
                        alert(getString(R.string.help_tw_skewb)) { okButton { } }.show()
                    }
                    "IVY" -> {
                        alert(getString(R.string.help_ivy)) { okButton { } }.show()
                    }
                    "REDI" -> {
                        alert(getString(R.string.help_redi)) { okButton { } }.show()
                    }
                    "CLOVER" -> {
                        alert(getString(R.string.help_clover)) { okButton { } }.show()
                    }
                    "SQUARE" -> {
                        alert(getString(R.string.help_square)) { okButton { } }.show()
                    }
                    "SQ_STAR" -> {
                        alert(getString(R.string.help_sq_star)) { okButton { } }.show()
                    }
                    "CUB2X2X3" -> {
                        alert(getString(R.string.help_cub_2x2x2)) { okButton { } }.show()
                    }
                    "PENTACLE" -> {
                        alert(getString(R.string.help_pentacle)) { okButton { } }.show()
                    }
                    "CONTAINER" -> {
                        alert(getString(R.string.help_container)) { okButton { } }.show()
                    }

                    "AZBUKA" -> {
                        alert(getString(R.string.help_azbuka)) { okButton { } }.show()
                    }
                    "AZBUKA2" -> {
                        alert(getString(R.string.help_azbuka)) { okButton { } }.show()
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
                    "BASIC3X3" -> {
                        alert(getString(R.string.help_basic_3x3)) { okButton { } }.show()
                    }
                    "BASIC4X4" -> {
                        alert(getString(R.string.help_basic_4x4)) { okButton { } }.show()
                    }
                    "BASIC5X5" -> {
                        alert(getString(R.string.help_basic_5x5)) { okButton { } }.show()
                    }
                    "BASIC_PYR" -> {
                        alert(getString(R.string.help_basic_pyr)) { okButton { } }.show()
                    }
                    "BASIC_SKEWB" -> {
                        alert(getString(R.string.help_basic_skewb)) { okButton { } }.show()
                    }
                    "BASIC_SQ1" -> {
                        alert(getString(R.string.help_basic_sq1)) { okButton { } }.show()
                    }
                    "BASIC_REDI" -> {
                        alert(getString(R.string.help_basic_redi)) { okButton { } }.show()
                    }
                    "BASIC_CLOVER" -> {
                        alert(getString(R.string.help_basic_clover)) { okButton { } }.show()
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
                    else -> {
                        if (curPhase.startsWith("search:", true)) {
                            alert(getString(R.string.help_search)) { okButton { } }.show()
                        } else {
                            alert(getString(R.string.help_missed)) {
                                positiveButton("Закрыть") { }
                                negativeButton("Отправить письмо автору") {
                                    email("rubicsguide@yandex.ru",
                                            "Отсутствует help",
                                            "Отсутствует help для $curPhase")
                                }
                            }.show()
                        }
                    }
                }
                return true
            }
            R.id.main_favorite -> {
                drawer_layout.openDrawer(GravityCompat.END)
                return true
            }
            R.id.main_search -> {
                alert {
                    customView {
                        verticalLayout {
                            padding = dip(10)
                            textView(R.string.searchTextViewText) {
                                textSize = 20f
                                padding = dip(10)
                                typeface = Typeface.DEFAULT_BOLD
                            }
                            val sp = PreferenceManager.getDefaultSharedPreferences(context)
                            val searchText = sp.getString("searchText", "")!!
                            val searchEditText = editText (searchText){
                                hint  = resources.getString(R.string.searchExamlpeHint)
                            }
                            positiveButton("OK") {
                                curPhase = "search:" + searchEditText.text.toString()
                                setListFragmentPhase(curPhase)
                                saveString2SP(searchEditText.text.toString(),"searchText",context)
                            }
                            negativeButton("Отмена") {}
                        }
                    }
                }.show()
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        Log.v(DebugTag.TAG, "NavigationItemSelected $item.itemId")
        when (item.itemId) {
            R.id.main2x2 -> { setListFragmentPhase("MAIN2X2") }

            R.id.main3x3 -> { setListFragmentPhase("MAIN3X3") }

            R.id.big_cubes -> { setListFragmentPhase("BIG_MAIN") }

            R.id.other3x3 -> { setListFragmentPhase("OTHER3X3") }

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
        favList = mListPagerLab.getPhaseList("FAVORITES")
        val rightDrawerAdapter = MyListAdapter(favList,1.3F)

        // подключим адаптер для выезжающего справа списка
        val rightDrawerListView  = findViewById<ListView>(R.id.main_right_drawer)
        rightDrawerListView.adapter = rightDrawerAdapter

        //Если фаза поменялась, то
        if (changedPhase != curPhase) {
            Log.v(DebugTag.TAG, "Change Phase from $curPhase to $changedPhase")
            //перейдем в основной активности на соответствующую фазу и откроем нужный слайдинг таб

            if (changedPhase in listOfOllMenu) {
                startActivity<F2LPagerActivity>(RUBIC_PHASE to changedPhase, EXTRA_ID to changedId)
                setListFragmentPhase(mListPagerLab.getBackPhase(changedPhase, this))
            } else {
                setListFragmentPhase(changedPhase)
                startActivity<SlidingTabsActivity>(RUBIC_PHASE to changedPhase, EXTRA_ID to changedId)
            }
        }
    }


    private fun loadStartPhase():String {
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        return sp.getString("startPhase", "BEGIN").let { it!! }
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
        when (lp.url) { //В зависимости от того, что прописано в url
            "thanks" -> {     //Если это меню "сказать спасибо"
                sayThanks(id)
            }
            "submenu" -> {  //Если это сабменю
                setListFragmentPhase(getString(lp.description))
                fab.setImageResource(R.drawable.ic_fab_backward)
            }
            "basic" -> {    //Если это движения и надо вывести тост с описанием действия
                toast(getString(lp.description))
            }
            "ollPager" -> {  //Если тип не submenu, а ollPager
                startActivity<F2LPagerActivity>(RUBIC_PHASE to (this.getString(lp.description)), EXTRA_ID to 0)
            }
            //В других случаях запускаем SlidingTabActivity
            else -> { startActivity<SlidingTabsActivity>(RUBIC_PHASE to lp.phase, EXTRA_ID to lp.id)}
        }
    }

    override fun onScrambleGenInteraction(button: String) {
        super.onScrambleGenInteraction(button)
        when (button) {
            "AZBUKA" -> {
                setFragment(FragmentAzbukaSelect.newInstance())
                fab.setImageResource(R.drawable.ic_fab_backward)
                curPhase = "AZBUKA"
            }
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
                    fab.hide()
                } else {
                    fab.show()
                }
            }
            "screen_always_on" -> {
                setScreenOn(IS_SCREEN_ALWAYS_ON, this)
            }
            "startPhase" -> {
                val phase = sp.getString(key, "BEGIN").let { it!! }
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
     *  File - New - ImportSample - TrivialDrive
     */

    private fun sayThanks( donationNumber : Int ) {
        if (donationNumber < 3) {
            //Донат через GooglePlay
            val donationString = when (donationNumber) {
                1 -> {
                    MEDIUM_DONATION
                }
                2 -> {
                    BIG_DONATION
                }
                else -> {
                    SMALL_DONATION
                }
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
        } else {
            //Донат на Яндекс.кошелек
            //startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://money.yandex.ru/to/410016716734895")))
            browse("https://money.yandex.ru/to/410016716734895")
            mCoins = 50
            saveData()
        }
    }

    private fun loadDataFromPlayMarket() {
        // load game data from PlayMarket
        loadData()
        // Создаем helper, передаем context и public key to verify signatures with
        Log.d(TAG, "Creating IAB helper.")
        mHelper = IabHelper(this, base64EncodedPublicKey)

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
                //complain("Пожалуйста обновите Google Play App до последней версии "); // + allComplete
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d(TAG, "onActivityResult($requestCode,$resultCode,$data")
        if (mHelper == null) return

        // Pass on the activity allComplete to the helper for handling
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

    /** Verifies the developer payload of a purchase.
     * * WARNING: Locally generating a random string when starting a purchase and
     * verifying it here might seem like a good approach, but this will fail in the
     * case where the user purchases an item on one device and then uses your app on
     * a different device, because on the other device you will not have access to the
     * random string you originally generated.*/
    private fun verifyDeveloperPayload( p : Purchase): Boolean {
        val payload = p.developerPayload
        Log.d (TAG, payload)
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
            complain("Ошибка покупки: $result")
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
        Log.d(TAG, "Consumption finished. Purchase: $purchase, allComplete: $result")

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
            complain("Failed to query inventory: $result")
            return@QueryInventoryFinishedListener
        }

        Log.d(TAG, "Query inventory was successful.")

        /*
         * Проверяем купленные товары. Обратите внимание, что проверяем для каждой покупки
         * to see if it's correct! See verifyDeveloperPayload().
         */

        // Проверяем, платил ли пользователь
        val smallDonat = inventory.getPurchase(SMALL_DONATION)
        val mediumDonat = inventory.getPurchase(MEDIUM_DONATION)
        val bigDonat = inventory.getPurchase(BIG_DONATION)
        mIsPremium = smallDonat != null &&
                     mediumDonat != null &&
                     bigDonat != null &&
                verifyDeveloperPayload(smallDonat) &&
                verifyDeveloperPayload(mediumDonat) &&
                verifyDeveloperPayload(bigDonat)

        //Если какая-то покупка была, а mCoins = 0, значит пользователь переустановил
        //приложение, но уже что-то покупал, считаем что это был маленький донат, хотя
        //можно было узнать и точно, но пока не принципиально
        if ((mCoins == 0) and (mIsPremium)) {
            mCoins = 50
            saveData()
        }
        setWaitScreen(false)
        Log.d(TAG, "Initial inventory query finished; enabling main UI.")
    }

    // Enables or disables the "please wait" screen.
    private fun setWaitScreen(set: Boolean) {
        findViewById<FrameLayout>(R.id.frame_container).visibility = if (set) View.GONE else View.VISIBLE
        findViewById<ImageView>(R.id.screen_wait).visibility = if (set) View.VISIBLE else View.GONE
    }

    private fun complain(message: String) {
        Log.e(TAG, "**** TrivialDrive Error: $message")
        alert("Error: $message")
    }

    private fun alert(message: String) {
        val bld = AlertDialog.Builder(this)
        bld.setMessage(message)
        bld.setNeutralButton("OK", null)
        Log.d(TAG, "Showing alert dialog: $message")
        bld.create().show()
    }

    private fun saveData() {
        val spe = getPreferences(MODE_PRIVATE).edit()
        spe.putInt("payCoins", mCoins)
        spe.apply()
        Log.d(TAG, "Saved data: Coins = " + mCoins.toString())
    }

    private fun loadData() {
        val sp = getPreferences(MODE_PRIVATE)
        mCoins = sp.getInt("payCoins", 0)
        Log.d(TAG, "Пользователь уже оплатил = " + mCoins.toString())
    }

}
