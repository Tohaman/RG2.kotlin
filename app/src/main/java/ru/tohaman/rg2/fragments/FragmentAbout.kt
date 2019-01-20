package ru.tohaman.rg2.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.text.Html
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.button_colored.view.*
import kotlinx.android.synthetic.main.button_subscribe.view.*
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk15.coroutines.onClick
import org.jetbrains.anko.support.v4.ctx
import ru.tohaman.rg2.BuildConfig
import ru.tohaman.rg2.DebugTag
import ru.tohaman.rg2.R
import ru.tohaman.rg2.adapters.MyGridAdapter
import ru.tohaman.rg2.ui.AnkoComponentEx
import ru.tohaman.rg2.util.spannedString

/**
 * Фрагмент с выбором Азбуки, UI создается в [onCreateView]
 * в этом UI вся логики работы фрагмента
 * фабричный метод [newInstance] для создания фрагмента
 */

class FragmentAbout : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
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

    override fun create(ui: AnkoContext<Fragment>): View = with(ui) {
        Log.v(DebugTag.TAG, "AboutUI create start")

        linearLayout {
            scrollView {
                linearLayout {
                    orientation = LinearLayout.VERTICAL
                    include<Button>(R.layout.button_subscribe) {
                        text = context.getString(R.string.subscibeButton)
                        textSize = 16F
                        padding = 20.dp
                    }.lparams { setMargins(0, dip(20), 0, dip(20)) }
                    val subscibeButton = btn_subscribe

                    textView {
                        var txt = "<html><body style=\"text-align:justify\"> %s </body></html>"
                        val st: String = resources.getString(R.string.about)
                        val imgGetter = Html.ImageGetter { ContextCompat.getDrawable(ctx, 0) }
                        txt = String.format(txt, st)
                        text = spannedString(txt, imgGetter)
                        // Делаем ссылки кликабельными
                        movementMethod = LinkMovementMethod.getInstance()
                    }

                    include<Button>(R.layout.button_colored) {
                        text = context.getString(R.string.fiveStarButtonText)
                        textSize = 16F
                        padding = 20.dp
                    }.lparams { setMargins(0, dip(20), 0, dip(20)) }
                    val fiveStarButton = btn_colored


//                    TODO Раскоментировать когда `themedButton` is fixed.
//                    val fiveStarButton = themedButton(theme = R.style.AppTheme_Button_Colored) {
//                        text = context.getString(R.string.fiveStarButtonText)
//                        textSize = 16F
//                        padding = 20.dp
//                    }.lparams {setMargins(0,dip(20),0,dip(20))}

                    textView {
                        text = BuildConfig.VERSION_NAME
                        textSize = 8F
                    }
                    fiveStarButton.onClick {
                        val appPackageName = ctx.packageName // getPackageName() from Context or Activity object
                        if (!browse("market://details?id=$appPackageName", false)) {
                            browse("https://play.google.com/store/apps/details?id=$appPackageName")
                        }
                    }

                    subscibeButton.onClick {
                        browse("https://www.youtube.com/channel/UCpSUF7w376aCRRvzkoNoAfQ", false)
                    }
                }
            }.lparams (matchParent, matchParent){
                margin = dip(16)
            }
        }


    }

}


