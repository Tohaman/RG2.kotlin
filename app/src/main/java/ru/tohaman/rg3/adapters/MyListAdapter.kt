package ru.tohaman.rg3.adapters

import android.graphics.Typeface
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.LinearLayout.HORIZONTAL
import org.jetbrains.anko.*
import ru.tohaman.rg3.listpager.ListPager

/**
 * Created by anton on 27.11.17. Адаптер для listview
 */

class MyListAdapter(val list: ArrayList<ListPager> = ArrayList<ListPager>()) : BaseAdapter() {
    override fun getView(i: Int, v: View?, parent: ViewGroup?) : View {
        return with (parent!!.context) {
            val taskNum: Int = list[i].icon
            linearLayout {
                padding = dip(5)
                orientation = HORIZONTAL

                imageView(taskNum).lparams(width = dip(70), height = dip(70)) {
                    padding = dip(5)
                }

                textView {
                    text = list[i].title
                    textSize = 18f
                    padding = dip(5)
                    leftPadding = dip(20)
                    typeface = Typeface.DEFAULT_BOLD

                }
            }
        }
    }

    override fun getItem(position: Int): Any {
        return list[position]
    }

    override fun getItemId(position: Int): Long {
        return 0L
    }

    override fun getCount(): Int {
        return list.size
    }

}