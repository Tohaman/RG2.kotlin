package ru.tohaman.rg3

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubeThumbnailLoader
import com.google.android.youtube.player.YouTubeThumbnailView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import org.jetbrains.anko.contentView
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import ru.tohaman.rg3.listpager.ListPagerLab
import ru.tohaman.rg3.DebugTag.TAG
import ru.tohaman.rg3.activitys.SlidingTabsActivity
import ru.tohaman.rg3.fragments.FragmentTimer
import ru.tohaman.rg3.fragments.FragmentTimerSettings
import ru.tohaman.rg3.fragments.ListViewFragment
import ru.tohaman.rg3.listpager.ListPager

// Статические переменные (верхнего уровня). Котлин в действии стр.77-78
const val EXTRA_ID = "ru.tohaman.rubicsguide.PHASE_ID"
const val RUBIC_PHASE = "ru.tohaman.rubicsguide.PHASE"
const val VIDEO_PREVIEW = "video_preview"   //наименования ключа для сохранения/извлечения значения из файла настроек
const val ONE_HAND_TO_START = "oneHandToStart"
const val METRONOM_ENABLED = "metronomEnabled"
const val METRONOM_TIME = "metronomTime"

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, ListViewFragment.OnFragmentInteractionListener {
    private lateinit var fragListView: ListViewFragment
    private lateinit var fragTimer: Fragment
    private var back_pressed_time: Long = 0
    lateinit private var mListPagerLab: ListPagerLab
    private var curPhase: String = "BEGIN"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v (TAG, "MainActivity ListPagerLab init")
        mListPagerLab = ListPagerLab.get(this)
        Log.v (TAG, "MainActivity CreateView")
        setContentView(R.layout.activity_main)

        fragTimer = FragmentTimerSettings()
        curPhase = loadStartPhase()
        fragListView = ListViewFragment.newInstance("BEGIN")
        val transaction : FragmentTransaction? = supportFragmentManager.beginTransaction()
        when (curPhase) {
            "TIMER" -> {transaction?.replace(R.id.frame_container, fragTimer)?.commit()}
            else -> { setPhase(curPhase) }
        }


        nav_view.setNavigationItemSelectedListener(this)
        fab.setOnClickListener { view ->
            if (curPhase == "G2F_NEXT") {
                curPhase = "G2F"
                fragListView.changePhase(curPhase, this)
            } else {
                drawer_layout.openDrawer(GravityCompat.START)
            }
        }
        setSupportActionBar(maintoolbar)
        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, maintoolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            if (curPhase == "G2F_NEXT") {
                curPhase = "G2F"
                fragListView.changePhase(curPhase, this)
            } else {
                if (back_pressed_time + 1000 > System.currentTimeMillis()) {
                    super.onBackPressed()
                } else {
                    toast("Нажмите еще раз для выхода")
                    back_pressed_time = System.currentTimeMillis()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_help -> {
                //TODO написать хелп к настройкам таймера и таймеру
                return true}
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        // Handle navigation view item clicks here.
        Log.v(DebugTag.TAG, "NavigationItemSelected $item.itemId")
        when (item.itemId) {
            R.id.begin2x2 -> { setPhase("BEGIN2X2") }

            R.id.adv2x2 -> { setPhase("ADV2X2") }

            R.id.begin -> { setPhase("BEGIN") }

            R.id.g2f -> { setPhase("G2F") }

            R.id.blind -> {
                Snackbar.make(contentView!!, "Blind пока недоступен", Snackbar.LENGTH_LONG)
                    .setAction("ОК", {}).show()

            }
            R.id.blind_acc -> {
                Snackbar.make(contentView!!, "Blind пока недоступен", Snackbar.LENGTH_LONG)
                        .setAction("ОК", {}).show()

            }
            R.id.begin4x4 -> { setPhase("BEGIN4X4")}

            R.id.timer -> {
                val transaction: FragmentTransaction? = supportFragmentManager.beginTransaction()
                transaction?.replace(R.id.frame_container, fragTimer)?.commit()
                saveStartPhase("TIMER")
            }
            R.id.scramble -> {
                Snackbar.make(contentView!!, "Генератор скрамблов пока недоступен", Snackbar.LENGTH_LONG)
                        .setAction("ОК", {}).show()
            }
            R.id.pll_game -> {
                Snackbar.make(contentView!!, "Игра пока недоступна", Snackbar.LENGTH_LONG)
                        .setAction("ОК", {}).show()
            }
            R.id.basic_move -> { setPhase("BASIC") }

        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
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
    }

    private fun setPhase (phase: String){
        curPhase = phase
        val transaction: FragmentTransaction? = supportFragmentManager.beginTransaction()
        transaction?.replace(R.id.frame_container, fragListView)?.commit()
        fragListView.changePhase(curPhase, this)
        saveStartPhase(curPhase)
    }

    //Обработка выбора пункта меню в листвью.
    override fun onFragmentInteraction(phase:String, id:Int) {
        //Обработка событий из ListViewFragment
        Log.v(DebugTag.TAG, "onFragmentInteraction Start, $phase, $id")
        val lp = mListPagerLab.getPhaseItem(id,phase)
        val desc:String = getString(lp.description)
        when (phase) {
            //Если в листвью "Основные движения", то показать "тост",
            "BASIC" -> { toast(desc)}
            //Если меню "переходим на Фридрих", то меняем текст листвью на соответствующую фазу
            "G2F" -> {
                fragListView.changePhase(desc, this)
                curPhase = "G2F_NEXT"
            }
            //в других случаях запустить SlideTab с просмотром этапов
            else -> { startActivity<SlidingTabsActivity>(RUBIC_PHASE to phase, EXTRA_ID to id)}
        }
    }

}
