package ru.tohaman.rg3.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.support.v4.withArguments

class FragmentPagerItem : Fragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = FragmentPagerItemtUI<Fragment>().createView(AnkoContext.create(context, this))
        val message = arguments.getString("message")
        (view.findViewById(FragmentPagerItemtUI.Ids.textViewFragmentMessage) as TextView).text = message
        return view
    }

    companion object {
        fun newInstance(message: String): FragmentPagerItem {
            return FragmentPagerItem().withArguments("message" to message)
        }
    }
}
