package ru.tohaman.rg2.adapters

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.widget.TextView
import org.jetbrains.anko.*
import ru.tohaman.rg2.R
import ru.tohaman.rg2.data.CubeAzbuka
import ru.tohaman.rg2.ui.squareRelativeLayout
import ru.tohaman.rg2.ui.SquareRelativeLayout

/**
 * Created by anton on 27.11.17. Адаптер для GridView включает в себя сразу и UI для одного элемента
 * хотя можно наверно сделать в getView() return gridUI() как например в SlidingTabsAdapter
 * в отличии от MyListAdapter тут есть ViewHolder
 */


class MyGridAdapter (val context: Context, var gridList: List<CubeAzbuka>) : BaseAdapter() {
    private val Int.dp: Int get() = this.dpf.toInt()
    private val Int.dpf: Float get() = this * context.resources.displayMetrics.density

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        var grid = convertView
        val holder : ViewHolder

        if (grid == null) {
            holder = ViewHolder()
            grid = with (context) {
                linearLayout {
                    holder.linLay = linearLayout {                  //Линлэйаут во всю клетку
                        holder.relLay = squareRelativeLayout {      //Квадратный RelativeLayout с отступом в 2dp(черная рамка)
                            gravity = Gravity.CENTER
                            holder.txtView = textView {             //В центре буква (если нужна)
                                gravity = Gravity.CENTER
                                textColorResource = R.color.black
                                text = " "
                            }
                        }.lparams(matchParent, matchParent) { margin = 2.dp }
                    }.lparams(matchParent, matchParent)
                }
            }
            grid.tag = holder
        } else {
            holder = grid.tag as ViewHolder
        }
        // Если символ не задан, значит это пустая клетка и делаем ее прозрачной
        // если клетка кубика, то лэйаут делаем черным.
        if (gridList[position].letter == "") {
            holder.linLay!!.backgroundColorResource = gridList[position].color
        } else {
            holder.linLay!!.backgroundColorResource = R.color.black
        }

        // для всех клеток внутренности делаем цвета из gridList и букву проставляем оттуда же
        // для пустых клеток они должны быть прозрачными и пустыми.
        holder.txtView!!.text = gridList[position].letter
        holder.relLay!!.backgroundColorResource = gridList[position].color

        return grid
    }


    override fun getCount(): Int {
        return gridList.size
    }

    // возвращает содержимое выделенного элемента списка
    override fun getItem(position: Int): String {
        return gridList[position].letter
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    class ViewHolder {
        internal var linLay : LinearLayout? = null
        internal var relLay : SquareRelativeLayout? = null
        internal var txtView : TextView? = null
    }


}