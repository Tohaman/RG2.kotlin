package ru.tohaman.rg2.activities

import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import android.util.Log
import android.view.WindowManager
import kotlinx.android.synthetic.main.activity_test_select_pll.*
import org.jetbrains.anko.*
import ru.tohaman.rg2.R

import org.jetbrains.anko.sdk15.coroutines.onClick
import ru.tohaman.rg2.DebugTag
import ru.tohaman.rg2.IS_SCREEN_ALWAYS_ON
import ru.tohaman.rg2.MyDefaultActivity
import ru.tohaman.rg2.adapters.MyListAdapter
import ru.tohaman.rg2.data.ListPager
import ru.tohaman.rg2.data.ListPagerLab
import ru.tohaman.rg2.fragments.FragmentListView
import ru.tohaman.rg2.util.getThemeFromSharedPreference

class PllTestSelect : MyDefaultActivity(), FragmentListView.OnListViewInteractionListener {
    private lateinit var fragListView: FragmentListView
    private lateinit var listPagers: ArrayList<ListPager>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v (DebugTag.TAG, "PLLTestSelect onCreate start")

        setContentView(R.layout.activity_test_select_pll)
        actionBar?.setDisplayHomeAsUpEnabled(true)
        val listPagerLab = ListPagerLab.get(ctx)
        listPagers = listPagerLab.getPhaseList("PLLTEST")
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
            Log.v (DebugTag.TAG, "PLLTestSelect select_maxim_pll.onClick start")
            listPagers.forEach {
                it.comment = it.title
                listPagerLab.updateListPager(it)
            }
            val listAdapter = MyListAdapter(listPagers,1.5f)
            fragListView.listAdapter = listAdapter
        }

        select_jperm_pll.onClick {
            Log.v (DebugTag.TAG, "PLLTestSelect select_jperm_pll.onClick start")
            listPagers.forEach {
                it.comment = it.url
                listPagerLab.updateListPager(it)
            }
            val listAdapter = MyListAdapter(listPagers,1.5f)
            fragListView.listAdapter = listAdapter
        }

        save_custom_pll.onClick {
            Log.v (DebugTag.TAG, "PLLTestSelect save_custom_pll.onClick start")
            listPagers.forEach {
                val lp = it.copy(phase = "PLLTEST_CUSTOM")
                listPagerLab.updateListPager(lp)
            }
            toast("Текущие названия сохранены")
        }

        load_custom_pll.onClick {
            Log.v (DebugTag.TAG, "PLLTestSelect load_custom_pll.onClick start")
            listPagers.forEach {
                it.comment = listPagerLab.getPhaseItem(it.id, "PLLTEST_CUSTOM").comment
                listPagerLab.updateListPager(it)
            }
            val listAdapter = MyListAdapter(listPagers,1.5f)
            fragListView.listAdapter = listAdapter
        }

    }

    override fun onListViewInteraction(phase:String, id:Int) {
        Log.v (DebugTag.TAG, "PLLTestSelect onListViewInteraction start")

        alert {
            customView {
                verticalLayout {
                    textView {
                        text = "Задайте свое имя для:"
                        textSize = 20F
                    }.lparams {margin = dip (16)}
                    imageView(listPagers[id].icon)
                    val setPll = editText (listPagers[id].comment){
                        hint = "введите свое название"
                    }.lparams {margin = dip (8)}
                    positiveButton("OK") {
                        listPagers[id].comment = setPll.text.toString()
                        fragListView.listAdapter = MyListAdapter(listPagers,1.5f)
                    }
                    negativeButton("Отмена") {}
                }
            }
        }.show()
    }

}
