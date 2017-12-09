package ru.tohaman.rg3.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.content_sliding_tabs.*

import ru.tohaman.rg3.R
import ru.tohaman.rg3.adapters.SlidingTabsAdapter


class FragmentG2F : Fragment() {


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val v = inflater!!.inflate(R.layout.content_sliding_tabs, container, false)

        val viewPagerSlidingTabs: ViewPager = v.findViewById(R.id.viewPagerSlidingTabs)
        viewPagerSlidingTabs.adapter = object : FragmentStatePagerAdapter(activity.supportFragmentManager) {
            private var titles  = listOf("Смежные окошки", "Противоположные окошки", "Fragment3", "Fragment4", "Fragment5")

            override fun getPageTitle(position: Int): CharSequence {
                return titles[position]
            }

            override fun getCount(): Int {

                return titles.size
            }

            override fun getItem(position: Int): Fragment {
                return FragmentPagerItem.newInstance(titles[position])
            }
        }



        // Inflate the layout for this fragment
        return v


    }

}// Required empty public constructor
