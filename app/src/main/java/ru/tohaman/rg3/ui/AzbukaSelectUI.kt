package ru.tohaman.rg3.ui

import android.graphics.drawable.Drawable
import android.os.Build
import android.text.Html
import android.util.Log
import android.view.View
import org.jetbrains.anko.*
import ru.tohaman.rg3.DebugTag.TAG
import kotlinx.android.synthetic.main.fragment_azbuka.view.*
import ru.tohaman.rg3.*
import ru.tohaman.rg3.adapters.MyGridAdapter
import ru.tohaman.rg3.util.prepareCubeToShowInGridView
import ru.tohaman.rg3.util.resetCube
import ru.tohaman.rg3.util.spannedString


/**
 * Created by Test on 15.12.2017. Интерфейс выбора Азбуки
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

        linearLayout {
            linearLayout {
                //backgroundColorResource = R.color.blue
                include<View>(R.layout.fragment_azbuka) {
                    //                    backgroundColorResource = R.color.blue
                }.lparams(matchParent, matchParent)
                val gridList = prepareCubeToShowInGridView(resetCube())
                gridAdapter = MyGridAdapter(ctx, gridList)
                azbuka_gridView.adapter = gridAdapter

                azbuka_textView.text = spannedString(resources.getString(R.string.azbuka2), imgGetter)
            }.lparams(matchParent, matchParent)
        }

    }
}
