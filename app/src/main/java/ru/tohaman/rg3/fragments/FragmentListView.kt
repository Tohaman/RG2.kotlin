package ru.tohaman.rg3.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import org.jetbrains.anko.*

import ru.tohaman.rg3.R


class FragmentListView : Fragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = ListViewUI<Fragment>().createView(AnkoContext.create(context, this))

        return view
    }


}// Required empty public constructor

class ListViewUI<Fragment> : AnkoComponent<Fragment> {

    override fun createView(ui: AnkoContext<Fragment>) = with(ui) {
        linearLayout {
            gravity = Gravity.CENTER
            orientation = LinearLayout.VERTICAL
            listView {
                adapter
            }
        }
    }

    object Ids {
        val textViewFragmentMessage = 1
    }
}