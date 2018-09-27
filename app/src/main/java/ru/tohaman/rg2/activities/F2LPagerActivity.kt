package ru.tohaman.rg2.activities

import android.content.SharedPreferences
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.app_bar_sliding.*
import ru.tohaman.rg2.DebugTag
import ru.tohaman.rg2.MyDefaultActivity
import ru.tohaman.rg2.R
import ru.tohaman.rg2.adapters.MyOnlyImageListAdapter
import ru.tohaman.rg2.data.F2lPhases
import ru.tohaman.rg2.data.ListPager
import ru.tohaman.rg2.data.ListPagerLab
import ru.tohaman.rg2.fragments.FragmentF2LPagerItem
import ru.tohaman.rg2.fragments.FragmentPagerItem
import java.util.ArrayList

class F2LPagerActivity : MyDefaultActivity(),
        View.OnClickListener,
        SharedPreferences.OnSharedPreferenceChangeListener,
        FragmentF2LPagerItem.OnViewPagerInteractionListener
{
    private lateinit var mViewPagerSlidingTabs: ViewPager
    private lateinit var mRecyclerView: RecyclerView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v (DebugTag.TAG, "F2LPagerActivity onCreate")
        setContentView(R.layout.activity_oll_pager)
        setSupportActionBar(toolbar)

        Log.v (DebugTag.TAG, "SlidingTabActivity onCreate Инициализируем ListPagers и передаем его адаптерам")
        val mListPagerLab = ListPagerLab.get(this)
        val mListPagers : ArrayList<ListPager> = mListPagerLab.getPhaseList("EXP_F2L").filter { it.url != "submenu" } as ArrayList<ListPager>

        mViewPagerSlidingTabs = findViewById<ViewPager>(R.id.viewPagerSlidingTabs)
        setSlidingTabAdapter(convertDescription2ListPagers(mListPagers[0]))
        mViewPagerSlidingTabs.currentItem = 0
        tabs.setViewPager(mViewPagerSlidingTabs)


        val a = mListPagers.size
        mRecyclerView = findViewById<RecyclerView>(R.id.leftRecycleView)
        // Создаем вертикальный RecycleView (задаем Layout Manager)
        mRecyclerView.layoutManager = LinearLayoutManager(this)
        // Назначаем адаптер типа MyOnlyImage элемент которого только картинка
        mRecyclerView.adapter = MyOnlyImageListAdapter(mListPagers,this)
        //mRecyclerView.setOnClickListener {  }

    }

    private fun convertDescription2ListPagers(lp:ListPager): ArrayList<ListPager> {
        val description : String = getString(lp.description)
        val slotListPagers = arrayListOf<ListPager>()
        val gson = GsonBuilder().create()
        val itemsListType = object : TypeToken<ArrayList<F2lPhases>>() {}.type
        val listOfTexts : ArrayList <F2lPhases> = gson.fromJson(description, itemsListType)
        for (i in 0..3) {
            slotListPagers.add(ListPager(lp.phase, i, listOfTexts[i].slot, lp.icon, lp.description, lp.url, lp.comment))
        }
        return slotListPagers
    }

    private fun setSlidingTabAdapter(mListPagers: ArrayList<ListPager>) {
        // подключим адаптер для слайдингтаба (основного текста)
        val adapter = object : FragmentStatePagerAdapter(supportFragmentManager) {
            val titles = arrayOf("FR", "FB", "FL", "BL")

            override fun getPageTitle(position: Int): CharSequence {

                //Заголовки для ViewPager
                return titles[position]
            }

            override fun getCount(): Int {
                //Количество элементов для ViewPager
                return titles.size
            }

            override fun getItem(position: Int): Fragment {
                //возвращает фрагмент с элементом для ViewPager
                return FragmentF2LPagerItem.newInstance(mListPagers[position])
            }

        }
        mViewPagerSlidingTabs.adapter = adapter
    }

    override fun onClick(view: View?) {
        val position=view?.tag as Int
        Log.v (DebugTag.TAG, "F2LPagerActivity Click on item $position")
    }


    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
