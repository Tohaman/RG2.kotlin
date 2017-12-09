package ru.tohaman.rg3.activitys

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v7.app.AppCompatActivity

import kotlinx.android.synthetic.main.activity_sliding_tabs.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.content_sliding_tabs.*
import ru.tohaman.rg3.R
import ru.tohaman.rg3.fragments.FragmentG2F
import ru.tohaman.rg3.fragments.FragmentPagerItem

class SlidingTabsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.content_main)

        var fragG2F = FragmentG2F()

        val fm = supportFragmentManager

        fm.beginTransaction().add(R.id.frame_container, fragG2F)?.commit()
    }
}
