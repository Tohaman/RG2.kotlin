package ru.tohaman.rg3

import android.annotation.SuppressLint
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
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import org.jetbrains.anko.contentView
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import ru.tohaman.rg3.listpager.ListPagerLab
import ru.tohaman.rg3.DebugTag.TAG
import ru.tohaman.rg3.activitys.SlidingTabsActivity
import ru.tohaman.rg3.fragments.FragmentTimer
import ru.tohaman.rg3.fragments.ListViewFragment

// Статические переменные (верхнего уровня). Котлин в действии стр.77-78
const val EXTRA_ID = "ru.tohaman.rubicsguide.PHASE_ID"
const val RUBIC_PHASE = "ru.tohaman.rubicsguide.PHASE"

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, ListViewFragment.OnFragmentInteractionListener {
    private lateinit var fragListView: ListViewFragment
    private lateinit var fragTimer: Fragment
    private var back_pressed_time: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v (TAG, "MainActivity ListPagerLab init")
        val mListPagerLab = ListPagerLab.get(this)
        Log.v (TAG, "MainActivity CreateView")
        setContentView(R.layout.activity_main)
        setSupportActionBar(maintoolbar)

        fab.setOnClickListener { view ->
            drawer_layout.openDrawer(GravityCompat.START)
//            drawer_layout.closeDrawer(GravityCompat.START)
//            fragListView.changePhase("BASIC", this)
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                    .setAction("OK", {}).show()
        }

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, maintoolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        fragTimer = FragmentTimer()

        fragListView = ListViewFragment.newInstance(loadStartPhase())
        val transaction : FragmentTransaction? = supportFragmentManager.beginTransaction()
        transaction?.replace(R.id.frame_container, fragListView)?.commit()

        nav_view.setNavigationItemSelectedListener(this)
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            if (back_pressed_time + 1000 > System.currentTimeMillis()) {
                super.onBackPressed()
            } else {
                toast("Нажмите еще раз для выхода")
                back_pressed_time = System.currentTimeMillis()
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
            R.id.action_settings -> return true
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        // Handle navigation view item clicks here.
        val transaction: FragmentTransaction? = supportFragmentManager.beginTransaction()
        Log.v(DebugTag.TAG, "NavigationItemSelected $item.itemId")
        when (item.itemId) {
            R.id.begin2x2 -> {
                transaction?.replace(R.id.frame_container, fragListView)?.commit()
                fragListView.changePhase("BEGIN2X2", this)
                saveStartPhase("BEGIN2X2")
            }
            R.id.adv2x2 -> {
                transaction?.replace(R.id.frame_container, fragListView)?.commit()
                fragListView.changePhase("ADV2X2", this)
                saveStartPhase("ADV2X2")
            }
            R.id.begin -> {
                transaction?.replace(R.id.frame_container, fragListView)?.commit()
                fragListView.changePhase("BEGIN", this)
                saveStartPhase("BEGIN")
            }
            R.id.g2f -> {
                transaction?.replace(R.id.frame_container, fragListView)?.commit()
                fragListView.changePhase("G2F", this)
                saveStartPhase("G2F")
            }
            R.id.blind -> {
                Snackbar.make(contentView!!, "Blind пока недоступен", Snackbar.LENGTH_LONG)
                    .setAction("ОК", {}).show()

            }
            R.id.blind_acc -> {
                Snackbar.make(contentView!!, "Blind пока недоступен", Snackbar.LENGTH_LONG)
                        .setAction("ОК", {}).show()

            }
            R.id.begin4x4 -> {
                transaction?.replace(R.id.frame_container, fragListView)?.commit()
                fragListView.changePhase("BEGIN4X4", this)
                saveStartPhase("BEGIN4X4")
            }
            R.id.timer -> {
                transaction?.replace(R.id.frame_container, fragTimer)?.commit()
            }
            R.id.scramble -> {

            }
            R.id.pll_game -> {

            }
            R.id.basic_move -> {

            }


        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    fun loadStartPhase():String {
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        return sp.getString("startPhase", "BEGIN")
    }

    @SuppressLint("ApplySharedPref")
    fun saveStartPhase(phase:String) {
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = sp.edit()
        editor.putString("startPhase", phase)
        editor.commit() // подтверждаем изменения
    }

    override fun onFragmentInteraction(phase:String, id:Int) {
        //Обработка событий из ListViewFragment
        startActivity<SlidingTabsActivity>(RUBIC_PHASE to phase, EXTRA_ID to id)
    }
}
