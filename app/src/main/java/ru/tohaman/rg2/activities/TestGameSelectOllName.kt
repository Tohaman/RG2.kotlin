package ru.tohaman.rg2.activities

import android.os.Bundle
import android.support.v4.app.FragmentTransaction
import android.util.Log
import kotlinx.android.synthetic.main.activity_test_select_pll.*
import org.jetbrains.anko.*
import ru.tohaman.rg2.R

import org.jetbrains.anko.sdk15.coroutines.onClick
import ru.tohaman.rg2.DebugTag
import ru.tohaman.rg2.adapters.MyListAdapter
import ru.tohaman.rg2.data.ListPager
import ru.tohaman.rg2.data.ListPagerLab
import ru.tohaman.rg2.fragments.FragmentListView

class TestGameSelectOllName : MyDefaultActivity(), FragmentListView.OnListViewInteractionListener {
    private lateinit var fragListView: FragmentListView
    private lateinit var listPagers: ArrayList<ListPager>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v (DebugTag.TAG, "OLLTestSelect onCreate start")

        setContentView(R.layout.activity_test_select_pll)
        actionBar?.setDisplayHomeAsUpEnabled(true)
        val listPagerLab = ListPagerLab.get(this)
        listPagers = listPagerLab.getPhaseList("OLLTEST")
        // Если еще нет текущих значений, значит они равны названиям Максимкиного OLL
        if (listPagers[0].comment == "") {
            listPagers.forEach {
                it.comment = it.title
                listPagerLab.updateListPager(it)
            }
        }

        select_classik_name.text = "Алгоритмы"

        fragListView = FragmentListView.newInstance("OLLTEST")
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.pll_oll_list_view, fragListView).commit()

        select_maxim_name.onClick {
            Log.v (DebugTag.TAG, "PLLTestSelect select_maxim_OLL.onClick start")
            listPagers.forEach {
                it.comment = it.title
                listPagerLab.updateListPager(it)
            }
            val listAdapter = MyListAdapter(listPagers,1.5f)
            fragListView.listAdapter = listAdapter
        }

        select_classik_name.onClick {
            Log.v (DebugTag.TAG, "OLLTestSelect select_classik_name.onClick start")
            listPagers.forEach {
                it.comment = it.url
                listPagerLab.updateListPager(it)
            }
            val listAdapter = MyListAdapter(listPagers,1.5f)
            fragListView.listAdapter = listAdapter
        }

        save_custom_name.onClick {
            Log.v (DebugTag.TAG, "OLLTestSelect save_custom_OLL.onClick start")
            listPagers.forEach {
                val lp = it.copy(phase = "OLLTEST_CUSTOM")
                listPagerLab.updateListPager(lp)
            }
            toast("Текущие названия сохранены")
        }

        load_custom_name.onClick {
            Log.v (DebugTag.TAG, "OLLTestSelect load_custom_OLL.onClick start")
            listPagers.forEach {
                it.comment = listPagerLab.getPhaseItem(it.id, "OLLTEST_CUSTOM").comment
                listPagerLab.updateListPager(it)
            }
            val listAdapter = MyListAdapter(listPagers,1.5f)
            fragListView.listAdapter = listAdapter
        }

    }

    override fun onListViewInteraction(phase:String, id:Int) {
        Log.v (DebugTag.TAG, "OLLTestSelect onListViewInteraction start")

        alert {
            customView {
                verticalLayout {
                    textView {
                        text = "Задайте свое имя для:"
                        textSize = 20F
                    }.lparams {margin = dip (16)}
                    imageView(listPagers[id].icon)
                    val setOll = editText (listPagers[id].comment){
                        hint = "введите свое название"
                    }.lparams {margin = dip (8)}
                    positiveButton("OK") {
                        listPagers[id].comment = setOll.text.toString()
                        fragListView.listAdapter = MyListAdapter(listPagers,1.5f)
                    }
                    negativeButton("Отмена") {}
                }
            }
        }.show()
    }

}
