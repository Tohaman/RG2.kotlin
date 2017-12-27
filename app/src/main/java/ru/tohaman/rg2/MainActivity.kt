package ru.tohaman.rg2

import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import org.jetbrains.anko.*
import org.jetbrains.anko.design.snackbar
import ru.tohaman.rg2.data.ListPagerLab
import ru.tohaman.rg2.DebugTag.TAG
import ru.tohaman.rg2.activitys.SlidingTabsActivity
import ru.tohaman.rg2.fragments.*

// Статические переменные (верхнего уровня). Котлин в действии стр.77-78
const val EXTRA_ID = "ru.tohaman.rubicsguide.PHASE_ID"
const val RUBIC_PHASE = "ru.tohaman.rubicsguide.PHASE"
const val VIDEO_PREVIEW = "video_preview"   //наименования ключа для сохранения/извлечения значения из файла настроек
const val ONE_HAND_TO_START = "oneHandToStart"
const val METRONOM_ENABLED = "metronomEnabled"
const val METRONOM_TIME = "metronomTime"

class MainActivity : AppCompatActivity(),
        NavigationView.OnNavigationItemSelectedListener,
        ListViewFragment.OnListViewInteractionListener,
        FragmentScrambleGen.OnSrambleGenInteractionListener {

    private lateinit var fragListView: ListViewFragment
    private var backPressedTime: Long = 0
    lateinit private var mListPagerLab: ListPagerLab
    private var curPhase: String = "BEGIN"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v (TAG, "MainActivity ListPagerLab init")
        mListPagerLab = ListPagerLab.get(this)
        Log.v (TAG, "MainActivity CreateView")
        setContentView(R.layout.activity_main)

        curPhase = loadStartPhase()
        fragListView = ListViewFragment.newInstance("BEGIN")
        when (curPhase) {
            "TIMER" -> {setFragment(FragmentTimerSettings.newInstance())}
            "SCRAMBLEGEN" -> {setFragment(FragmentScrambleGen.newInstance())}
            "TESTPLL" -> {setFragment(FragmentTestPLL.newInstance())}
            else -> { setFragment(ListViewFragment.newInstance(curPhase)) }
        }

        nav_view.setNavigationItemSelectedListener(this)
        fab.setOnClickListener { _ ->
            when (curPhase) {
                "G2F_NEXT" -> {
                    curPhase = "G2F"
                    fragListView.changePhase(curPhase, this)
                    fab.setImageResource(R.drawable.ic_fab_forward)
                }
                "AZBUKA" -> {
                    curPhase = "SCRAMBLEGEN"
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
    }

    private fun setFragment (fragment: Fragment) {
        val transaction : FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame_container, fragment).commit()
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            if (curPhase == "G2F_NEXT") {
                curPhase = "G2F"
                fragListView.changePhase(curPhase, this)
            } else {
                if (curPhase == "AZBUKA"){
                    curPhase = "SCRAMBLEGEN"
                    setFragment(FragmentScrambleGen.newInstance())
                } else {
                    if (backPressedTime + 1000 > System.currentTimeMillis()) {
                        super.onBackPressed()
                    } else {
                        toast("Нажмите еще раз для выхода")
                        backPressedTime = System.currentTimeMillis()
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
                    "TIMER" -> {
                        alert(getString(R.string.help_timer)) { okButton { } }.show()
                    }
                  }
                return true}
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

            R.id.blind -> {
                snackbar(contentView!!, "Blind пока недоступен","ОК") {/** Do something */}
            }
            R.id.blind_acc -> {
                snackbar(contentView!!, "Blind пока недоступен","ОК") {/** Do something */}
            }
            R.id.begin4x4 -> { setListFragmentPhase("BEGIN4X4")}

            R.id.timer -> {
                setFragment(FragmentTimerSettings.newInstance())
                saveStartPhase("TIMER")
            }
            R.id.scramble -> {
                setFragment(FragmentScrambleGen.newInstance())
                saveStartPhase("SCRAMBLEGEN")
            }
            R.id.pll_game -> {
                setFragment(FragmentTestPLL.newInstance())
                saveStartPhase("TESTPLL")
            }

            R.id.basic_move -> { setListFragmentPhase("BASIC") }

            R.id.thanks -> {
                snackbar(contentView!!, "Спасибо!","ОК") {/** Do something */}
            }
            R.id.about -> {
                snackbar (contentView!!, "О программе", "OK") {/** Do something */}
            }

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

    private fun setListFragmentPhase(phase: String){
        curPhase = phase
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame_container, fragListView).commit()
        fragListView.changePhase(curPhase, this)
        saveStartPhase(curPhase)
    }

    //Обработка выбора пункта меню в листвью.
    override fun onListViewInteraction(phase:String, id:Int) {
        //Обработка событий из ListViewFragment
        Log.v(DebugTag.TAG, "onListViewInteraction Start, $phase, $id")
        val lp = mListPagerLab.getPhaseItem(id,phase)
        val desc:String = getString(lp.description)
        when (phase) {
            //Если в листвью "Основные движения", то показать "тост",
            "BASIC" -> { toast(desc)}
            //Если меню "переходим на Фридрих", то меняем текст листвью на соответствующую фазу
            "G2F" -> {
                fragListView.changePhase(desc, this)
                fab.setImageResource(R.drawable.ic_fab_backward)
                curPhase = "G2F_NEXT"
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

}
