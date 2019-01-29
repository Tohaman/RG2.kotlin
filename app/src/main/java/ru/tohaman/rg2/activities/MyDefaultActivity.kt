package ru.tohaman.rg2.activities

import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import android.view.WindowManager
import ru.tohaman.rg2.IS_SCREEN_ALWAYS_ON
import ru.tohaman.rg2.util.getThemeFromSharedPreference

/**
 * Created by Test on 09.02.2018.
 * Задаем дефолтные параметры для всех активностей в программе
 */

abstract class MyDefaultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(getThemeFromSharedPreference(this))
        //Включаем поддержку векторной графики на устройствах ниже Лилипопа (5.0)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        //Настраиваем отключение экрана
        val isScreenAlwaysOn = sp.getBoolean(IS_SCREEN_ALWAYS_ON, false)
        if (isScreenAlwaysOn) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        super.onCreate(savedInstanceState)
    }
}