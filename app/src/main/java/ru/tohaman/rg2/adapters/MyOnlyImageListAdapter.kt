package ru.tohaman.rg2.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.oll_list_view_item.view.*
import ru.tohaman.rg2.R
import ru.tohaman.rg2.data.ListPager

/**
 * Created by anton on 25.09.18. Адаптер для listview содержащего только превьюшки ситуаций включает в себя сразу и UI
 * хотя можно наверно сделать в getView() return listUI()
 * а ListUI сделать наследником AnkoComponentEx, хотя кода не много и так наверно проще
 */

class MyOnlyImageListAdapter(private val listOfLP: ArrayList<ListPager> = ArrayList(),
                             private var context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return ItemHolder (LayoutInflater.from(context).inflate(R.layout.oll_list_view_item, parent, false))
        }

        override fun getItemCount(): Int {
            return listOfLP.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            holder.itemView.leftListViewImage.setImageResource(listOfLP[position].icon)
        }

        class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            //internal var imageView : ImageView? = null
            //val imageView = itemView.leftListViewImage
        }
    }