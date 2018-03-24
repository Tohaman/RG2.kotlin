package ru.tohaman.rg2.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatDelegate
import android.util.Log
import android.view.WindowManager
import org.jetbrains.anko.ctx
import ru.tohaman.rg2.DebugTag.TAG
import ru.tohaman.rg2.R
import ru.tohaman.rg2.fragments.FragmentTimer
import ru.tohaman.rg2.util.getThemeFromSharedPreference

class TimerActivity : AppCompatActivity() {

    private val FRAGMENT_INSTANCE_NAME = "fragment"
    var fragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(getThemeFromSharedPreference(ctx))
        setContentView(R.layout.activity_fragment)
        //Включаем поддержку векторной графики на устройствах ниже Лилипопа (5.0)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        Log.v (TAG, "TimerActivity Start")
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        super.onCreate(savedInstanceState)

        val fm = supportFragmentManager
        fragment = fm.findFragmentByTag(FRAGMENT_INSTANCE_NAME)
        if (fragment == null) {
            fragment = FragmentTimer()
            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment, FRAGMENT_INSTANCE_NAME)
                    .commit()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val timerFragment = supportFragmentManager.findFragmentByTag(FRAGMENT_INSTANCE_NAME) as FragmentTimer

        timerFragment.backButtonWasPressed()
    }

}
