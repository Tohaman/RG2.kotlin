package ru.tohaman.rg2.fragments


import android.content.Context
import android.os.Bundle
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
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk15.coroutines.onCheckedChange
import org.jetbrains.anko.sdk15.coroutines.onClick
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.ctx
import ru.tohaman.rg2.DebugTag
import ru.tohaman.rg2.DebugTag.TAG
import ru.tohaman.rg2.R
import ru.tohaman.rg2.adapters.MyGridAdapter
import ru.tohaman.rg2.data.ListPagerLab
import ru.tohaman.rg2.util.*
import java.util.*

/**
 * Created by anton on 27.11.17. Фрагмент отображающий генератор скрамблов
 *
 */

class FragmentScrambleGen : Fragment() {

    private val SCRAMBLE = "scramble"
    private val SCRAMBLE_LEN = "scrambleLength"
    private val CHK_BUF_EDGES = "checkEdgesBuffer"
    private val CHK_BUF_CORNERS = "checkCornersBuffer"
    private val CHK_SHOW_SOLVE = "checkShowSolve"

    private var mListener: OnSrambleGenInteractionListener? = null

    private var currentCube = IntArray(54)

    private lateinit var gridAdapter : MyGridAdapter

    private lateinit var progressBar : ProgressBar
    private lateinit var progressText : TextView
    private lateinit var generateScramble : Button
    private lateinit var checkBoxEdges : CheckBox
    private lateinit var checkBoxCorners : CheckBox
    private lateinit var checkBoxShowSolve : CheckBox
    private lateinit var textScrambleLen: TextView
    private lateinit var textScramble: TextView
    private lateinit var textSolve: TextView
    private lateinit var buttonPlus: Button
    private lateinit var buttonMinus: Button

    private var listEdgesOnPlace: SortedMap<Int,Int> = sortedMapOf(0 to 0)
    private var listCornersOnPlace: SortedMap<Int,Int> = sortedMapOf(0 to 0)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.v (TAG, "FragmentScrambleGen onCreateView - start")

        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        val uri = activity?.intent?.data
        var scramble = sp.getString(SCRAMBLE, "U2 F2 L\' D2 R U F\' L2 B2 R L2 B2 U R")
        // Если вызван с параметром, то скрамбл взять из параметра, а не из базы
        if (uri != null) {
            scramble = uri.getQueryParameter("scram")
            scramble = scramble.replace("_", " ")
        }
        var chkEdgesBuffer = sp.getBoolean(CHK_BUF_EDGES, true)
        var chkCornersBuffer = sp.getBoolean(CHK_BUF_CORNERS, false)
        var scrambleLength = sp.getInt(SCRAMBLE_LEN, 14)
        var chkShowSolve = sp.getBoolean(CHK_SHOW_SOLVE, true)

        val view = inflater.inflate(R.layout.fragment_scramble_gen, container, false)

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
            Log.v (TAG, "AzbukaButton Click")
            if (mListener != null) {
                mListener!!.onScrambleGenInteraction("AZBUKA")
            }
        }

        // Главная кнопка - Генерация скрамбла
        generateScramble = view.findViewById(R.id.button_generate)
        generateScramble.onClick {
            Log.v (TAG, "GenerateScrambleButton Click")
            scrambleGenerate(chkEdgesBuffer, chkCornersBuffer, scrambleLength)
        }

        // Чекбокс переплавки граней
        checkBoxEdges = view.findViewById(R.id.checkBox_edges)
        checkBoxEdges.isChecked = chkEdgesBuffer
        checkBoxEdges.onCheckedChange { buttonView, isChecked ->
            Log.v (TAG, "CheckBoxEdges changed")
            chkEdgesBuffer = isChecked
            saveBoolean2SP(chkEdgesBuffer,CHK_BUF_EDGES,buttonView!!.context)
        }

        // Чекбокс переплавки углов
        checkBoxCorners = view.findViewById(R.id.checkBox_corners)
        checkBoxCorners.isChecked = chkCornersBuffer
        checkBoxCorners.onCheckedChange { buttonView, isChecked ->
            Log.v (TAG, "CheckBoxCorners changed")
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

        //Текст самого скрамбла
        textScramble = view.findViewById(R.id.scramble)
        textScramble.text = scramble
        textScramble.onClick { v ->
            Log.v (TAG, "CustomScrambleSe select")
            alert {
                customView {
                    verticalLayout {
                        val setScramble = editText (textScramble.text){
                            hint = "введите свой скрамбл"
                        }
                        positiveButton("OK") {
                            val scramble_st = setScramble.text.toString()
                            textScramble.text = scramble_st
                            saveString2SP(scramble_st,SCRAMBLE, v!!.context)
                            val cube = runScramble(resetCube(), scramble_st)
                            showCube(cube)
                            textSolve.text = showSolve(cube)
                        }
                        negativeButton("Отмена") {}
                    }
                }
            }.show()
        }

        // Чекбокс отображать или нет решение скрамбла
        checkBoxShowSolve = view.findViewById(R.id.checkBox_solve)
        checkBoxShowSolve.isChecked = chkShowSolve
        checkBoxShowSolve.onCheckedChange { buttonView, isChecked ->
            Log.v (TAG, "CheckBoxShowSolve changed")
            chkShowSolve = isChecked
            val cube = runScramble(resetCube(), textScramble.text as String)
            textSolve.text = showSolve(cube)
            saveBoolean2SP(chkShowSolve,CHK_SHOW_SOLVE, buttonView!!.context)
        }

        currentCube = runScramble(resetCube(), scramble)
        //Текст с решением скрамбла
        textSolve = view.findViewById(R.id.solve_text)
        textSolve.text = showSolve(currentCube)

        val gridList = prepareCubeToShowInGridView(currentCube)        //подготавливаем текущий кубик для вывода в GridView

        //находим GridView и выводим в него текущий кубик
        val gridView = view.findViewById(R.id.scram_gridView) as GridView
        gridAdapter = MyGridAdapter(view.context, gridList)
        gridView.adapter = gridAdapter

        return view
    }

    private fun showSolve(cube: IntArray): String {
        val st = getSolve(cube).first
        return if (checkBoxShowSolve.isChecked) {
            st
        } else {
            st.split(" ".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray().size.toString()
        }
    }

    private fun showCube(cube: IntArray) {
        Log.v (TAG, "FragmentScrambleGen showCube")
        gridAdapter.gridList = prepareCubeToShowInGridView(cube)
        gridAdapter.notifyDataSetChanged()
    }

    //Возвращаем решение, была ли переплавка буф.ребер, была ли переплавка буф.углов
    private fun getSolve(mainCube: IntArray):  Triple<String, Boolean, Boolean>  {
        var solve = "("
        var cube = mainCube.clone()
        var isEdgeMelted = false
        var isCornerMelted = false

        //решаем ребра
        do {
            //сначала ребра: смотрим что в буфере ребер
            val sumColor = getColorOfElement(cube, 23, 30)
            //если там буферный элемент бело-красный или красно-белый, то ставим признак переплавки
            if ((sumColor == 43) or (sumColor == 34)) { isEdgeMelted = true }
            // ставим на место ребро из буфера
            val sc = edgeBufferSolve(cube, mainEdge[sumColor]!!, solve)
            // сохраняем результаты выполнения одной "буквы"
            solve = sc.solve
            cube = sc.cube
            // выполняем пока все ребра не будут на своих местах
        } while (!isAllEdgesOnItsPlace(cube))

        solve = solve.trim { it <= ' ' }
        solve += ") "
        // Проверяем нужен ли экватор, и выполняем его если надо
        val j = solve.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray().size
        if (j % 2 != 0) {
            solve += "Эк "
            cube = ekvator(cube)
        }

        //решаем углы
        solve += "("
        do {
            //сначала ребра: смотрим что в буфере углов
            val sumColor = getColorOfElement(cube,18,11)
            //если там буферный элемент, то ставим признак переплавки
            if ((sumColor == 18) or (sumColor == 11) or (sumColor == 6)) { isCornerMelted = true }
            // ставим на место угол из буфера
            val sc = cornerBufferSolve(cube,  mainCorner[sumColor]!!, solve)
            // сохраняем результаты выполнения одной "буквы"
            solve = sc.solve
            cube = sc.cube
            // выполняем пока все углы не будут на своих местах
        } while (!isAllCornersOnItsPlace(cube))

        solve = solve.trim { it <= ' ' }
        solve += ")"
        return Triple(solve,isEdgeMelted,isCornerMelted)
    }


    private suspend fun scrambleGenerate(chkEdgesBuffer: Boolean, chkCornersBuffer: Boolean, scrambleLength: Int) {
        Log.v(TAG, "FragmentScrambleGen scrambleGenerate")
        // делаем кнопку "Генерерировать" не активной, прогресбар активным и убираем решение скрамбла
        button_generate.isEnabled = false
        progressBar.visibility = View.VISIBLE
        progressText.visibility = View.VISIBLE
        textSolve.text = ""
        //запускаем в бэкграунде поиск скрамбла удовлетворяющего условиям
        val scramble = async(UI) {
            generateScrambleWithParam(chkEdgesBuffer, chkCornersBuffer, scrambleLength)
        }
        // ждем результат генерации и выводим его
        showScrambleGenResult(scramble.await())
    }

    private fun showScrambleGenResult(genRes: String) {
        Log.v(TAG, "FragmentScrambleGen showScrambleGenResult $genRes")
        button_generate.isEnabled = true
        progressBar.visibility = View.INVISIBLE
        progressText.visibility = View.INVISIBLE
        textScramble.text = genRes
        val cube = runScramble(resetCube(), genRes)
        showCube(cube)
        textSolve.text = showSolve(cube)
    }

    private fun generateScrambleWithParam(checkEdge: Boolean, checkCorner: Boolean, lenScramble: Int): String {
        Log.v(TAG, "Ищем скрамбл подходящий по параметрам переплавок буфера и длинне")
        var scramble: String
        do {
            var result = true
            //сгенерируем скрамбл длинны указанной в поле ScrambleLength
            scramble = generateScramble(lenScramble)
            //разбираем кубик по скрамблу
            val genScrambleCube = runScramble(resetCube(), scramble)
            // получаем решение кубика (solve,isEdgeMelted,isCornerMelted)
            val triple = getSolve(genScrambleCube)
            val isEdgeMelted = triple.second
            val isCornerMelted = triple.third

            Log.v(TAG, "Проверка Scramble $scramble, Переплавка буфера ребер - ${triple.second} , Переплавка буфера углов - ${triple.third}")
            if (isEdgeMelted && checkEdge) { result = false }
            if (isCornerMelted && checkCorner) { result = false }
        } while (!result)
        Log.v(TAG, "Таки скрамбл $scramble подошел под наши условия")

        return scramble
    }

    // Установка на свое место элемента цвета colorOfElement находящегося в буфере ребер
    // Возвращает SolveCube = куб после выполнения установки и решение solve + текущий ход
    private fun edgeBufferSolve(cube: IntArray, colorOfElement: Int, solve: String): SolveCube {
        var tmpCube = cube
        var colOfElem = colorOfElement
        var solv = solve
        if (!((colOfElem == 23) or (colOfElem == 30))) {           //проверяем, не буфер ли?, если нет, то добоавляем букву к решению
            solv += findLetter(colOfElem) + " "        //если буфер, то будем его переплавлять и букву уже
        }                                               //подставим в рекурсии
        when (colOfElem) {
            1 -> tmpCube = blinde1(tmpCube)
            3 -> tmpCube = blinde3(tmpCube)
            5 -> tmpCube = blinde5(tmpCube)
            7 -> tmpCube = blinde7(tmpCube)
            10 -> tmpCube = blinde10(tmpCube)
            12 -> tmpCube = blinde12(tmpCube)
            14 -> tmpCube = blinde14(tmpCube)
            16 -> tmpCube = blinde16(tmpCube)
            19 -> tmpCube = blinde19(tmpCube)
            21 -> tmpCube = blinde21(tmpCube)
            23 ->                     // для бело-красного ребра
                if (!isAllEdgesOnItsPlace(tmpCube)) {
                    val sc = meltingEdge(tmpCube, solv)
                    solv = sc.solve
                    tmpCube = sc.cube
                } else {
                    //Если все ребра на месте, то преобразуем буквы в слова
                }
            25 -> tmpCube = blinde25(tmpCube)
            28 -> tmpCube = blinde28(tmpCube)
            30 ->                       //для красно-белого ребра
                if (!isAllEdgesOnItsPlace(tmpCube)) {
                    colOfElem = 0
                    // цикл поиска свободной корзины
                    var j = 0
                    while (colOfElem == 0) {
                        var i = 0
                        do {
                            if (edgePriority[j] == listEdgesOnPlace[i]) {
                                colOfElem = edgePriority[j]!!
                            } //ищем ребра на своем месте по приоритету edgePriority
                            i++
                        } while (listEdgesOnPlace[i] != null)
                        j++
                    }
                    //переплавляем буфер (рекурсия)
                    val sc = edgeBufferSolve(tmpCube, colOfElem, solv)
                    solv = sc.solve
                    tmpCube = sc.cube
                }
            32 -> tmpCube = blinde32(tmpCube)
            34 -> tmpCube = blinde34(tmpCube)
            37 -> tmpCube = blinde37(tmpCube)
            39 -> tmpCube = blinde39(tmpCube)
            41 -> tmpCube = blinde41(tmpCube)
            43 -> tmpCube = blinde43(tmpCube)
            46 -> tmpCube = blinde46(tmpCube)
            48 -> tmpCube = blinde48(tmpCube)
            50 -> tmpCube = blinde50(tmpCube)
            52 -> tmpCube = blinde52(tmpCube)
        }
        return SolveCube(tmpCube, solv)
    }

    private fun meltingEdge(tmpCube: IntArray, solv: String): SolveCube {
        var colorOfElement = 0
        // цикл поиска свободной корзины
        var j = 0
        while (colorOfElement == 0) {
            var i = 0
            do {
                if (edgePriority[j] == listEdgesOnPlace[i]) {
                    colorOfElement = edgePriority[j]!!
                } //ищем ребра на своем месте по приоритету edgePriority
                i++
            } while (listEdgesOnPlace[i] != null)
            j++
        }
        //переплавляем буфер (рекурсия)
        return edgeBufferSolve(tmpCube, colorOfElement, solv)
    }

    private fun isAllEdgesOnItsPlace(cube: IntArray): Boolean {    //проверяем все ли грани на своих местах
        //предположим что все на местах
        var result = true
        //Обнуляем список ребер стоящих на местах
        listEdgesOnPlace.clear()
        var j = 0
        for (i in 0..52) {
            val secColor = dopEdge[i]
            if (secColor != null) {
                val fcolor = getColorOfElement(cube,i,secColor)
                if (mainEdge[fcolor] != i) {
                    listEdgesOnPlace.put(j, i)
                    j++
                    result = false
                }
            }
        }
        return result
    }

    // Установка на свое место элемента цвета colorOfElement находящегося в буфере углов
    // Возвращает SolveCube = куб после выполнения установки и решение solve + текущий ход
    private fun cornerBufferSolve(cube: IntArray, colorOfElement: Int, solve: String): SolveCube {
        var tmpCube = cube
        var solv = solve
        if (!(colorOfElement == 18 || colorOfElement == 11 || colorOfElement == 6)) {           //если с не равно 18,11 или 6, то буфер не на месте и добавляем букву к решению.
            solv = solv + findLetter(colorOfElement) + " "
        }
        when (colorOfElement) {
            0 -> tmpCube = blinde0(tmpCube)
            2 -> tmpCube = blinde2(tmpCube)
            6 -> if (!isAllCornersOnItsPlace(tmpCube)) {
                val sc = meltingCorner(tmpCube, solv)
                solv = sc.solve
                tmpCube = sc.cube
            }
            8 -> tmpCube = blinde8(tmpCube)
            9 -> tmpCube = blinde9(tmpCube)
            11 -> if (!isAllCornersOnItsPlace(tmpCube)) {
                val sc = meltingCorner(tmpCube, solv)
                solv = sc.solve
                tmpCube = sc.cube
            }
            15 -> tmpCube = blinde15(tmpCube)
            17 -> tmpCube = blinde17(tmpCube)
            18 -> if (!isAllCornersOnItsPlace(tmpCube)) {
                val sc = meltingCorner(tmpCube, solv)
                solv = sc.solve
                tmpCube = sc.cube
            }
            20 -> tmpCube = blinde20(tmpCube)
            24 -> tmpCube = blinde24(tmpCube)
            26 -> tmpCube = blinde26(tmpCube)
            27 -> tmpCube = blinde27(tmpCube)
            29 -> tmpCube = blinde29(tmpCube)
            33 -> tmpCube = blinde33(tmpCube)
            35 -> tmpCube = blinde35(tmpCube)
            36 -> tmpCube = blinde36(tmpCube)
            38 -> tmpCube = blinde38(tmpCube)
            42 -> tmpCube = blinde42(tmpCube)
            44 -> tmpCube = blinde44(tmpCube)
            45 -> tmpCube = blinde45(tmpCube)
            47 -> tmpCube = blinde47(tmpCube)
            51 -> tmpCube = blinde51(tmpCube)
            53 -> tmpCube = blinde53(tmpCube)
        }
        return SolveCube(tmpCube, solv)
    }

    private fun meltingCorner(tmpCube: IntArray, solv: String): SolveCube {
        var colorOfElement = 0
        // цикл поиска свободной корзины
        var j = 0
        while (colorOfElement == 0) {
            var i = 0
            do {
                if (cornerPriority[j] == listCornersOnPlace[i]) {
                    colorOfElement = cornerPriority[j]!!
                } //ищем ребра на своем месте по приоритету cornerPriority
                i++
            } while (listCornersOnPlace[i] != null)
            j++
        }
        //переплавляем буфер (рекурсия)
        return cornerBufferSolve(tmpCube, colorOfElement, solv)
    }

    private fun isAllCornersOnItsPlace(cube: IntArray): Boolean {    //проверяем все ли углы на своих местах
        //предположим что все на местах
        var result = true
        //Обнуляем список углов стоящих на своих местах
        listCornersOnPlace.clear()
        var j = 0
        //Будем проверять все элементы кубика
        for (i in 0..52) {
            //Проверяем данный элемент угол или ребро
            val secColor = dopCorner[i]
            if (secColor != null) {
                val fcolor = getColorOfElement(cube,i,secColor)
                if (mainCorner[fcolor] != i) {
                    listCornersOnPlace.put(j, i)
                    j++
                    result = false
                }
            }
        }
        return result
    }

    //поиск буквы в азбуке
    private fun findLetter(c: Int): String {     //Доработать функцию поиска буквы из азбуки, пока просто цифра
        val listPagerLab = ListPagerLab.get(ctx)
        val azbuka = listPagerLab.getCurrentAzbuka()
        return azbuka[c]
    }

    override fun onAttach(context: Context?) {
        Log.v (DebugTag.TAG, "FragmentScrambleGen onAttach")
        super.onAttach(context)
        if (context is FragmentScrambleGen.OnSrambleGenInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnScrambleGenInteractionListener")
        }
    }

    override fun onDetach() {
        Log.v (DebugTag.TAG, "FragmentScrambleGen onDetach")
        super.onDetach()
        mListener = null
    }

    interface OnSrambleGenInteractionListener {
        fun onScrambleGenInteraction(button: String) {
            Log.v(DebugTag.TAG, "FragmentScrambleGen onListViewInteraction")
        }
    }

    companion object {
        fun newInstance(): FragmentScrambleGen {
            Log.v(DebugTag.TAG, "FragmentScrambleGen newInstance")
            return FragmentScrambleGen()
        }
    }

    inner class SolveCube (var cube: IntArray, var solve: String)   // куб, решение

}

