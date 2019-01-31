package ru.tohaman.rg2.activities

import android.content.SharedPreferences
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.app_bar_sliding.*
import ru.tohaman.rg2.*
import ru.tohaman.rg2.adapters.MyOnlyImageListAdapter
import ru.tohaman.rg2.data.ListPager
import ru.tohaman.rg2.data.ListPagerLab
import ru.tohaman.rg2.fragments.FragmentF2LPagerItem
import java.util.ArrayList

class F2LPagerActivity : MyDefaultActivity(),
        View.OnClickListener,
        SharedPreferences.OnSharedPreferenceChangeListener,
        FragmentF2LPagerItem.OnViewPagerInteractionListener
{
    private lateinit var mListPagers : ArrayList<ListPager>
    private lateinit var mListMainItem : ArrayList<ListPager>
    private lateinit var mListPagerLab : ListPagerLab
    private var curPhase = "EXP_F2L"
    private var curId = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v (DebugTag.TAG, "F2LPagerActivity onCreate")
        setContentView(R.layout.activity_oll_pager)
        setSupportActionBar(toolbar)

        //Инициируем фазу и номер этапа, должны быть переданы из другой активности, если нет, то используем значения по-умолчанию
        if (intent.hasExtra(RUBIC_PHASE)) curPhase = intent.extras!!.getString(RUBIC_PHASE)!!
        if (intent.hasExtra(EXTRA_ID)) curId = intent.extras!!.getInt(EXTRA_ID)

        Log.v (DebugTag.TAG, "SlidingTabActivity onCreate Инициализируем ListPagers и передаем его адаптерам")
        mListPagerLab = ListPagerLab.get(this)
        mListPagers  = mListPagerLab.getPhaseList(curPhase, "0").filter { it.url != "ollPager" } as ArrayList<ListPager>

        setSlidingTabAdapter(convertDescription2ListPagers(mListPagers[curId]))
        viewPagerSlidingTabs.currentItem = 0
        tabs.setViewPager(viewPagerSlidingTabs)


        val mRecyclerView = findViewById<RecyclerView>(R.id.leftRecycleView)
        // Создаем вертикальный RecycleView (задаем Layout Manager)
        mRecyclerView.layoutManager = LinearLayoutManager(this)
        // Назначаем адаптер типа MyOnlyImage элемент которого только картинка
        // назначем обработчик для слушателя onClick адаптера в активности
        mListMainItem  = mListPagers.filter { it.subID == "0" } as ArrayList<ListPager>
        mRecyclerView.adapter = MyOnlyImageListAdapter(mListMainItem,this)

    }

    private fun convertDescription2ListPagers(lp:ListPager): ArrayList<ListPager> {
        return mListPagerLab.getPhaseItemList(lp.id, lp.phase)
    }

    //при смене этапа, меняем (устанавливаем) адаптер для слайдингтаба
    private fun setSlidingTabAdapter(mListPagers: ArrayList<ListPager>) {
        // подключим адаптер для слайдингтаба (основного текста)
        val adapter = object : FragmentStatePagerAdapter(supportFragmentManager) {

            override fun getPageTitle(position: Int): CharSequence {
                //Заголовки для ViewPager
                return mListPagers[position].subTitle
            }

            override fun getCount(): Int {
                //Количество элементов для ViewPager
                return mListPagers.size
            }

            override fun getItem(position: Int): Fragment {
                //возвращает фрагмент с элементом для ViewPager
                return FragmentF2LPagerItem.newInstance(mListPagers[position])
            }

        }
        viewPagerSlidingTabs.adapter = adapter
    }

    //обрабатываем onClick по элементам RecycleView, через перехват onClick адаптера
    override fun onClick(view: View?) {
        val position = view?.tag as Int
        //Log.v (DebugTag.TAG, "F2LPagerActivity Click on item $position")
        setSlidingTabAdapter(convertDescription2ListPagers(mListMainItem[position]))
        tabs.setViewPager(viewPagerSlidingTabs)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
