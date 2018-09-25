package ru.tohaman.rg2.activities

import android.content.SharedPreferences
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import kotlinx.android.synthetic.main.app_bar_sliding.*
import org.jetbrains.anko.*
import ru.tohaman.rg2.DebugTag
import ru.tohaman.rg2.MyDefaultActivity
import ru.tohaman.rg2.R
import ru.tohaman.rg2.adapters.MyOnlyImageListAdapter
import ru.tohaman.rg2.data.ListPager
import ru.tohaman.rg2.data.ListPagerLab
import ru.tohaman.rg2.fragments.FragmentPagerItem
import java.util.ArrayList

class OllPagerActivity : MyDefaultActivity(),
        SharedPreferences.OnSharedPreferenceChangeListener,
        FragmentPagerItem.OnViewPagerInteractionListener
{
    private lateinit var mListPagerLab: ListPagerLab
    private lateinit var mViewPagerSlidingTabs: ViewPager
    private lateinit var mRecyclerView: RecyclerView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v (DebugTag.TAG, "OllPagerActivity onCreate")
        setContentView(R.layout.activity_oll_pager)
        setSupportActionBar(toolbar)

        Log.v (DebugTag.TAG, "SlidingTabActivity onCreate Инициализируем ListPagers и передаем его адаптерам")
        mListPagerLab = ListPagerLab.get(this)
        val mListPagers : ArrayList<ListPager> = mListPagerLab.getPhaseList("EXP_F2L").filter { it.url != "submenu" } as ArrayList<ListPager>

        mViewPagerSlidingTabs = findViewById<ViewPager>(R.id.viewPagerSlidingTabs)

        // подключим адаптер для слайдингтаба (основного текста)
        val adapter = object : FragmentStatePagerAdapter(supportFragmentManager) {

            override fun getPageTitle(position: Int): CharSequence {
                val titles = arrayOf("FR", "FB", "FL", "BL")
                //Заголовки для ViewPager
                return titles[position]
            }

            override fun getCount(): Int {
                //Количество элементов для ViewPager
                return 4
            }

            override fun getItem(position: Int): Fragment {
                //возвращает фрагмент с элементом для ViewPager
                return FragmentPagerItem.newInstance(mListPagers[position])
            }

        }

        mViewPagerSlidingTabs.adapter = adapter
        mViewPagerSlidingTabs.currentItem = 0
        tabs.setViewPager(mViewPagerSlidingTabs)


        val a = mListPagers.size
        mRecyclerView = findViewById<RecyclerView>(R.id.leftRecycleView)
        // Создаем вертикальный RecycleView (задаем Layout Manager)
        mRecyclerView.layoutManager = LinearLayoutManager(this)
        // Назначаем адаптер типа MyOnlyImage элемент которого только картинка
        mRecyclerView.adapter = MyOnlyImageListAdapter(mListPagers,ctx)

    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
