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
import com.google.android.youtube.player.*
import org.jetbrains.anko.browse
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
 * </intent-filter>
*/

class YouTubeActivity : YouTubeBaseActivity(), YouTubePlayer.OnInitializedListener {
    //https://www.stacktips.com/tutorials/android/youtube-android-player-api-example
    //https://developers.google.com/youtube/v3/code_samples/java - подписаться на канал

    private var date: Date? = null
    private var videoId = ""
    private val RECOVERY_DIALOG_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Включаем поддержку векторной графики на устройствах ниже Лилипопа (5.0)
        //AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        //Log.v (DebugTag.TAG, "YouTubeActivity onCreate")
        // Проверяем значения из настроек, выключать экран или нет при прсмотре видео
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        val sleepOnYouTube = sp.getBoolean(IS_VIDEO_SCREEN_ON, false)
        if (sleepOnYouTube) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        //Log.v (DebugTag.TAG, "YouTubeActivity преобразуем время")
        var time: String
        intent.data!!.getQueryParameter("time").let { time = it!! }
        intent.data!!.getQueryParameter("link").let { videoId = it!! }

        val result = YouTubeApiServiceUtil.isYouTubeApiServiceAvailable(this)
        if (result == YouTubeInitializationResult.SUCCESS) {
            startActivity(
                YouTubeStandalonePlayer.createVideoIntent(this, DEVELOPER_KEY, videoId, stringToTimeMillis(time), true, true)
            )
        } else {
            time = Regex(":").replace(time,"m")
            browse("https://youtu.be/$videoId" + "?t=$time" + "s")
        }
        finish()
    }

    override fun onInitializationSuccess(p0: YouTubePlayer.Provider?, p1: YouTubePlayer?, p2: Boolean) {
        p1?.loadVideo(DEVELOPER_KEY)
    }

    override fun onInitializationFailure(p0: YouTubePlayer.Provider?, p1: YouTubeInitializationResult?) {
        Log.v(DebugTag.TAG, p1.toString())
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
//        super.onActivityResult(requestCode, resultCode, data)
//        Log.v (DebugTag.TAG, "YouTubeActivity onActivityResult")
//        if (requestCode == RECOVERY_DIALOG_REQUEST) {
//                playerView.initialize(DEVELOPER_KEY, this)
//        }
//    }

//    private fun canResolveIntent(intent: Intent): Boolean {
//        Log.v (DebugTag.TAG, "Установлен ли в системе плеер?")
//        val resolveInfo = this.packageManager.queryIntentActivities(intent, 0)
//        return resolveInfo != null && !resolveInfo.isEmpty()
//    }

    //Преобразует строку вида 0:25 в милисекунды (25000)
    private fun stringToTimeMillis(text: String): Int {
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
