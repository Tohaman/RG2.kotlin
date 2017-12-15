package ru.tohaman.rg3.ui

import android.graphics.Color
import android.os.Build
import android.view.View
import android.widget.LinearLayout
import org.jetbrains.anko.*
import ru.tohaman.rg3.AnkoComponentEx
import ru.tohaman.rg3.R
import ru.tohaman.rg3.ankoconstraintlayout.constraintLayout


/**
 * Created by Test on 15.12.2017.
 */
class TimerUI<Fragment> : AnkoComponentEx<Fragment>() {

    override fun create(ui: AnkoContext<Fragment>): View = with(ui) {
        linearLayout {
            constraintLayout {
                val leftpad = linearLayout {
                    backgroundColor = getColorFromResourses(R.color.blue)
                }.lparams(120.dp,120.dp)
                val rigthpad = linearLayout {
                    backgroundColor = getColorFromResourses(R.color.red)
                }.lparams(120.dp,120.dp)
//                val name = textView("David")
//                val surname = textView("Khol") {
//                    textColor = Color.BLUE
//                }

                constraints {
                    leftpad.connect( RIGHT to LEFT of rigthpad with 16.dp,
                            TOPS of parentId with 16.dp,
                            BOTTOMS of parentId with 8.dp,
                            LEFTS of parentId with 16.dp
                            )
                    rigthpad.connect( BOTTOMS of parentId with 20.dp, LEFTS of parentId with 20.dp)
//                    name.connect(
//                            STARTS of parentId with 16.dp,
//                            TOPS of parentId with 16.dp
//                    )
//                    surname.connect(
//                            TOP to BOTTOM of name,
//                            STARTS of name
//                    )
                }
            }.lparams(matchParent, matchParent)
        }
    }


    fun getColorFromResourses (colorRes:Int):Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context.resources.getColor(colorRes,null)
        } else {
            @Suppress("DEPRECATION")
            context.resources.getColor(colorRes)
        }
    }

}
