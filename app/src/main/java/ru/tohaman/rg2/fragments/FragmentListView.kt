package ru.tohaman.rg2.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.ListFragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import ru.tohaman.rg2.DebugTag

import ru.tohaman.rg2.R
import ru.tohaman.rg2.adapters.MyListAdapter
import ru.tohaman.rg2.data.ListPager
import ru.tohaman.rg2.data.ListPagerLab
import java.util.ArrayList
import android.graphics.drawable.ColorDrawable
import android.os.Build
import org.jetbrains.anko.support.v4.ctx


/**
 * FragmentListView - фрагмент в котором встроенный ListView
 * отображает лист этапов определенной фазы
 * Активити содержащая этот fragment должна имплементить
 * [FragmentListView.OnListViewInteractionListener] interface
 * для получения ответов от этого фрагмента.
 * Для создания экземпляра фрагмента используйте [FragmentListView.newInstance]
 */
class FragmentListView : ListFragment() {
    private var mPhase: String = "BEGIN"
    private var mListener: OnListViewInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.v (DebugTag.TAG, "FragmentListView onCreate")
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        loadSavedState()
        //Берем layout listview_4_fragment, чтобы небольшие литсвью располагались по центру фрагмента, а не по верхнему краю
        return inflater.inflate(R.layout.listview_4_fragment, null)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        Log.v (DebugTag.TAG, "FragmentListView onActivityCreated $mPhase")
        super.onActivityCreated(savedInstanceState)
        val listPagers : ArrayList<ListPager> = ListPagerLab.get(ctx).getPhaseList(mPhase)
        listAdapter = MyListAdapter(listPagers,1.5f)
        //толщина разделителя между пунктами меню
        listView.dividerHeight = 1
        //цвет разделителя между пунктами меню
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            listView.divider = ColorDrawable(resources.getColor(R.color.transparent,null))
        } else {
            @Suppress("DEPRECATION")
            listView.divider = ColorDrawable(resources.getColor(R.color.transparent))
        }
    }

//    fun changePhase (phase: String, context: Context?) {
//        Log.v (DebugTag.TAG, "FragmentListView changePhase $phase")
//        mPhase = phase
//        val mListPagers : ArrayList<ListPager> = ListPagerLab.get(context!!).getPhaseList(mPhase)
//        val mListAdapter = MyListAdapter(mListPagers,1.5f)
//        listAdapter = mListAdapter
//    }

    override fun onListItemClick(l: ListView?, v: View?, position: Int, id: Long) {
        super.onListItemClick(l, v, position, id)
        if (mListener != null) {
            mListener!!.onListViewInteraction(mPhase,position)
        }
        //toast("Ваш выбор $mPhase, $position")
    }

    override fun onAttach(context: Context?) {
        Log.v (DebugTag.TAG, "FragmentListView onAttach")
        super.onAttach(context)
        if (context is OnListViewInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnListViewInteractionListener")
        }
    }

    override fun onDetach() {
        Log.v (DebugTag.TAG, "FragmentListView onDetach")
        super.onDetach()
        mListener = null
    }

    override fun onSaveInstanceState(state: Bundle) {
        state.putString("phase", mPhase)
        Log.v(DebugTag.TAG, "Save InstanceState = $mPhase")
        super.onSaveInstanceState(state)
    }

    private fun loadSavedState() {
        mPhase = arguments!!.getString("phase")
        Log.v (DebugTag.TAG, "Load SavedState = $mPhase")
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html) for more information.
     */
    interface OnListViewInteractionListener {
        fun onListViewInteraction(phase: String, id : Int) {
            Log.v(DebugTag.TAG, "FragmentListView onListViewInteraction")
        }
    }

    companion object {
        fun newInstance(param1: String): FragmentListView {
            Log.v(DebugTag.TAG, "FragmentListView newInstance with $param1")
            val fragment = FragmentListView()
            val args = Bundle()
            args.putString("phase", param1)
            fragment.arguments = args
            return fragment
        }
    }
}// Required empty public constructor
