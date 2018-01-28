package ru.tohaman.rg2.activitys

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.FragmentTransaction
import android.util.Log
import android.view.WindowManager
import org.jetbrains.anko.ctx
import org.jetbrains.anko.toast
import ru.tohaman.rg2.DebugTag.TAG
import ru.tohaman.rg2.IS_SCREEN_ALWAYS_ON
import ru.tohaman.rg2.R
import ru.tohaman.rg2.fragments.FragmentScrambleGen
import ru.tohaman.rg2.util.getThemeFromSharedPreference

class ScrambleActivity : AppCompatActivity(), FragmentScrambleGen.OnSrambleGenInteractionListener  {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(getThemeFromSharedPreference(ctx))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fragment)
        Log.v (TAG, "ScrambleActivity Start")
        val transaction : FragmentTransaction? = supportFragmentManager.beginTransaction()
        transaction?.replace(R.id.fragment_container, FragmentScrambleGen())?.commit()

        val sp = PreferenceManager.getDefaultSharedPreferences(ctx)
        val isScreenAlwaysOn = sp.getBoolean(IS_SCREEN_ALWAYS_ON, false)
        if (isScreenAlwaysOn) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    override fun onScrambleGenInteraction(button: String) {
        super.onScrambleGenInteraction(button)
        if (button == "AZBUKA") {
            //TODO Сделать нормальный обработчик ссылок на генератор скрамблов
            toast("Для выбора азбуки зайдите в генератор скрамблов из основного меню программы.")
        }
    }
}
