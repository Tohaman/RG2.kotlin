package ru.tohaman.rg2.adapters

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.f2l_list_view_item.view.*
import ru.tohaman.rg2.R
import ru.tohaman.rg2.data.ListPager
import ru.tohaman.rg2.ui.inflate
import ru.tohaman.rg2.ui.listen


/**
 * Created by anton on 25.09.18. Адаптер для listview содержащего только превьюшки ситуаций включает в себя сразу и UI
 * хотя можно наверно сделать в getView() return listUI()
 * а ListUI сделать наследником AnkoComponentEx, хотя кода не много и так наверно проще
 */

class MyOnlyImageListAdapter(private val listOfLP: ArrayList<ListPager> = ArrayList(),
                             private var onClickListener: View.OnClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflatedView = parent.inflate(R.layout.f2l_list_view_item, false)
        return ItemHolder(inflatedView).listen { _, _ ->  }
    }

    override fun getItemCount(): Int {
        return listOfLP.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder.itemView.leftListViewImage.tag = position
        holder.itemView.leftListViewImage.setImageResource(listOfLP[position].icon)
        holder.itemView.leftListViewImage.setOnClickListener {
            onClickListener.onClick(holder.itemView.leftListViewImage)
        }
    }

    class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        private var imageView = itemView
//        private var image: Int? = null

    }
}