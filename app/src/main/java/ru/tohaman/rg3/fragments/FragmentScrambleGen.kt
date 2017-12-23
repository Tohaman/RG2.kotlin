package ru.tohaman.rg3.fragments


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
import org.jetbrains.anko.backgroundColorResource
import org.jetbrains.anko.coroutines.experimental.bg
import org.jetbrains.anko.sdk15.coroutines.onCheckedChange
import org.jetbrains.anko.sdk15.coroutines.onClick
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
    private lateinit var textScramble: TextView
    private lateinit var textSolve: TextView
    private lateinit var buttonPlus: Button
    private lateinit var buttonMinus: Button

    private var mainEdge: HashMap<Int,Int> = hashMapOf(0 to 0)
    private var dopEdge: HashMap<Int,Int> = hashMapOf(0 to 0)
    private var mainCorner: HashMap<Int,Int> = hashMapOf(0 to 0)
    private var popCorner: HashMap<Int,Int> = hashMapOf(0 to 0)
    private var listEdgesOnPlace: SortedMap<Int,Int> = sortedMapOf(0 to 0)
    private var edgePriority: HashMap<Int,Int> = hashMapOf(0 to 0)
    private var cornerPriority: HashMap<Int,Int> = hashMapOf(0 to 0)

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.v (TAG, "FragmentScrambleGen onCreateView - start")

        sp = PreferenceManager.getDefaultSharedPreferences(context)
        val scramble = sp.getString(SCRAMBLE, "U2 F2 L\' D2 R U F\' L2 B2 R L2 B2 U R")
        var chkEdgesBuffer = sp.getBoolean(CHK_BUF_EDGES, true)
        var chkCornersBuffer = sp.getBoolean(CHK_BUF_CORNERS, false)
        var scrambleLength = sp.getInt(SCRAMBLE_LEN, 12)
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
            generateScrambleWithParam(chkEdgesBuffer, chkCornersBuffer, scrambleLength)
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

        //Текст самого скрамбла
        textScramble = view.findViewById(R.id.scramble)

        //Текст с решением скрамбла
        textSolve = view.findViewById(R.id.solve_text)

        currentCube = initialize()                  //берем собранный кубик
        gridList = initGridList(currentCube)        //подготавливаем текущий кубик для вывода в GridView
        val a = isAllEdgesOnItsPlace(currentCube)

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
        gridAdapter.gridList = initGridList(cube)
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
        // берем собранный куб и выводим его на экран А смысл???, закоментировал
//        currentCube = initialize()
//        showCube(currentCube)
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

    private fun showScrambleGenResult(genRes: GenerateResult) {
        Log.v(TAG, "FragmentScrambleGen showScrambleGenResult ${genRes.scramble}, ${genRes.solve}")
        button_generate.isEnabled = true
        progressBar.visibility = View.INVISIBLE
        progressText.visibility = View.INVISIBLE
        textScramble.text = genRes.scramble
        textSolve.text = genRes.solve
        runScramble(currentCube, genRes.scramble)
        showCube(currentCube)
    }


    private fun generateScrambleWithParam(checkEdge: Boolean, checkCorner: Boolean, lenScramble: Int): GenerateResult {
        Log.v(TAG, "Ищем скрамбл подходящий по параметрам переплавок буфера и длинне")

        var j = 0                                  //счетчик количества попыток найти скрмбл
        var genRes = GenerateResult("","")

        do {
            var genScrambleCube = initialize()
//            j++
            genRes.scramble = generateScramble(lenScramble)     //сгенерировать скрамбл длинны указанной в поле ScrambleLength
//            genRes.scramble = "R' F' R U2 B R' D' U2 B' L'"
            genScrambleCube = runScramble(genScrambleCube, genRes.scramble)
            var isEdgeMelted = false
            var isCornerMelted = false
            var result = true
            genRes.solve = ""

            do {
                var summColor = getColorOfElement(genScrambleCube,23,30) //смотрим что в буфере
                if ((summColor == 43) or (summColor == 34)) {           //если там буферный элемент бело-красный или красно-белый, то
                    isEdgeMelted = true                                 //ставим признак переплавки
                }
                val sc = edgeBufferSolve(genScrambleCube, mainEdge[summColor]!!, genRes.solve)

                genRes.solve = sc.solve
                genScrambleCube = sc.cube
            } while (!isAllEdgesOnItsPlace(genScrambleCube))


            val d = genRes.solve.split(" ".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray().size
            if (d % 2 != 0) {
                genRes.solve += "Эк "
            }

//            do {
//                val a = CurCube[18] + 1       //смотрим что в буфере углов
//                val b = CurCube[11] + 1
//                val c = mainCorner[a * 10 + b]
//                if ((c == 18) or (c == 11) or (c == 6)) {
//                    isCornerMelted = true
//                }
//                val sc = BufferUgolSolve(CurCube, c, solve)
//                solve = sc.getSolve()
//                CurCube = sc.getCube()
//            } while ((!CheckUgol(CurCube))!!)
            Log.v(TAG, "Проверка Scramble ${genRes.scramble}, Переплавка буфера ребер - $isEdgeMelted , Переплавка буфера углов - $isCornerMelted")
            if (isEdgeMelted && checkEdge) { result = false }
            if (isCornerMelted && checkCorner) { result = false }
        } while (!result)
        Log.v(TAG, "Таки скрамбл ${genRes.scramble} подошел под условия")
        //solve = solve + j;            //добавить к решению количество попыток решения
        return genRes
    }

    //получаем цвет переданных ячеек куба
    private fun getColorOfElement(cube: IntArray, firstElement: Int, secondElement: Int): Int {
//        Log.v(TAG, "FragmentScrambleGen getColorOfElement")
        val firstColor = cube[firstElement] + 1       //смотрим что в буфере ребер
        val secondColor = cube[secondElement] + 1
        return firstColor * 10 + secondColor
    }


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

    private fun isAllEdgesOnItsPlace(cube: IntArray): Boolean {    //проверяем все ли грани на своих местах
        var check = true
        listEdgesOnPlace.clear()
        var j = 0
        for (i in 0..52) {
            val secColor = dopEdge[i]
            if (secColor != null) {
                val fcolor = getColorOfElement(cube,i,secColor)
                if (mainEdge[fcolor] != i) {
                    listEdgesOnPlace.put(j, i )
                    j++
                    check = false
                }
            }
        }
        return check
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
        popCorner.clear()
        popCorner.put(0, 9)       //сине-оранжево-желтый Л
        popCorner.put(2, 36)       //сине-желто-красный К
        popCorner.put(6, 18)       //сине-бело-оранжевый М
        popCorner.put(8, 27)       //сине-красно-белый И
        popCorner.put(9, 38)      //оранжево-желто-синий Р
        popCorner.put(11, 6)       //оранжево-сине-белый Н
        popCorner.put(15, 51)      //оранжево-зелено-желтый П
        popCorner.put(17, 24)      //оранжево-бело-зеленый О
        popCorner.put(18, 11)      //бело-оранжево-синий А
        popCorner.put(20, 8)      //бело-сине-красный Б
        popCorner.put(24, 45)      //бело-зелено-оранжевый Г
        popCorner.put(26, 33)      //бело-красно-зеленый В
        popCorner.put(27, 20)      //красно-бело-синяя Ф
        popCorner.put(29, 2)      //красно-сине-желтая У
        popCorner.put(33, 47)      //красно-зелено-белая С
        popCorner.put(35, 42)      //красно-желто-зеленая Т
        popCorner.put(36, 29)      //желто-красно-синяя Ц
        popCorner.put(38, 0)      //желто-сине-оранжевая Х
        popCorner.put(42, 53)      //желто-зелено-красная Ч
        popCorner.put(44, 15)      //желто-оранжево-зеленая Ш
        popCorner.put(45, 17)      //зелено-оранжево-белая Д
        popCorner.put(47, 26)      //зелено-бело-красная Е
        popCorner.put(51, 44)      //зелено-желто-оранжевая З
        popCorner.put(53, 35)      //зелено-красно-желтая Ж

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

    inner class GenerateResult (var scramble: String, var solve: String)    //скрамбл, решение

    inner class SolveCube (var cube: IntArray, var solve: String)   // куб, решение

}

