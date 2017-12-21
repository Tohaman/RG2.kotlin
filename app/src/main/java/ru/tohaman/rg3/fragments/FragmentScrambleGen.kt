package ru.tohaman.rg3.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import android.widget.ProgressBar
import android.widget.TextView
import org.jetbrains.anko.backgroundColorResource
import ru.tohaman.rg3.DebugTag.TAG
import ru.tohaman.rg3.R
import ru.tohaman.rg3.adapters.MyGridAdapter
import ru.tohaman.rg3.data.CubeAzbuka

/**
 * Created by anton on 27.11.17. Фрагмент отображающий генератор скрамблов
 *
 */



class FragmentScrambleGen : Fragment() {
    private var gridList = ArrayList<CubeAzbuka>()
    private val cubeColor = IntArray(6)
    private var currentCube = IntArray(54)

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater?.inflate(R.layout.fragment_scramble_gen, container, false)
        Log.v (TAG, "FragmentScrambleGen onCreateView - start")
        cubeColor[0] = R.color.cube_blue
        cubeColor[1] = R.color.cube_orange
        cubeColor[2] = R.color.cube_white
        cubeColor[3] = R.color.cube_red
        cubeColor[4] = R.color.cube_yellow
        cubeColor[5] = R.color.cube_green

        Log.v (TAG, "FragmentScrambleGen onCreateView - hide ProgressBar & ProgressText")
        val mProgressBar = view!!.findViewById(R.id.progressBar) as ProgressBar
        mProgressBar.visibility = View.INVISIBLE
        val progressText = view.findViewById(R.id.progressText) as TextView
        progressText.text = getString(R.string.scram_waiting)
        progressText.backgroundColorResource = R.color.white
        progressText.textSize = 12f
        progressText.visibility = View.INVISIBLE

        Log.v (TAG, "FragmentScrambleGen onCreateView - Initialize Cube")
        currentCube = Initialize()                  //берем собранный кубик
        gridList = initGridList(currentCube)        //подготавливаем текущий кубик для вывода в GridView

        //находим GridView и выводим в него текущий кубик
        val gridView = view.findViewById(R.id.scram_gridView) as GridView
        val gridAdapter = MyGridAdapter(view.context, gridList)
        gridView.adapter = gridAdapter

        return view
    }

    private fun Initialize(): IntArray {
        Log.v (TAG, "FragmentScrambleGen Initialize = ResetCube")
        val cube = IntArray(54)
        for (i in cube.indices) {
            cube[i] = i / 9
        }
        return cube
    }

    private fun initGridList(cube: IntArray) : ArrayList<CubeAzbuka> {
        Log.v (TAG, "FragmentScrambleGen InitGridList")
        // очищаем
        val grList = clearArray4GridList()


        // если буква элемента = пробелу, то это элемент куба, если остается = "" то фона
        // задаем элементам куба GridList'а цвет, соответствующий элемнтам куба (массива)
        for (i in 0..8) {
            grList[(i / 3) * 12 + 3 + i % 3] = CubeAzbuka(cubeColor[cube[i]], " ")
            grList[(i / 3 + 3) * 12 + i % 3] = CubeAzbuka(cubeColor[cube[i + 9]], " ")
            grList[(i / 3 + 3) * 12 + 3 + i % 3] = CubeAzbuka(cubeColor[cube[i + 18]], " ")
            grList[(i / 3 + 3) * 12 + 6 + i % 3] = CubeAzbuka(cubeColor[cube[i + 27]], " ")
            grList[(i / 3 + 3) * 12 + 9 + i % 3] = CubeAzbuka(cubeColor[cube[i + 36]], " ")
            grList[(i / 3 + 6) * 12 + 3 + i % 3] = CubeAzbuka(cubeColor[cube[i + 45]], " ")
        }
        return grList
    }

    private fun clearArray4GridList(): ArrayList<CubeAzbuka> {
        // 108 элементов GridList делаем пустыми и прозрачными
        val cubeAzbuka = CubeAzbuka(R.color.transparent, "")
        val grList = arrayListOf<CubeAzbuka>()
        for (i in 0..107) {
            grList.add(cubeAzbuka)
        }
        return grList
    }

}

