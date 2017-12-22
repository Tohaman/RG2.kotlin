package ru.tohaman.rg3.util

import android.content.Context
import android.preference.PreferenceManager

/**
 * Created by Test on 22.12.2017. Различные утилиты
 */

fun saveBoolean2SP(bool : Boolean, st : String, context: Context) {
    val sp = PreferenceManager.getDefaultSharedPreferences(context)
    val editor = sp.edit()
    editor.putBoolean(st, bool)
    editor.apply() // подтверждаем изменения
}

fun saveInt2SP(int : Int, st : String, context: Context) {
    val sp = PreferenceManager.getDefaultSharedPreferences(context)
    val editor = sp.edit()
    editor.putInt(st, int)
    editor.apply() // подтверждаем изменения
}
