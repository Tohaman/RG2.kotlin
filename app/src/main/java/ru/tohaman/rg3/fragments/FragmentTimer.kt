package ru.tohaman.rg3.fragments


import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.UI
import org.jetbrains.anko.support.v4.withArguments
import ru.tohaman.rg3.DebugTag

import ru.tohaman.rg3.R
import ru.tohaman.rg3.listpager.ListPager


class FragmentTimer : Fragment() {


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        //return inflater!!.inflate(R.layout.fragment_timer, container, false)

        val view = UI {
            linearLayout {
//                constraintLayout {
//                    textView {
//                        text = "shgdajsgh"
//                    }
//                }
            }
        }.view
        return view
    }

    fun getColorFromResourses (colorRes:Int):Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            resources.getColor(colorRes,null)
        } else {
            @Suppress("DEPRECATION")
            resources.getColor(colorRes)
        }
    }

    companion object {
        fun newInstance(lp: ListPager): FragmentPagerItem {
            return FragmentPagerItem().withArguments("title" to lp.title,
                    "topImage" to lp.icon,
                    "desc" to lp.description,
                    "url" to lp.url)
        }
    }
}
