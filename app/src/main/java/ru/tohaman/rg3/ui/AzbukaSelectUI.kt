package ru.tohaman.rg3.ui

import android.graphics.drawable.Drawable
import android.os.Build
import android.text.Html
import android.util.Log
import android.view.View
import org.jetbrains.anko.*
import ru.tohaman.rg3.DebugTag.TAG
import kotlinx.android.synthetic.main.fragment_azbuka.view.*
import org.jetbrains.anko.sdk15.coroutines.onClick
import ru.tohaman.rg3.*
import ru.tohaman.rg3.adapters.MyGridAdapter
import ru.tohaman.rg3.data.ListPagerLab
import ru.tohaman.rg3.util.prepareAzbukaToShowInGridView
import ru.tohaman.rg3.util.spannedString


/**
 * Created by Test on 15.12.2017. Интерфейс (UI) выбора Азбуки
 * в этом фрагменте UI обходимся без findViewById, импортируем xml лэйаут
 * через [include<View>(R.layout.fragment_azbuka)] и далее создаем обработчики
 * кнопок, адаптер к GridView и т.п.
 */
class AzbukaSelectUI<in Fragment> : AnkoComponentEx<Fragment>()  {
    private lateinit var gridAdapter : MyGridAdapter

    override fun create(ui: AnkoContext<Fragment>): View = with(ui) {
        Log.v(TAG, "AzbukaSelectUI create start with ScreenSize = ")

        val imgGetter  = Html.ImageGetter { _ ->
            val drawable: Drawable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                resources.getDrawable (R.drawable.ic_warning, null)
            } else {
                @Suppress("DEPRECATION")
                resources.getDrawable (R.drawable.ic_warning)
            }
            drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
            drawable
        }
        val listPagerLab = ListPagerLab.get(ctx)

        linearLayout {
            linearLayout {
                include<View>(R.layout.fragment_azbuka) {
//                    backgroundColorResource = R.color.blue
                }.lparams(matchParent, matchParent)

                val gridList = prepareAzbukaToShowInGridView(listPagerLab.getCustomAzbuka())
                gridAdapter = MyGridAdapter(ctx, gridList)
                azbuka_gridView.adapter = gridAdapter

                button_max_azbuka.onClick {
                    gridAdapter.gridList = prepareAzbukaToShowInGridView(listPagerLab.getMaximAzbuka())
                    gridAdapter.notifyDataSetChanged()
                }

                button_my_azbuka.onClick {
                    gridAdapter.gridList = prepareAzbukaToShowInGridView(listPagerLab.getMyAzbuka())
                    gridAdapter.notifyDataSetChanged()
                }

                button_save_azbuka.onClick {
                    var azbuka = getAzbukaFromAdapter(gridAdapter)
                    val st : String = azbuka.joinToString (" ","", "")
                    Log.v(TAG, "Azbuka = $azbuka")
                }

                button_load_azbuka.onClick {
                    gridAdapter.gridList = prepareAzbukaToShowInGridView(listPagerLab.getCustomAzbuka())
                    gridAdapter.notifyDataSetChanged()
                }

                azbuka_textView.text = spannedString(resources.getString(R.string.azbuka2), imgGetter)
            }.lparams(matchParent, matchParent)
        }

    }

    private fun getAzbukaFromString(st: String) = st.split(" ") as ArrayList<String>

    private fun getAzbukaFromAdapter(gridAdapter: MyGridAdapter) = gridAdapter.gridList.indices
                .filter { (gridAdapter.getItem(it) != "") and (gridAdapter.getItem(it) != "-")  }
                .map { gridAdapter.getItem(it) }

}
