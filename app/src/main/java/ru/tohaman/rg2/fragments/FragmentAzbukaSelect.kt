package ru.tohaman.rg2.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.ctx
import ru.tohaman.rg2.DebugTag
import ru.tohaman.rg2.ui.AzbukaSelectUI

/**
 * Фрагмент с выбором Азбуки, UI создается в [onCreateView]
 * в этом UI вся логики работы фрагмента
 * фабричный метод [newInstance] для создания фрагмента
 */

class FragmentAzbukaSelect : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return AzbukaSelectUI<Fragment>().createView(AnkoContext.create (ctx, this))
}

    companion object {
        fun newInstance(): FragmentAzbukaSelect {
            Log.v(DebugTag.TAG, "FragmentAzbukaSelect newInstance")
            return FragmentAzbukaSelect()
        }
    }


}

