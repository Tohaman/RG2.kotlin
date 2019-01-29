package ru.tohaman.rg2.activities

import android.os.Bundle
import android.util.Log
import ru.tohaman.rg2.*
import ru.tohaman.rg2.DebugTag.TAG
import ru.tohaman.rg2.util.saveInt2SP
import ru.tohaman.rg2.util.saveString2SP

/*
    Активность которая вызывается для перехода по ссылкам внутри программы, меняет значения
    в SharedPreference (saveInt2SP, saveString2SP), тем самым вызывая колбэки в основных активностях.
*/

class ShowPagerActivity : MyDefaultActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fragment)
        Log.v (TAG, "ShowPagerActivity Start")
        val uri = this.intent?.data
        var phase = "BEGIN"
        var item = "0"

        // Если вызван с параметром, то взять параметры из ссылки
        if (uri != null) {
            phase = uri.getQueryParameter("phase")!!
            item = intent.data!!.getQueryParameter("item")!!
        }
        saveInt2SP(item.toInt(), "startId", this)
        saveString2SP(phase, "startPhase", this)
        finish()
    }

}
