package ru.tohaman.rg2.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AppCompatDelegate
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
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fragment)
        Log.v (TAG, "ScrambleActivity Start")
        val transaction : FragmentTransaction? = supportFragmentManager.beginTransaction()
        transaction?.replace(R.id.fragment_container, FragmentScrambleGen())?.commit()
    }

    override fun onScrambleGenInteraction(button: String) {
        super.onScrambleGenInteraction(button)
        if (button == "AZBUKA") {
            //TODO Сделать нормальный обработчик ссылок на генератор скрамблов
            toast("Для выбора азбуки зайдите в генератор скрамблов из основного меню программы.")
        }
    }
}
