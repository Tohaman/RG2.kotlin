package ru.tohaman.rg2.fragments


import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Html
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.fragment_azbuka.view.*
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk15.coroutines.onClick
import org.jetbrains.anko.support.v4.ctx
import ru.tohaman.rg2.DebugTag
import ru.tohaman.rg2.R
import ru.tohaman.rg2.adapters.MyGridAdapter
import ru.tohaman.rg2.data.ListPagerLab
import ru.tohaman.rg2.ui.AnkoComponentEx
import ru.tohaman.rg2.ui.AzbukaSelectUI
import ru.tohaman.rg2.util.prepareAzbukaToShowInGridView
import ru.tohaman.rg2.util.spannedString

/**
 * Фрагмент с выбором Азбуки, UI создается в [onCreateView]
 * в этом UI вся логики работы фрагмента
 * фабричный метод [newInstance] для создания фрагмента
 */

class FragmentAbout : Fragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return AboutUI<Fragment>().createView(AnkoContext.create(ctx, this))
}

    companion object {
        fun newInstance(): FragmentAbout {
            Log.v(DebugTag.TAG, "FragmentAbout newInstance")
            return FragmentAbout()
        }
    }

}

class AboutUI<in Fragment> : AnkoComponentEx<Fragment>() {
    private lateinit var gridAdapter: MyGridAdapter

    override fun create(ui: AnkoContext<Fragment>): View = with(ui) {
        Log.v(DebugTag.TAG, "AboutUI create start")

        linearLayout {
            linearLayout {
                orientation = LinearLayout.VERTICAL
                textView {
                    text = ""
                }
            }
        }


    }

}


