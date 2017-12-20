package ru.tohaman.rg3.adapters

import android.content.Context
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import ru.tohaman.rg3.R
import ru.tohaman.rg3.data.CubeAzbuka
import ru.tohaman.rg3.ui.SquareRelativeLayout
import ru.tohaman.rg3.ui.SquareTextView


class MyGridAdapter (val context: Context, val layout: Int, val gridList: List<CubeAzbuka>) : BaseAdapter() {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        var grid: View
        grid = if (convertView == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            inflater.inflate(layout, parent, false)
        } else {
            convertView
        }

//        val textView = grid.findViewById(R.id.grid_text) as SquareTextView
//        textView.text = gridList[position].letter
//        textView.setBackgroundColor(gridList[position].color)
//        val myRelativeLayout = grid.findViewById(R.id.grid_main_layout) as SquareRelativeLayout
//
//        // Если символ не задан, значит клетку делаем прозрачной (цвета, который задан в gridList
//        // если клетка кубика, то лэйаут делаем черным.
//        if (gridList[position].letter == "") {
//            myRelativeLayout.setBackgroundColor(gridList[position].color)
//        } else {
//            myRelativeLayout.setBackgroundColor(ContextCompat.getColor(grid.context, R.color.black))
//        }

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

}