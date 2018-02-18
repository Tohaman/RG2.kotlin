package ru.tohaman.rg2.adapters

import android.content.Context
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.LinearLayout.HORIZONTAL
import android.widget.LinearLayout.VERTICAL
import org.jetbrains.anko.*
import ru.tohaman.rg2.data.ListPager

/**
 * Created by anton on 27.11.17. Адаптер для listview включает в себя сразу и UI
 * хотя можно наверно сделать в getView() return listUI()
 * а ListUI сделать наследником AnkoComponentEx, хотя кода не много и так наверно проще
 */

class MyListAdapter(private val listOfLP: ArrayList<ListPager> = ArrayList(), private val m: Float = 1f) : BaseAdapter() {
    lateinit var context: Context
    private val Int.dp: Int get() = this.dpf.toInt()
    private val Int.dpf: Float get() = this * context.resources.displayMetrics.density

    override fun getView(i: Int, v: View?, parent: ViewGroup?): View {
        context = parent!!.context
        return with(context) {
            when (listOfLP[0].phase) {
                "BASIC", "BASIC_PYR", "BASIC_SKEWB" -> {
                    val taskNum: Int = listOfLP[i].icon
                    linearLayout {
                        orientation = HORIZONTAL
                        gravity = Gravity.CENTER

                        textView {
                            text = listOfLP[i].title
                            textSize = 30f
                            padding = dip(5)
                            rightPadding = dip(30)
                            typeface = Typeface.DEFAULT_BOLD
                        }
                        imageView(taskNum).lparams(height = dip(70)) {
                            padding = dip(5)
                        }
                    }
                }
                else -> {
                    val taskNum: Int = listOfLP[i].icon
                    linearLayout {
                        orientation = HORIZONTAL
//                        gravity = Gravity.CENTER

                        imageView(taskNum) {
                        }.lparams(dip(42 * m),dip(42 * m)) {margin = 5.dp}

                        linearLayout() {
                            orientation = VERTICAL
                            textView {
                                text = if (listOfLP[0].phase == "PLLTEST"){
                                    listOfLP[i].comment
                                } else {
                                    listOfLP[i].title
                                }
                                textSize = m * 12f
                                typeface = Typeface.DEFAULT_BOLD
                            }.lparams(matchParent, wrapContent) { setMargins(15.dp, 5.dp, 5.dp, 0.dp) }
                            textView {
                                textSize = m *8F
                                singleLine = true
                                text = if (listOfLP[0].phase == "PLLTEST"){
                                    ""
                                } else {
                                    listOfLP[i].comment
                                }
                            }.lparams(matchParent, wrapContent) { setMargins(15.dp, 0.dp, 5.dp, 5.dp) }
                        }.lparams(matchParent, matchParent)
                    }
                }
            }
        }
    }

    override fun getItem(position: Int): ListPager {
        return listOfLP[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return listOfLP.size
    }

}