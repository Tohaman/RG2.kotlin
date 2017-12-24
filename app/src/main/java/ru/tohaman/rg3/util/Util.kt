package ru.tohaman.rg3.util

import android.content.Context
import android.preference.PreferenceManager

/**
 * Created by Test on 22.12.2017. Различные утилиты
 */

fun saveBoolean2SP(value: Boolean, key: String, context: Context) {
    val sp = PreferenceManager.getDefaultSharedPreferences(context)
    val editor = sp.edit()
    editor.putBoolean(key, value)
    editor.apply() // подтверждаем изменения
}

fun saveInt2SP(value: Int, key: String, context: Context) {
    val sp = PreferenceManager.getDefaultSharedPreferences(context)
    val editor = sp.edit()
    editor.putInt(key, value)
    editor.apply() // подтверждаем изменения
}

fun saveString2SP(value : String, key: String, context: Context) {
    val sp = PreferenceManager.getDefaultSharedPreferences(context)
    val editor = sp.edit()
    editor.putString(key, value)
    editor.apply() // подтверждаем изменения
}

