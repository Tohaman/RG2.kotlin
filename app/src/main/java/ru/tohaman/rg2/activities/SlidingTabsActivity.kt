package ru.tohaman.rg2.activities

import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.GravityCompat
import android.support.v4.view.ViewPager
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.ListView
import ru.tohaman.rg2.DebugTag.TAG

import ru.tohaman.rg2.adapters.MyListAdapter
import ru.tohaman.rg2.data.ListPager
import ru.tohaman.rg2.data.ListPagerLab
import java.util.ArrayList
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_sliding.*
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onItemClick
import ru.tohaman.rg2.*
import ru.tohaman.rg2.fragments.FragmentPagerItem
import ru.tohaman.rg2.util.getThemeFromSharedPreference


class SlidingTabsActivity : AppCompatActivity() {
    private var mPhase = "BEGIN"

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(getThemeFromSharedPreference(ctx))
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        Log.v (TAG, "SlidingTabActivity onCreate")
        setContentView(R.layout.activity_sliding_tabs)
        setSupportActionBar(toolbar)
        val sp = PreferenceManager.getDefaultSharedPreferences(ctx)
        if (!sp.getBoolean("fab_on", true)) {
            fab_sl.visibility = View.GONE
        } else {
            fab_sl.visibility = View.VISIBLE
        }
        fab_sl.setOnClickListener {
            onBackPressed()
        }

        val isScreenAlwaysOn = sp.getBoolean(IS_SCREEN_ALWAYS_ON, false)
        if (isScreenAlwaysOn) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }


        //Инициируем фазу и номер этапа, должны быть переданы из другой активности, если нет, то используем значения по-умолчанию
        var id = 0
        if (intent.hasExtra(RUBIC_PHASE)){
            mPhase = intent.extras.getString(RUBIC_PHASE)
        }
        if (intent.hasExtra(EXTRA_ID)){
            id = intent.extras.getInt(EXTRA_ID)
        }

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        Log.v (TAG, "SlidingTabActivity onCreate Инициализируем ListPagers и передаем его адаптерам")
        val mListPagerLab = ListPagerLab.get(this)
        val mListPagers : ArrayList<ListPager> = mListPagerLab.getPhaseList(mPhase)

        Log.v (TAG, "SlidingTabActivity onCreate Настраиваем SlidingTab")
        val mViewPagerSlidingTabs = findViewById<ViewPager>(R.id.viewPagerSlidingTabs)

        // подключим адаптер для слайдингтаба (основного текста)
        mViewPagerSlidingTabs.adapter = object : FragmentStatePagerAdapter(supportFragmentManager) {

            override fun getPageTitle(position: Int): CharSequence {
                return mListPagers[position].title
            }

            override fun getCount(): Int {
                return mListPagers.size
            }

            override fun getItem(position: Int): Fragment {
                return FragmentPagerItem.newInstance(mListPagers[position])
            }

        }
        mViewPagerSlidingTabs.currentItem = id
        tabs.setViewPager(mViewPagerSlidingTabs)

        Log.v (TAG, "SlidingTabActivity onCreate Настраиваем ListView для списка слева")
        // Настраиваем листвью для выезжающего слева списка
        val mListAdapter = MyListAdapter(mListPagers)
        val mDrawerListView  = findViewById<ListView>(R.id.left_drawer)
        // подключим адаптер для выезжающего слева списка
//        mDrawerListView.setBackgroundResource(R.color.background_material_light)
        mDrawerListView.adapter = mListAdapter

        mDrawerListView.setOnItemClickListener { _, _, position, _ ->
            mViewPagerSlidingTabs.currentItem = position
            drawer_layout.closeDrawer(GravityCompat.START)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.sliding_tab_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //Если нажали на ? в правом верхнем углу, то вызываем АлертДиалог
        //со списком основных движений
        Log.v (TAG, "onOptionsItemSelected")
        when (item.itemId) {
            R.id.basic_move_help -> {
                alert {
                    customView {
                        positiveButton("Закрыть окно") {
                        }
                        verticalLayout {
                            val listPagers : ArrayList<ListPager> =
                                    when (mPhase) {
                                        "PYRAMINX" -> {ListPagerLab.get(ctx).getPhaseList("BASIC_PYR")}
                                        else -> {ListPagerLab.get(ctx).getPhaseList("BASIC")}
                                    }
                            val lstView = listView {
                                adapter = MyListAdapter(listPagers)
                            }
                            lstView.onItemClick { p0, p1, p2, p3 ->
                                toast(listPagers[p2].description)
                            }
                        }
                    }
                }.show()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }



}
