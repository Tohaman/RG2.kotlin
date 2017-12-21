package ru.tohaman.rg3.adapters

import android.content.Context
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.LinearLayout.HORIZONTAL
import org.jetbrains.anko.*
import ru.tohaman.rg3.data.ListPager

/**
 * Created by anton on 27.11.17. Адаптер для listview включает в себя сразу и UI
 * хотя можно наверно сделать в getView() return listUI()
 * а ListUI сделать наследником AnkoComponentEx, пока это TODO
 */

class MyListAdapter(val list: ArrayList<ListPager> = ArrayList(), private val m: Float = 1f) : BaseAdapter() {
    lateinit var context: Context
    private val Int.dp: Int get() = this.dpf.toInt()
    private val Int.dpf: Float get() = this * context.resources.displayMetrics.density

    override fun getView(i: Int, v: View?, parent: ViewGroup?): View {
        context = parent!!.context
        return with(context) {
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
                        gravity = Gravity.START

                        imageView(taskNum) {
                        }.lparams(dip(40 * m),dip(40 * m)) {margin = 5.dp}

                        textView {
                            text = list[i].title
                            textSize = m * 12f
                            typeface = Typeface.DEFAULT_BOLD
                        }.lparams(matchParent, wrapContent) { setMargins(20.dp, 5.dp, 5.dp, 5.dp) }
                    }
                }
            }
        }
    }

    override fun getItem(position: Int): ListPager {
        return list[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return list.size
    }

}