package ru.tohaman.rg3.ui

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.content.res.Configuration.*
import android.graphics.Typeface
import android.os.Build
import android.os.Handler
import android.preference.PreferenceManager
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import org.jetbrains.anko.*
import ru.tohaman.rg3.DebugTag.TAG
import ru.tohaman.rg3.ankoconstraintlayout.constraintLayout
import android.media.SoundPool
import android.media.AudioManager
import ru.tohaman.rg3.*


/**
 * Created by Test on 15.12.2017. Интерфейс таймера
 */
class AzbukaSelectUI<in Fragment> : AnkoComponentEx<Fragment>()  {

    override fun create(ui: AnkoContext<Fragment>): View = with(ui) {
        Log.v (TAG, "AzbukaSelectUI create start with ScreenSize = ")
        linearLayout {
            constraintLayout {
                backgroundColorResource = R.color.blue

            }.lparams(matchParent, matchParent)
        }
    }

}
