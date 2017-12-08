package ru.tohaman.rg3.activitys

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

import kotlinx.android.synthetic.main.activity_sliding_tabs.*
import kotlinx.android.synthetic.main.content_sliding_tabs.*
import ru.tohaman.rg3.R
import ru.tohaman.rg3.adapters.SlidingTabsAdapter

class SlidingTabsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sliding_tabs)

        setSupportActionBar(toolbar)

        viewPagerSlidingTabs.adapter = SlidingTabsAdapter(supportFragmentManager)
        tabs.setViewPager(viewPagerSlidingTabs)
    }
}
