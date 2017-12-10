package ru.tohaman.rg3

import android.net.Uri
import android.os.Bundle
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
import org.jetbrains.anko.startActivity
import ru.tohaman.rg3.listpager.ListPagerLab
import ru.tohaman.rg3.DebugTag.TAG
import ru.tohaman.rg3.activitys.SlidingTabsActivity
import ru.tohaman.rg3.fragments.Fragment2x2Advanced
import ru.tohaman.rg3.fragments.FragmentListView
import ru.tohaman.rg3.fragments.ListViewFragment

// Статические переменные (верхнего уровня). Котлин в действии стр.77-78
const val EXTRA_ID = "ru.tohaman.rubicsguide.PHASE_ID"
const val RUBIC_PHASE = "ru.tohaman.rubicsguide.PHASE"
lateinit var fragListView: ListViewFragment
lateinit var frag2x2Adv : Fragment
lateinit var fragG2F: Fragment

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, ListViewFragment.OnFragmentInteractionListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v (TAG, "MainActivity ListPagerLab init")
        val mListPagerLab = ListPagerLab.get(this)
        Log.v (TAG, "MainActivity CreateView")
        setContentView(R.layout.activity_main)
        setSupportActionBar(maintoolbar)

        fab.setOnClickListener { view ->
            fragListView.changePhase("BASIC", this)
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                    .setAction("Action", null).show()
        }

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, maintoolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        frag2x2Adv = Fragment2x2Advanced()

        fragListView = ListViewFragment.newInstance("BEGIN")
        val transaction : FragmentTransaction? = supportFragmentManager.beginTransaction()
        transaction?.replace(R.id.frame_container, fragListView)?.commit()
        //fragListView.changePhase("BEGIN", this)

        nav_view.setNavigationItemSelectedListener(this)
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
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
        val transaction : FragmentTransaction? = supportFragmentManager.beginTransaction()
        when (item.itemId) {
            R.id.begin2x2 -> {
                Log.v(DebugTag.TAG, "NavigationItemSelected begin 2x2")
                transaction?.replace(R.id.frame_container, fragListView)?.commit()
                fragListView.changePhase("BEGIN", this)
            }
            R.id.adv2x2 -> {
                Log.v(DebugTag.TAG, "NavigationItemSelected adv 2x2")
                transaction?.replace(R.id.frame_container, frag2x2Adv)?.commit()
            }
            R.id.begin -> {
                Log.v(DebugTag.TAG, "NavigationItemSelected begin")
                transaction?.replace(R.id.frame_container, fragListView)?.commit()
                fragListView.changePhase("BASIC", this)
            }
            R.id.g2f -> {
                Log.v(DebugTag.TAG, "NavigationItemSelected g2f")
                startActivity<SlidingTabsActivity>()
                //frams?.replace(R.id.frame_container, fragG2F)?.commit()
            }
            R.id.blind -> {
                Log.v(DebugTag.TAG, "NavigationItemSelected blind")

            }
            R.id.timer -> {
                Log.v(DebugTag.TAG, "NavigationItemSelected timer")
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onFragmentInteraction(uri: Uri) {
        //Обработка событий из ListViewFragment
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
