package ru.tohaman.rg3.adapters

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

import ru.tohaman.rg3.fragments.CustomFragment
import ru.tohaman.rg3.listpager.ListPager
import ru.tohaman.rg3.listpager.ListPagerLab
import java.util.ArrayList

/**
 * Created by inaka on 12/23/15
 */
class SlidingTabsAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    private var titles  = listOf("Смежные окошки", "Противоположные окошки", "Fragment3", "Fragment4", "Fragment5")

    override fun getPageTitle(position: Int): CharSequence {
        return titles[position]
    }

    override fun getCount(): Int {

        return titles.size
    }

    override fun getItem(position: Int): Fragment {
        return CustomFragment.newInstance(titles[position])
    }

}
