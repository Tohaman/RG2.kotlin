package ru.tohaman.rg2.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.jetbrains.anko.*
import ru.tohaman.rg2.DebugTag
import ru.tohaman.rg2.ui.TestPllUI

/**
 * Фрагмент с выбором Азбуки, UI создается в [onCreateView]
 * в этом UI вся логики работы фрагмента
 * фабричный метод [newInstance] для создания фрагмента
 */

class FragmentTestPLL : Fragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return TestPllUI<Fragment>().createView(AnkoContext.create(context, this))
}

    companion object {
        fun newInstance(): FragmentTestPLL {
            Log.v(DebugTag.TAG, "FragmentTestPLL newInstance")
            return FragmentTestPLL()
        }
    }


}

