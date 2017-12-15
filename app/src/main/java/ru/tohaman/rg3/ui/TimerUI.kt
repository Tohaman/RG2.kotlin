package ru.tohaman.rg3.ui

import android.os.Build
import android.view.View
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.linearLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.textView
import ru.tohaman.rg3.AnkoComponentEx
import ru.tohaman.rg3.ankoconstraintlayout.constraintLayout


/**
 * Created by Test on 15.12.2017.
 */
class TimerUI<Fragment> : AnkoComponentEx<Fragment>() {

    override fun create(ui: AnkoContext<Fragment>): View = with(ui) {
        linearLayout {
            constraintLayout {
                val name = textView("David")
                val surname = textView("Khol")

                constraints {
                    name.connect(
                            STARTS of parentId with 16.dp,
                            TOPS of parentId with 16.dp
                    )
                    surname.connect(
                            TOP to BOTTOM of name,
                            STARTS of name
                    )
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
