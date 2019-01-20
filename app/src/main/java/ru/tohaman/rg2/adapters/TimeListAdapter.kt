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
import ru.tohaman.rg2.R
import ru.tohaman.rg2.ankoconstraintlayout.constraintLayout
import ru.tohaman.rg2.data.ListPager
import ru.tohaman.rg2.data.TimeNote
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by anton on 27.11.17. Адаптер для listview включает в себя сразу и UI
 * хотя можно наверно сделать в getView() return listUI()
 * а ListUI сделать наследником AnkoComponentEx, хотя кода не много и так наверно проще
 */

class TimeListAdapter(private val listOfTimes: List<TimeNote> = ArrayList(), private val m: Float = 1f) : BaseAdapter() {
    lateinit var context: Context
    private val Int.dp: Int get() = this.dpf.toInt()
    private val Int.dpf: Float get() = this * context.resources.displayMetrics.density

    override fun getView(i: Int, v: View?, parent: ViewGroup?): View {
        context = parent!!.context
        return with(context) {
             constraintLayout {

                val timeTextView = textView {
                    text = listOfTimes[i].time
//                    backgroundColorResource = R.color.c_b
                    textSize = 20f
                    padding = dip(4)
                    typeface = Typeface.DEFAULT_BOLD
                }

                val dateTextView = textView {
                    var df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
                    val date = df.parse(listOfTimes[i].dateTime)
                    df = SimpleDateFormat("dd/MM/yyyy", Locale.US)
                    text = df.format(date)
                    textSize = 10f
                    padding = dip(8)
                }

                val scrambleImage = imageView(R.drawable.ic_scramble) {

                }.lparams {margin = 8.dp}


                val commentText = textView() {
                    text = listOfTimes[i].comment
                    textSize = 8f
                }.lparams {leftMargin = 16.dp}
//                val commentImage = imageView(R.drawable.ic_comment) {
//                     visibility = if (listOfTimes[i].comment == "") {
//                         View.GONE
//                     } else {
//                         View.VISIBLE
//                     }
//                }.lparams {rightMargin = 8.dp}

                 val scrambleText = textView {
                    text = listOfTimes[i].scramble
                    textSize = 10f
                }.lparams {leftMargin = 4.dp}

                 constraints {
                     timeTextView.connect( TOPS of parentId,
                             HORIZONTAL of parentId)
                     dateTextView.connect(TOPS of parentId,
                             RIGHTS of parentId)
                     scrambleImage.connect(LEFTS of parentId,
                             TOP to BOTTOM of timeTextView,
                             BOTTOMS of parentId)
                     scrambleText.connect(LEFT to RIGHT of scrambleImage,
                             TOP to BOTTOM of timeTextView,
                             BOTTOMS of parentId)
                     commentText.connect(
                             TOP to BOTTOM of timeTextView,
                             LEFT to RIGHT of scrambleText,
                             BOTTOMS of parentId)
                 }
             }
        }
    }

    override fun getItem(position: Int): TimeNote {
        return listOfTimes[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return listOfTimes.size
    }

}