package ru.tohaman.rg2.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubeStandalonePlayer
import org.jetbrains.anko.ctx
import ru.tohaman.rg2.DebugTag
import ru.tohaman.rg2.DeveloperKey.DEVELOPER_KEY
import ru.tohaman.rg2.IS_VIDEO_SCREEN_ON
import ru.tohaman.rg2.R
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Toha. Активность для воспроизведения YouTube видео по ссылкам из текста
 * В активность передается ссылка на видео и время с которого надо воспроизводить видео в виде
 * [Time 1:15](rg2://ytplay?time=1:15&link=Vt9dHndW7-E)
 * в манифесте надо для активности прописать <intent-filter>
 * <data android:scheme="rg2" android:host="ytplay"></data>
 * а манифест перед <application добавить строку></application><uses-permission android:name="android.permission.INTERNET"></uses-permission>
  </intent-filter> */

class YouTubeActivity : AppCompatActivity() {
    private var date: Date? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Включаем поддержку векторной графики на устройствах ниже Лилипопа (5.0)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        Log.v (DebugTag.TAG, "YouTubeActivity onCreate")
        // Проверяем значения из настроек, выключать экран или нет при прсмотре видео
        val sp = PreferenceManager.getDefaultSharedPreferences(ctx)
        val sleepOnYouTube = sp.getBoolean(IS_VIDEO_SCREEN_ON, false)
        if (sleepOnYouTube) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        Log.v (DebugTag.TAG, "YouTubeActivity преобразуем время")
        val text1 = intent.data!!.getQueryParameter("time")
        val videoId = intent.data!!.getQueryParameter("link")
        var intent = YouTubeStandalonePlayer.createVideoIntent(this, DEVELOPER_KEY, videoId, StringToTimeMillis(text1), true, true)
        if (intent != null) {
            if (canResolveIntent(intent)) {
                Log.v (DebugTag.TAG, "Установлен - запускаем StandAlone плеер c нужного времени")
                startActivityForResult(intent, REQ_START_STANDALONE_PLAYER)
            } else {
                Log.v (DebugTag.TAG, "Видимо нету Youtube плеера")
                YouTubeInitializationResult.SERVICE_MISSING.getErrorDialog(this, REQ_RESOLVE_SERVICE_MISSING).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.v (DebugTag.TAG, "YouTubeActivity onActivityResult")
        if (requestCode == REQ_START_STANDALONE_PLAYER && resultCode != Activity.RESULT_OK) {
            val errorReason = YouTubeStandalonePlayer.getReturnedInitializationResult(data)
            if (errorReason.isUserRecoverableError) {
                errorReason.getErrorDialog(this, 0).show()
            } else {
                val errorMessage = String.format(getString(R.string.error_player), errorReason.toString())
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            }
        }
        if (requestCode == REQ_START_STANDALONE_PLAYER && resultCode == Activity.RESULT_OK) {
            Log.v (DebugTag.TAG, "Просмотр завершился удачно")
            finish()
        }
    }

    private fun canResolveIntent(intent: Intent): Boolean {
        Log.v (DebugTag.TAG, "Установлен ли в системе плеер?")
        val resolveInfo = this.packageManager.queryIntentActivities(intent, 0)
        return resolveInfo != null && !resolveInfo.isEmpty()
    }

    private fun StringToTimeMillis(text: String): Int {
        val calendar = Calendar.getInstance()
        val format = SimpleDateFormat("m:s", Locale.ENGLISH)
        try {
            date = format.parse(text)
        } catch (ex: ParseException) {
            Log.v (DebugTag.TAG, "Это не должно произойти. Ошибка при преобразовании даты.")
            //но если произошло, то считаем что видео воспроизводится с начала возвращаем 0 милисек
            return 0
        }

        calendar.time = date
        val second = calendar.get(Calendar.SECOND)
        val minute = calendar.get(Calendar.MINUTE)
        return (minute * 60 + second) * 1000
    }

    companion object {
        //        private static String VIDEO_ID = "0TvO_rpG_aM";

        val REQ_START_STANDALONE_PLAYER = 101
        private val REQ_RESOLVE_SERVICE_MISSING = 2
    }

}
