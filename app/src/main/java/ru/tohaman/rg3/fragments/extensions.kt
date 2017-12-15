package ru.tohaman.rg3.fragments

import android.content.Context
import android.os.Build
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import android.view.ViewManager
import android.widget.TextView
import com.google.android.youtube.player.YouTubeThumbnailView
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.custom.ankoView
import org.jetbrains.anko.textColor
import org.jetbrains.anko.textView


//вот таким хитрым способом используя функции-расширения kotlin
// добавляем YouTubeThumbnailView в Anko, чтобы можно было вызвать в Анко: youTubeThumbnailView {... }

inline fun ViewManager.youTubeThumbnailView (init: YouTubeThumbnailView.() -> Unit): YouTubeThumbnailView {
    return ankoView({ YouTubeThumbnailView(it) }, 0, init)
}
