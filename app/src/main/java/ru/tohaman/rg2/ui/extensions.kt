package ru.tohaman.rg2.ui

import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.support.annotation.LayoutRes
import android.support.v7.widget.RecyclerView
import android.transition.TransitionManager
import android.view.*
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import com.google.android.youtube.player.YouTubeThumbnailView
import org.jetbrains.anko.*
import org.jetbrains.anko.custom.ankoView
import ru.tohaman.rg2.R


abstract class AnkoComponentEx<in T>: AnkoComponent<T> {

    protected lateinit var context: Context
    protected val Int.dp: Int get() = this.dpf.toInt()
    private val Int.dpf: Float get() = this * context.resources.displayMetrics.density

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

    protected fun ViewManager.defaultButton(txt: CharSequence, init: Button.() -> Unit): Button {
        return button {
            text = txt
            height = wrapContent
            width = matchParent
            init()
        }
    }

    protected fun ViewManager.coloredButton(txt: CharSequence, init: Button.() -> Unit): Button {
        return button {
            text = txt
            textSize = 16F
//            padding = 20.dp
            height = matchParent
            width = matchParent
//            backgroundResource = R.color.colorAccent
            init()
        }
    }


    final override fun createView(ui: AnkoContext<T>): View {
        this.context = ui.ctx
        return create(ui)
    }

    protected abstract fun create(ui: AnkoContext<T>): View

}

//вот таким хитрым способом используя функции-расширения kotlin
// добавляем YouTubeThumbnailView в Anko, чтобы можно было вызвать в Анко: youTubeThumbnailView {... }

inline fun ViewManager.youTubeThumbnailView (init: YouTubeThumbnailView.() -> Unit): YouTubeThumbnailView {
    return ankoView({ YouTubeThumbnailView(it) }, 0, init)
}

inline fun ViewManager.styledButton(styleRes: Int = 0, init: Button.() -> Unit): Button {
    return ankoView({
        if (styleRes == 0) Button(it)
            else Button(ContextThemeWrapper(it, styleRes), null, 0) }, 0) { init() }
}

//inline fun ViewManager.styledButton(styleRes: Int = 0): Button = styledButton(styleRes) {}


inline fun ViewManager.squareRelativeLayout(init: RelativeLayout.() -> Unit) : SquareRelativeLayout {
    return ankoView({SquareRelativeLayout(it)},0,init)
}

fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
}

fun <T : RecyclerView.ViewHolder> T.listen(event: (position: Int, type: Int) -> Unit): T {
    itemView.setOnClickListener {
        event.invoke(adapterPosition, itemViewType)
    }
    return this
}