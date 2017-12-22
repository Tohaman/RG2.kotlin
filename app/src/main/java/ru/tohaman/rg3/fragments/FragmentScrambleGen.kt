package ru.tohaman.rg3.fragments


import android.content.SharedPreferences
import android.os.Bundle
import android.os.SystemClock.sleep
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import kotlinx.android.synthetic.main.fragment_scramble_gen.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.backgroundColorResource
import org.jetbrains.anko.coroutines.experimental.bg
import org.jetbrains.anko.sdk15.coroutines.onCheckedChange
import org.jetbrains.anko.sdk15.coroutines.onClick
import ru.tohaman.rg3.DebugTag.TAG
import ru.tohaman.rg3.R
import ru.tohaman.rg3.adapters.MyGridAdapter
import ru.tohaman.rg3.data.CubeAzbuka
import ru.tohaman.rg3.util.saveBoolean2SP
import ru.tohaman.rg3.util.saveInt2SP

/**
 * Created by anton on 27.11.17. Фрагмент отображающий генератор скрамблов
 *
 */



class FragmentScrambleGen : Fragment() {

    private val SCRAMBLE = "scramble"
    private val SCRAMBLE_LEN = "scrambleLength"
    private val CHK_BUF_EDGES = "checkEdgesBuffer"
    private val CHK_BUF_CORNERS = "checkCornersBuffer"

    private var gridList = ArrayList<CubeAzbuka>()
    private val cubeColor = IntArray(6)
    private var currentCube = IntArray(54)

    private lateinit var gridAdapter : MyGridAdapter
    private lateinit var sp : SharedPreferences
    private lateinit var progressBar : ProgressBar
    private lateinit var progressText : TextView
    private lateinit var generateScramble : Button
    private lateinit var checkBoxEdges : CheckBox
    private lateinit var checkBoxCorners : CheckBox
    private lateinit var textScrambleLen: TextView
    private lateinit var buttonPlus: Button
    private lateinit var buttonMinus: Button

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.v (TAG, "FragmentScrambleGen onCreateView - start")

        sp = PreferenceManager.getDefaultSharedPreferences(context)
        val scramble = sp.getString(SCRAMBLE, "U2 F2 L\' D2 R U F\' L2 B2 R L2 B2 U R")
        var chkEdgesBuffer = sp.getBoolean(CHK_BUF_EDGES, true)
        var chkCornersBuffer = sp.getBoolean(CHK_BUF_CORNERS, false)
        var scrambleLength = sp.getInt(SCRAMBLE_LEN, 12)

        val view = inflater?.inflate(R.layout.fragment_scramble_gen, container, false)

        cubeColor[0] = R.color.cube_blue
        cubeColor[1] = R.color.cube_orange
        cubeColor[2] = R.color.cube_white
        cubeColor[3] = R.color.cube_red
        cubeColor[4] = R.color.cube_yellow
        cubeColor[5] = R.color.cube_green

        Log.v (TAG, "FragmentScrambleGen onCreateView - hide ProgressBar & ProgressText")
        progressBar = view!!.findViewById<ProgressBar>(R.id.progressBar)
        progressBar.visibility = View.INVISIBLE
        progressText = view.findViewById(R.id.progressText)
        progressText.backgroundColorResource = R.color.gray
        progressText.textSize = 12f
        progressText.visibility = View.INVISIBLE

        // Кнопка вызова редактирования азбуки
        val azbukaButton = view.findViewById<Button>(R.id.button_azbuka)
        azbukaButton.onClick {
//            startActivity<AzbukaActivity>()
        }

        // Главная кнопка - Генерация скрамбла
        generateScramble = view.findViewById(R.id.button_generate)
        generateScramble.onClick {
            scrambleGenerate(chkEdgesBuffer, chkCornersBuffer, scrambleLength)
        }

        // Чекбокс переплавки граней
        checkBoxEdges = view.findViewById(R.id.checkBox_edges)
        checkBoxEdges.isChecked = chkEdgesBuffer
        checkBoxEdges.onCheckedChange { buttonView, isChecked ->
            chkEdgesBuffer = isChecked
            saveBoolean2SP(chkEdgesBuffer,CHK_BUF_EDGES,buttonView!!.context)
        }

        // Чекбокс переплавки углов
        checkBoxCorners = view.findViewById(R.id.checkBox_corners)
        checkBoxCorners.isChecked = chkCornersBuffer
        checkBoxCorners.onCheckedChange { buttonView, isChecked ->
            chkCornersBuffer = isChecked
            saveBoolean2SP(chkCornersBuffer,CHK_BUF_CORNERS,buttonView!!.context)
        }

        // Длина скрамбла (текстовое поле)
        textScrambleLen =  view.findViewById(R.id.scrambleLength)
        textScrambleLen.text = scrambleLength.toString()

        // Кнопка +
        buttonPlus = view.findViewById(R.id.button_plus)
        buttonPlus.onClick { v ->
            scrambleLength++
            if (scrambleLength > 30) { scrambleLength = 30 }
            textScrambleLen.text = scrambleLength.toString()
            saveInt2SP(scrambleLength,SCRAMBLE_LEN, v!!.context)
        }

        // Кнопка -
        buttonMinus = view.findViewById(R.id.button_minus)
        buttonMinus.onClick { v ->
            scrambleLength--
            if (scrambleLength < 1 ) { scrambleLength = 1 }
            textScrambleLen.text = scrambleLength.toString()
            saveInt2SP(scrambleLength,SCRAMBLE_LEN, v!!.context)
        }

        currentCube = initialize()                  //берем собранный кубик
        gridList = initGridList(currentCube)        //подготавливаем текущий кубик для вывода в GridView

        //находим GridView и выводим в него текущий кубик
        val gridView = view.findViewById(R.id.scram_gridView) as GridView
        gridAdapter = MyGridAdapter(view.context, gridList)
        gridView.adapter = gridAdapter

        return view
    }

    private fun initialize(): IntArray {
        Log.v (TAG, "FragmentScrambleGen initialize = ResetCube")
        val cube = IntArray(54)
        for (i in cube.indices) {
            cube[i] = i / 9
        }
        return cube
    }

    private fun showCube(cube: IntArray) {
        Log.v (TAG, "FragmentScrambleGen showCube")
        initGridList(cube)
        gridAdapter.notifyDataSetChanged()
    }

    //TODO переименовать в prepareCubeToShowInGridView
    private fun initGridList(cube: IntArray) : ArrayList<CubeAzbuka> {
        Log.v (TAG, "FragmentScrambleGen InitGridList")
        // очищаем grList = ListOf<(R.color.transparent, "")> - 108штук
        val grList = clearArray4GridList()
        // Задаем для элементов куба букву равную пробелу, и цвет соответствующий элемнтам куба (массива)
        // если остается = "" и цвет прозрачный то это элемент фона (и будет не виден)
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
        Log.v (TAG, "FragmentScrambleGen clearArray4GridList")
        // 108 элементов GridList делаем пустыми и прозрачными
        val cubeAzbuka = CubeAzbuka(R.color.transparent, "")
        val grList = arrayListOf<CubeAzbuka>()
        for (i in 0..107) {
            grList.add(cubeAzbuka)
        }
        return grList
    }

    private fun scrambleGenerate(chkEdgesBuffer: Boolean, chkCornersBuffer: Boolean, scrambleLength: Int) {
        Log.v(TAG, "FragmentScrambleGen scrambleGenerate")
        // берем собранный куб и выводим его на экран
        currentCube = initialize()
        showCube(currentCube)
//            solvetext.setText("")
        // делаем кнопку "Генерерировать" не активной, а прогресбар активным
        button_generate.isEnabled = false
        progressBar.visibility = View.VISIBLE
        progressText.visibility = View.VISIBLE
        async(UI) {
            val data = bg {
                // Выполняем в background, ключевое слово bg
                generateScrambleWithParam(chkEdgesBuffer, chkCornersBuffer, scrambleLength)
            }
            // А этот код будет уже в UI потоке
            showScrambleGenResult(data.await())
        }
    }

    private fun generateScrambleWithParam(chEdge: Boolean, chCorner: Boolean, lenScramble: Int): String {
        Log.v(TAG, "FragmentScrambleGen generateScramble with param in background")
        sleep(3000)
        return "F2 L\' D2 R U F\' L2 B2 R L2 B2 U R"
    }

    private fun showScrambleGenResult(sсramble: String) {
        Log.v(TAG, "FragmentScrambleGen showScrambleGenResult")
        button_generate.isEnabled = true
        progressBar.visibility = View.INVISIBLE
        progressText.visibility = View.INVISIBLE

    }

}

