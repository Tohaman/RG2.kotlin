package ru.tohaman.rg3.fragments


import android.content.Context
import android.content.SharedPreferences
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
import org.jetbrains.anko.coroutines.experimental.bg
import org.jetbrains.anko.sdk15.coroutines.onCheckedChange
import org.jetbrains.anko.sdk15.coroutines.onClick
import org.jetbrains.anko.support.v4.alert
import ru.tohaman.rg3.DebugTag
import ru.tohaman.rg3.DebugTag.TAG
import ru.tohaman.rg3.R
import ru.tohaman.rg3.adapters.MyGridAdapter
import ru.tohaman.rg3.data.CubeAzbuka
import ru.tohaman.rg3.data.ListPagerLab
import ru.tohaman.rg3.util.*
import java.util.*
import kotlin.collections.HashMap

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
    private lateinit var checkBoxShowSolve : CheckBox
    private lateinit var textScrambleLen: TextView
    private lateinit var textScramble: TextView
    private lateinit var textSolve: TextView
    private lateinit var buttonPlus: Button
    private lateinit var buttonMinus: Button

    private var mainEdge: HashMap<Int,Int> = hashMapOf(0 to 0)
    private var dopEdge: HashMap<Int,Int> = hashMapOf(0 to 0)
    private var mainCorner: HashMap<Int,Int> = hashMapOf(0 to 0)
    private var dopCorner: HashMap<Int,Int> = hashMapOf(0 to 0)
    private var listEdgesOnPlace: SortedMap<Int,Int> = sortedMapOf(0 to 0)
    private var listCornersOnPlace: SortedMap<Int,Int> = sortedMapOf(0 to 0)
    private var edgePriority: HashMap<Int,Int> = hashMapOf(0 to 0)
    private var cornerPriority: HashMap<Int,Int> = hashMapOf(0 to 0)

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.v (TAG, "FragmentScrambleGen onCreateView - start")

        sp = PreferenceManager.getDefaultSharedPreferences(context)
        val scramble = sp.getString(SCRAMBLE, "U2 F2 L\' D2 R U F\' L2 B2 R L2 B2 U R")
        var chkEdgesBuffer = sp.getBoolean(CHK_BUF_EDGES, true)
        var chkCornersBuffer = sp.getBoolean(CHK_BUF_CORNERS, false)
        var scrambleLength = sp.getInt(SCRAMBLE_LEN, 14)
        var chkShowSolve = sp.getBoolean(CHK_SHOW_SOLVE, true)
        initArrays()

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

        gridList = prepareCubeToShowInGridView(currentCube)        //подготавливаем текущий кубик для вывода в GridView

        //находим GridView и выводим в него текущий кубик
        val gridView = view.findViewById(R.id.scram_gridView) as GridView
        gridAdapter = MyGridAdapter(view.context, gridList)
        gridView.adapter = gridAdapter

        return view
    }

    private fun showSolve(cube: IntArray): String {
        val st = getSolve(cube)
        return if (checkBoxShowSolve.isChecked) {
            st
        } else {
            st.split(" ".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray().size.toString()
        }
    }

    private fun resetCube(): IntArray {
        Log.v (TAG, "FragmentScrambleGen resetCube")
        val cube = IntArray(54)
        for (i in cube.indices) {
            cube[i] = i / 9
        }
        return cube
    }

    private fun showCube(cube: IntArray) {
        Log.v (TAG, "FragmentScrambleGen showCube")
        gridAdapter.gridList = prepareCubeToShowInGridView(cube)
        gridAdapter.notifyDataSetChanged()
    }

    private fun prepareCubeToShowInGridView(cube: IntArray) : ArrayList<CubeAzbuka> {
        Log.v (TAG, "FragmentScrambleGen prepareCubeToShowInGridView")
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

    private fun getSolve(maincube: IntArray): String {
        var solve = "("
        var cube = maincube.clone()
        do {
            val sumColor = getColorOfElement(cube,23,30)
            val sc = edgeBufferSolve(cube, mainEdge[sumColor]!!, solve)
            solve = sc.solve
            cube = sc.cube
        } while (!isAllEdgesOnItsPlace(cube))

        solve = solve.trim { it <= ' ' }
        solve += ") "
        val j = solve.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray().size
        if (j % 2 != 0) {
            solve += "Эк "
        }

        solve += "("
        do {
            val sumColor = getColorOfElement(cube,18,11)
            val sc = cornerBufferSolve(cube,  mainCorner[sumColor]!!, solve)
            solve = sc.solve
            cube = sc.cube
        } while (!isAllCornersOnItsPlace(cube))

        solve = solve.trim { it <= ' ' }
        solve += ")"
        return solve
    }


    private fun scrambleGenerate(chkEdgesBuffer: Boolean, chkCornersBuffer: Boolean, scrambleLength: Int) {
        Log.v(TAG, "FragmentScrambleGen scrambleGenerate")
        // делаем кнопку "Генерерировать" не активной, прогресбар активным и убираем решение скрамбла
        button_generate.isEnabled = false
        progressBar.visibility = View.VISIBLE
        progressText.visibility = View.VISIBLE
        textSolve.text = ""
        //запускаем в бэкграунде поиск скрамбла удовлетворяющего условиям
        async(UI) {
            val data = bg {
                // Выполняем в background, ключевое слово bg
                generateScrambleWithParam(chkEdgesBuffer, chkCornersBuffer, scrambleLength)
            }
            // А этот код будет уже в UI потоке, отображаем результаты поиска
            showScrambleGenResult(data.await())
        }
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
            //сгенерируем скрамбл длинны указанной в поле ScrambleLength
            scramble = generateScramble(lenScramble)
            //разбираем кубик по скрамблу
            var genScrambleCube = runScramble(resetCube(), scramble)
            //устанавливаем флаги в начальное положение, обнуляем решение
            var isEdgeMelted = false
            var isCornerMelted = false
            var result = true

            //проверяем подходит ли нам скрамбл, для этого собираем кубик,
            do {
                //сначала ребра: смотрим что в буфере ребер
                val sumColor = getColorOfElement(genScrambleCube,23,30)
                //если там буферный элемент бело-красный или красно-белый, то ставим признак переплавки
                if ((sumColor == 43) or (sumColor == 34)) { isEdgeMelted = true }
                // ставим на место ребро из буфера, решение не волнует, поэтому ""
                val sc = edgeBufferSolve(genScrambleCube, mainEdge[sumColor]!!, "")
                // сохраняем результаты выполнения одной "буквы"
                genScrambleCube = sc.cube
            // выполняем пока все ребра не будут на своих местах
            } while (!isAllEdgesOnItsPlace(genScrambleCube))

            do {
                //сначала ребра: смотрим что в буфере углов (18,11)
                val sumColor = getColorOfElement(genScrambleCube,18,11)
                //если там буферный элемент (бело-красный-зеленый), то ставим признак переплавки
                if ((sumColor == 18) or (sumColor == 11) or (sumColor == 6)) { isCornerMelted = true }
                // ставим на место угол из буфера
                val sc = cornerBufferSolve(genScrambleCube, mainCorner[sumColor]!!, "")
                genScrambleCube = sc.cube
            } while (!isAllCornersOnItsPlace(genScrambleCube))
            Log.v(TAG, "Проверка Scramble $scramble, Переплавка буфера ребер - $isEdgeMelted , Переплавка буфера углов - $isCornerMelted")
            if (isEdgeMelted && checkEdge) { result = false }
            if (isCornerMelted && checkCorner) { result = false }
        } while (!result)
        Log.v(TAG, "Таки скрамбл $scramble подошел под наши условия")

        return scramble
    }

    //Генерация скрамбла определенной длинны (без учета переплавки буфера)
    private fun generateScramble(length: Int): String {
        Log.v(TAG, "FragmentScrambleGen generateScramble $length")
        val random = Random()
        var scramble = ""
        var i = 0
        var prevRandom = 9
        val map = hashMapOf(1 to "R", 2 to "U", 3 to "F", 4 to "L", 5 to "D", 6 to "B" )

        do {
            val curRandom = random.nextInt(1..7)                     //генерируем число от 1 до 6
            if (curRandom != prevRandom) {
                    i++                                                 //увеличиваем счетчик на 1
                    // ход будет по часовой, против или двойной
                    when (random.nextInt(3)) {
                        //по часовой
                        0 -> { scramble = "$scramble${map[curRandom]} " }      //просто добавляем букву
                        //против часовой
                        1 -> { scramble = "$scramble${map[curRandom]}' " }      //добавляем букву c '
                        //двойной
                        2 -> { scramble = "$scramble${map[curRandom]}2 " }      //добавляем двойку
                    }
                    prevRandom = curRandom                              //запоминаем это число в prevRandom
                }
        } while (i < length)

        scramble = scramble.trim (' ')                 //убираем лишние пробелы
        return scramble
    }

    //получаем цвет переданных ячеек куба (двузначное число, первая и вторая цифры которого соответствую икомым цветам)
    private fun getColorOfElement(cube: IntArray, firstElement: Int, secondElement: Int): Int
            = (cube[firstElement] + 1) * 10 + cube[secondElement] + 1

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


    private fun findLetter(c: Int): String {     //Доработать функцию поиска буквы из азбуки, пока просто цифра
        val listPagerLab = ListPagerLab.get(context)
        val azbuka = listPagerLab.getCustomAzbuka()
        return azbuka[c]
    }


    fun Random.nextInt(range: IntRange): Int {
        return range.start + nextInt(range.last - range.start)
    }

    private fun initArrays() {        //Инициализируем таблицы соответствий
        //Создаем табличку (словарь) номеров основных ребер, для определенных сочетаний цветов, остальные элементы равны null
        //первое число цвет, например 12 = синий 1 + оранжевый 2
        //второе число номер основного(первого) цвета как элемента куба (0..53)
        mainEdge.clear()
        mainEdge.put(12, 3)      //для сине-оранжевого ребра
        mainEdge.put(13, 7)      //для сине-белого ребра
        mainEdge.put(14, 5)      //для сине-красного ребра
        mainEdge.put(15, 1)      //для сине-желтого ребра
        mainEdge.put(21, 10)     //для оранжево-синей ребра
        mainEdge.put(23, 14)     //для оранжево-белого ребра
        mainEdge.put(25, 12)     //для оранжево-желтого ребра
        mainEdge.put(26, 16)     //для оранжево-зеленого ребра
        mainEdge.put(31, 19)     //для бело-синей ребра
        mainEdge.put(32, 21)     //для бело-оранжевого ребра
        mainEdge.put(34, 23)     //для бело-красного ребра
        mainEdge.put(36, 25)     //для бело-зеленого ребра
        mainEdge.put(41, 28)     //для красно-синей ребра
        mainEdge.put(43, 30)     //для красно-белого ребра
        mainEdge.put(45, 32)     //для красно-желтого ребра
        mainEdge.put(46, 34)     //для красно-зеленого ребра
        mainEdge.put(51, 37)     //для желто-синей ребра
        mainEdge.put(52, 41)     //для желто-оранжевого ребра
        mainEdge.put(54, 39)     //для желто-красного ребра
        mainEdge.put(56, 43)     //для желто-зеленого ребра
        mainEdge.put(62, 48)     //для зелено-оранжевого ребра
        mainEdge.put(63, 46)     //для зелено-белого ребра
        mainEdge.put(64, 50)     //для зелено-красного ребра
        mainEdge.put(65, 52)     //для зелено-желтого ребра

        //Создаем табличку соответствия основного цвета и дополнительного цвета ребра [где искать второй цвет)
        //первая и вторая цифра номер соответствующих друг другу позиций ребра в кубе. т.е. 1->37, 37->1
        dopEdge.clear()
        dopEdge.put(1, 37)           //сине-желтое
        dopEdge.put(3, 10)           //сине-оранжевое
        dopEdge.put(5, 28)           //сине-красное
        dopEdge.put(7, 19)           //сине-белое
        dopEdge.put(10, 3)           //оранжево-синяя
        dopEdge.put(12, 41)          //оранжево-желтое
        dopEdge.put(14, 21)          //оранжево-белое
        dopEdge.put(16, 48)          //оранжево-зеленое
        dopEdge.put(19, 7)           //бело-синяя
        dopEdge.put(21, 14)          //бело-оранжевое
        dopEdge.put(23, 30)          //бело-красное
        dopEdge.put(25, 46)          //бело-зеленое
        dopEdge.put(28, 5)           //красно-синяя
        dopEdge.put(30, 23)          //красно-белое
        dopEdge.put(32, 39)          //красно-желтое
        dopEdge.put(34, 50)          //красно-зеленое
        dopEdge.put(37, 1)           //желто-синяя
        dopEdge.put(39, 32)          //желто-красное
        dopEdge.put(41, 12)          //желто-оранжевое
        dopEdge.put(43, 52)          //желто-зеленое
        dopEdge.put(46, 25)          //зелено-белое
        dopEdge.put(48, 16)          //зелено-оранжевое
        dopEdge.put(50, 34)          //зелено-красное
        dopEdge.put(52, 43)          //зелено-желтое

        //Создаем табличку номеров основных углов, для определенных сочетаний цветов (по цвету его место)
        //первое число цвет, например 12 = синий 1 + оранжевый 2
        //второе число номер основного(первого) цвета как элемента куба (0..53)
        mainCorner.clear()
        mainCorner.put(12, 0)      //для сине-оранжево-желтого угла
        mainCorner.put(13, 6)      //для сине-бело-оранжевого угла
        mainCorner.put(14, 8)      //для сине-красно-белого угла
        mainCorner.put(15, 2)      //для сине-желто-красного угла
        mainCorner.put(21, 11)     //для оранжево-сине-белого угла
        mainCorner.put(23, 17)     //для оранжево-бело-зеленого угла
        mainCorner.put(25, 9)     //для оранжево-желто-синего угла
        mainCorner.put(26, 15)     //для оранжево-зелено-желтого угла
        mainCorner.put(31, 20)     //для бело-сине-красного угла
        mainCorner.put(32, 18)     //для бело-оранжево-синего угла
        mainCorner.put(34, 26)     //для бело-красно-зеленого угла
        mainCorner.put(36, 24)     //для бело-зелено-оранжевого угла
        mainCorner.put(41, 29)     //для красно-сине-желтого угла
        mainCorner.put(43, 27)     //для красно-бело-синего угла
        mainCorner.put(45, 35)     //для красно-желто-зеленого угла
        mainCorner.put(46, 33)     //для красно-зелено-белого угла
        mainCorner.put(51, 38)     //для желто-сине-оранжевого угла
        mainCorner.put(52, 44)     //для желто-оранжево-зеленого угла
        mainCorner.put(54, 36)     //для желто-красно-синего угла
        mainCorner.put(56, 42)     //для желто-зелено-красного угла
        mainCorner.put(62, 45)     //для зелено-оранжево-белого угла
        mainCorner.put(63, 47)     //для зелено-бело-красного угла
        mainCorner.put(64, 53)     //для зелено-красно-желтого угла
        mainCorner.put(65, 51)     //для зелено-желто-оранжевого угла

        //Создаем табличку соответствия основного и дополнительного угла [где искать второй цвет]
        //углы рассматриваем по часовой стрелке, поэтому достаточно первых двух цветов, чтобы пределить угол
        //первая и вторая цифра номер соответствующих позиций угла в кубе. т.е. 0->9, 9->38, 38->0
        dopCorner.clear()
        dopCorner.put(0, 9)       //сине-оранжево-желтый Л
        dopCorner.put(2, 36)       //сине-желто-красный К
        dopCorner.put(6, 18)       //сине-бело-оранжевый М
        dopCorner.put(8, 27)       //сине-красно-белый И
        dopCorner.put(9, 38)      //оранжево-желто-синий Р
        dopCorner.put(11, 6)       //оранжево-сине-белый Н
        dopCorner.put(15, 51)      //оранжево-зелено-желтый П
        dopCorner.put(17, 24)      //оранжево-бело-зеленый О
        dopCorner.put(18, 11)      //бело-оранжево-синий А
        dopCorner.put(20, 8)      //бело-сине-красный Б
        dopCorner.put(24, 45)      //бело-зелено-оранжевый Г
        dopCorner.put(26, 33)      //бело-красно-зеленый В
        dopCorner.put(27, 20)      //красно-бело-синяя Ф
        dopCorner.put(29, 2)      //красно-сине-желтая У
        dopCorner.put(33, 47)      //красно-зелено-белая С
        dopCorner.put(35, 42)      //красно-желто-зеленая Т
        dopCorner.put(36, 29)      //желто-красно-синяя Ц
        dopCorner.put(38, 0)      //желто-сине-оранжевая Х
        dopCorner.put(42, 53)      //желто-зелено-красная Ч
        dopCorner.put(44, 15)      //желто-оранжево-зеленая Ш
        dopCorner.put(45, 17)      //зелено-оранжево-белая Д
        dopCorner.put(47, 26)      //зелено-бело-красная Е
        dopCorner.put(51, 44)      //зелено-желто-оранжевая З
        dopCorner.put(53, 35)      //зелено-красно-желтая Ж

        // Порядок поиска свободной корзины для переплавки ребра
        edgePriority = hashMapOf(
                0 to 21,     // в первую очередь проверяем не занята ли бело-оранжевое ребро
                1 to 25,            // бело-зеленое
                2 to 48,            // зелено-оранжевое
                3 to 3,             // сине-оранжевое
                4 to 41,            // желто-оранжевое
                5 to 43,            // желто-зеленое
                6 to 37,            // желто-синее
                7 to 39,            // желто-красное
                8 to 7,             // сине-белое
                9 to 34,            // красно-зеленое
                10 to 28)            // красно-синее

        // Порядок поиска свободной корзины для переплавки угла
        cornerPriority = hashMapOf(
                0 to 26,     // в первую очередь проверяем не занят ли бело-красно-зеленый угол
                1 to 44,            // желто-зелено-оранжевый
                2 to 36,            // желто-красно-синий
                3 to 42,            // желто-красно-зеленый
                4 to 38,            // желто-сине-оранжевый
                5 to 20,            // бело-сине-красный
                6 to 24)            // бело-зелено-оранжевый
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
            Log.v(DebugTag.TAG, "FragmentScrambleGen onFragmentInteraction")
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

