package ru.tohaman.rg3.ui

import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
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
        //толщина рамки в dp
        val m = 10
        //высота верхней части
        val h = 100
        val w = 220
        linearLayout {
            constraintLayout {
                backgroundColor = getColorFromResourses(R.color.blue)

                val leftPad = linearLayout {
                    backgroundColor = getColorFromResourses(R.color.dark_gray)
                }.lparams(0.dp,0.dp) {margin = m.dp}    //{setMargins(m.dp,m.dp,m.dp,m.dp)}
                val rigthPad = linearLayout {
                    backgroundColor = getColorFromResourses(R.color.dark_gray)
                }.lparams(0.dp,0.dp) {margin = m.dp}    //{setMargins(m.dp,m.dp,m.dp,m.dp)}

                val topLayout = linearLayout {
                    backgroundColor = getColorFromResourses(R.color.blue)
                }.lparams(w.dp,h.dp)

                val topInsideLayout = linearLayout {
                    backgroundColor = getColorFromResourses(R.color.dark_gray)
                }.lparams(0.dp,0.dp) {margin = m.dp}

                val timeLayout = linearLayout {
                    backgroundColor = getColorFromResourses(R.color.white)
                    textView {
                        text = "0:00:00"
                        textSize = 24F
                        padding = m.dp
                        typeface = Typeface.MONOSPACE
                        textColor = getColorFromResourses(R.color.black)
                    }
                }

                val leftCircle = imageView (R.drawable.timer_circle){

                }

                constraints {
                    val layouts = arrayOf (leftPad,rigthPad)
                    layouts.chainSpreadInside(RIGHT of parentId,LEFT of parentId)

                    leftPad.connect(LEFTS of parentId,
                            TOPS of parentId,
                            RIGHT to LEFT of rigthPad,
                            BOTTOMS of parentId
                    )
                    rigthPad.connect( LEFT to RIGHT of leftPad,
                            TOPS of parentId,
                            RIGHTS of parentId,
                            BOTTOMS of parentId
                    )
                    topLayout.connect(RIGHTS of parentId,
                            TOPS of parentId,
                            LEFTS of parentId)

                    topInsideLayout.connect(RIGHTS of topLayout,
                            TOPS of topLayout,
                            LEFTS of topLayout,
                            BOTTOMS of topLayout)

                    timeLayout.connect(RIGHTS of topLayout,
                            TOPS of topLayout,
                            LEFTS of topLayout,
                            BOTTOMS of topLayout)

                    leftCircle.connect( LEFTS of timeLayout)
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
