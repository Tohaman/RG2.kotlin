package ru.tohaman.rg2.activitys

import android.os.Bundle
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_test_select_pll.*
import ru.tohaman.rg2.R

import org.jetbrains.anko.ctx
import org.jetbrains.anko.sdk15.coroutines.onClick
import org.jetbrains.anko.toast
import ru.tohaman.rg2.adapters.MyListAdapter
import ru.tohaman.rg2.data.ListPagerLab
import ru.tohaman.rg2.fragments.FragmentListView

class PllTestSelect : AppCompatActivity(), FragmentListView.OnListViewInteractionListener {
    private lateinit var fragListView: FragmentListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_select_pll)
        actionBar?.setDisplayHomeAsUpEnabled(true)
        val listPagerLab = ListPagerLab.get(ctx)
        val listPagers = listPagerLab.getPhaseList("PLLTEST")
        // Если еще нет текущих значений, значит они равны названиям Максимкиного PLL
        if (listPagers[0].comment == "") {
            listPagers.forEach {
                it.comment = it.title
                listPagerLab.updateListPager(it)
            }
        }
        fragListView = FragmentListView.newInstance("PLLTEST")
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.pll_list_view, fragListView).commit()

        select_maxim_pll.onClick {
            //TODO Убрать повторения кода в обработчике каждой кнопки
            listPagers.forEach {
                it.comment = it.title
                listPagerLab.updateListPager(it)
            }
            val listAdapter = MyListAdapter(listPagers,1.5f)
            fragListView.listAdapter = listAdapter

        }

        select_jperm_pll.onClick {
            listPagers.forEach {
                it.comment = it.url
                listPagerLab.updateListPager(it)
            }
            val listAdapter = MyListAdapter(listPagers,1.5f)
            fragListView.listAdapter = listAdapter
        }

        save_custom_pll.onClick {
            listPagers.forEach {
                val lp = it.copy(phase = "PLLTEST_CUSTOM")
                listPagerLab.updateListPager(lp)
            }
            toast("Текущие названия сохранены")
        }

        load_custom_pll.onClick {
            listPagers.forEach {
                it.comment = ListPagerLab.get(ctx).getPhaseItem(it.id, "PLLTEST_CUSTOM").comment
                listPagerLab.updateListPager(it)
            }
            val listAdapter = MyListAdapter(listPagers,1.5f)
            fragListView.listAdapter = listAdapter
        }

    }

    override fun onListViewInteraction(phase:String, id:Int) {

    }

}
