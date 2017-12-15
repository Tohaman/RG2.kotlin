package ru.tohaman.rg3.adapters

import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.LinearLayout.HORIZONTAL
import org.jetbrains.anko.*
import ru.tohaman.rg3.listpager.ListPager

/**
 * Created by anton on 27.11.17. Адаптер для listview включает в себя сразу и UI
 * хотя правильне наверно сделать в getView() return listUI()
 * а ListUI сделать наследником AnkoComponentEx
 */

class MyListAdapter(val list: ArrayList<ListPager> = ArrayList(), private val m: Float = 1f) : BaseAdapter() {
    override fun getView(i: Int, v: View?, parent: ViewGroup?): View {
        return with(parent!!.context) {
            when (list[0].phase) {
                "BASIC" -> {
                    val taskNum: Int = list[i].icon
                    linearLayout {
                        padding = dip(5)
                        orientation = HORIZONTAL
                        gravity = Gravity.CENTER

                        textView {
                            text = list[i].title
                            textSize = 30f
                            padding = dip(5)
                            rightPadding = dip(30)
                            typeface = Typeface.DEFAULT_BOLD

                        }

                        imageView(taskNum).lparams(height = dip(75)) {
                            padding = dip(5)
                        }
                    }
                }
                else -> {
                    val taskNum: Int = list[i].icon
                    linearLayout {
                        padding = dip(5)
                        orientation = HORIZONTAL

                        imageView(taskNum).lparams(width = dip(40 * m), height = dip(40 * m)) {
                            padding = dip(5)
                        }

                        textView {
                            text = list[i].title
                            textSize = m * 12f
                            padding = dip(5)
                            leftPadding = dip(20)
                            typeface = Typeface.DEFAULT_BOLD

                        }
                    }
                }
            }
        }
    }

    override fun getItem(position: Int): ListPager {
        return list[position]
    }

    override fun getItemId(position: Int): Long {
        return 0L
    }

    override fun getCount(): Int {
        return list.size
    }

}