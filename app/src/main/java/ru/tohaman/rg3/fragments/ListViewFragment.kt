package ru.tohaman.rg3.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.ListFragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import org.jetbrains.anko.support.v4.toast
import ru.tohaman.rg3.DebugTag

import ru.tohaman.rg3.R
import ru.tohaman.rg3.R.color.transparent
import ru.tohaman.rg3.adapters.MyListAdapter
import ru.tohaman.rg3.listpager.ListPager
import ru.tohaman.rg3.listpager.ListPagerLab
import java.util.ArrayList
import android.graphics.drawable.ColorDrawable
import android.os.Build


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [ListViewFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [ListViewFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ListViewFragment : ListFragment() {

    // TODO: Rename and change types of parameters
    private var mParam1: String? = null
    private var mParam2: String? = null
    private var mPhase: String = "BEGIN"
    private lateinit var mDrawerListView: ListView

    private var mListener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.v (DebugTag.TAG, "ListViewFragment onCreate")
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = arguments.getString(ARG_PARAM1)
            mParam2 = arguments.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater, container, savedInstanceState)

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        Log.v (DebugTag.TAG, "ListViewFragment onActivityCreated $mPhase")
        super.onActivityCreated(savedInstanceState)
        val mListPagers : ArrayList<ListPager> = ListPagerLab.get(context!!).getPhaseList(mPhase)
        val mListAdapter = MyListAdapter(mListPagers,1.5f)
        listAdapter = mListAdapter
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

    fun changePhase (phase: String, context: Context?) {
        Log.v (DebugTag.TAG, "ListViewFragment changePhase $phase")
        mPhase = phase
        val mListPagers : ArrayList<ListPager> = ListPagerLab.get(context!!).getPhaseList(mPhase)
        val mListAdapter = MyListAdapter(mListPagers,1.5f)
        listAdapter = mListAdapter
//        mDrawerListView.adapter = mListAdapter
//        mDrawerListView.deferNotifyDataSetChanged()
    }

    override fun onListItemClick(l: ListView?, v: View?, position: Int, id: Long) {
        super.onListItemClick(l, v, position, id)
        toast("Ваш выбор $mPhase, $position")
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        Log.v (DebugTag.TAG, "ListViewFragment onButtonPressed")
        if (mListener != null) {
            mListener!!.onFragmentInteraction(uri)
        }
    }

    override fun onAttach(context: Context?) {
        Log.v (DebugTag.TAG, "ListViewFragment onAttach")
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        Log.v (DebugTag.TAG, "ListViewFragment onDetach")
        super.onDetach()
        mListener = null
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
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri) {
            Log.v(DebugTag.TAG, "ListViewFragment onFragmentInteraction")
        }
    }

    companion object {
        // TODO: Rename parameter arguments, choose names that match
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private val ARG_PARAM1 = "param1"
        private val ARG_PARAM2 = "param2"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ListViewFragment.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(param1: String, param2: String = ""): ListViewFragment {
            Log.v(DebugTag.TAG, "ListViewFragment newInstance")
            val fragment = ListViewFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            Log.v(DebugTag.TAG, "ListViewFragment newInstance end")
            return fragment
        }
    }
}// Required empty public constructor
