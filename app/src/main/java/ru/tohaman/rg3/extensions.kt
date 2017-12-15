package ru.tohaman.rg3

import android.content.Context
import android.os.Build
import android.support.constraint.ConstraintLayout
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


abstract class AnkoComponentEx<in T>: AnkoComponent<T> {

    protected lateinit var context: Context
    protected val Int.dp: Int get() = this.dpf.toInt()
    protected val Int.dpf: Float get() = this * context.resources.displayMetrics.density

    protected fun beginDelayedTransition(sceneRoot: ViewGroup) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            TransitionManager.beginDelayedTransition(sceneRoot)
        }
    }

    protected fun ViewManager.defaultTextView(text: CharSequence, init: TextView.() -> Unit): TextView {
        return textView(text) {
            textColor = 0xFF000000.toInt()
            textSize = 20F
            init()
        }
    }

    final override fun createView(ui: AnkoContext<T>): View {
        this.context = ui.ctx
        return create(ui)
    }

    abstract protected fun create(ui: AnkoContext<T>): View

}

//вот таким хитрым способом используя функции-расширения kotlin
// добавляем YouTubeThumbnailView в Anko, чтобы можно было вызвать в Анко: youTubeThumbnailView {... }

inline fun ViewManager.youTubeThumbnailView (init: YouTubeThumbnailView.() -> Unit): YouTubeThumbnailView {
    return ankoView({ YouTubeThumbnailView(it) }, 0, init)
}

inline fun ViewManager.constraintLayout (init: ConstraintLayout.() -> Unit): ConstraintLayout {
    return ankoView({ ConstraintLayout(it) }, 0, init)
}
