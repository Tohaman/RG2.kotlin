package ru.tohaman.rg3.activitys

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.GravityCompat
import android.support.v4.view.ViewPager
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.ListView
import kotlinx.android.synthetic.main.app_bar_main.*
import org.jetbrains.anko.listView
import org.jetbrains.anko.sdk15.coroutines.onItemClick
import org.jetbrains.anko.toast
import ru.tohaman.rg3.DebugTag.TAG

import ru.tohaman.rg3.R
import ru.tohaman.rg3.adapters.MyListAdapter
import ru.tohaman.rg3.listpager.ListPager
import ru.tohaman.rg3.listpager.ListPagerLab
import java.util.ArrayList
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_sliding_tabs.*
import ru.tohaman.rg3.fragments.FragmentPagerItem


class SlidingTabsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v (TAG, "SlidingTabActivity Create")
        setContentView(R.layout.activity_sliding_tabs)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()


//        var fragPager = FragmentSlidePager()
//        val fm = supportFragmentManager
//        fm.beginTransaction().add(R.id.content_frame, fragPager)?.commit()


        Log.v (TAG, "MainFragment CreateView")
        val mListPagerLab = ListPagerLab.get(this)
        val mListPagers : ArrayList<ListPager> = mListPagerLab.getPhaseList("BEGIN")


        val mListAdapter = MyListAdapter(mListPagers)
        val mDrawerListView : ListView = findViewById(R.id.left_drawer)
        // подключим адаптер для выезжающего слева списка
        mDrawerListView.adapter = mListAdapter

//        val mSlideTabsAdapter = SlidingTabsAdapter(fm,mListPagers)
//        val mSlidePager : ViewPager = findViewById(R.id.viewPagerSlidingTabs)

        val mViewPagerSlidingTabs: ViewPager = findViewById(R.id.viewPagerSlidingTabs)
        // подключим адаптер для слайдингтаба (основного текста)
        mViewPagerSlidingTabs.adapter = object : FragmentStatePagerAdapter(supportFragmentManager) {

            override fun getPageTitle(position: Int): CharSequence {
                return mListPagers[position].title
            }

            override fun getCount(): Int {
                return mListPagers.size
            }

            override fun getItem(position: Int): Fragment {
                return FragmentPagerItem.newInstance(mListPagers[position].title)
            }

        }
        tabs.setViewPager(mViewPagerSlidingTabs)

        mDrawerListView.setOnItemClickListener { parent, view, position, id ->
            mViewPagerSlidingTabs.currentItem = position
            drawer_layout.closeDrawer(GravityCompat.START)
        }

//        mViewPagerSlidingTabs.currentItem = 5


    }

}
