package ru.tohaman.rg3.fragments

import android.view.ViewManager
import com.google.android.youtube.player.YouTubeThumbnailView
import org.jetbrains.anko.custom.ankoView


//вот таким хитрым способом используя функции-расширения kotlin
// добавляем YouTubeThumbnailView в Anko, чтобы можно было вызвать в Анко: youTubeThumbnailView {... }

inline fun ViewManager.youTubeThumbnailView (init: YouTubeThumbnailView.() -> Unit): YouTubeThumbnailView {
    return ankoView({ YouTubeThumbnailView(it) }, 0, init)
}
