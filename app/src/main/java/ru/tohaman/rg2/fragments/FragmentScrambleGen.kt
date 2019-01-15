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
import kotlinx.coroutines.*
import org.jetbrains.anko.*
//TODO Check Anko SDK Version change to 27
import org.jetbrains.anko.sdk15.coroutines.onCheckedChange
import org.jetbrains.anko.sdk15.coroutines.onClick
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.startActivity
import ru.tohaman.rg2.*
import ru.tohaman.rg2.DebugTag.TAG
import ru.tohaman.rg2.activities.TimerActivity
import ru.tohaman.rg2.adapters.MyGridAdapter
import ru.tohaman.rg2.data.ListPagerLab
import ru.tohaman.rg2.util.*
import java.util.*
import kotlin.coroutines.CoroutineContext

/**
 * Created by anton on 27.11.17. Фрагмент отображающий генератор скрамблов
 *
 */

class FragmentScrambleGen : Fragment(), CoroutineScope {

    private var mListener: OnScrambleGenInteractionListener? = null

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

    // корутина может быть легко ограничена жизненным циклом Fragment
    private val scope = CoroutineScope(Dispatchers.Main)

    private val job = SupervisorJob()

    override val coroutineContext = Dispatchers.Main + job

    override fun onDestroy() {
        super.onDestroy()
        coroutineContext.cancelChildren()
    }


//    private var listEdgesOnPlace: SortedMap<Int,Int> = sortedMapOf(0 to 0)
//    private var listCornersOnPlace: SortedMap<Int,Int> = sortedMapOf(0 to 0)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.v (TAG, "FragmentScrambleGen onCreateView - start")

        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        val uri = activity?.intent?.data
        var scramble = sp.getString(SCRAMBLE, "U2 F2 L\' D2 R U F\' L2 B2 R L2 B2 U R")!!
        // Если вызван с параметром, то скрамбл взять из параметра, а не из базы
        if (uri != null) {
            scramble = uri.getQueryParameter("scram")!!
            scramble = scramble.replace("_", " ")
        }
        var chkEdgesBuffer = sp.getBoolean(CHK_BUF_EDGES, true)
        var chkCornersBuffer = sp.getBoolean(CHK_BUF_CORNERS, false)
        var scrambleLength = sp.getInt(SCRAMBLE_LEN, 14)
        var chkShowSolve = sp.getBoolean(CHK_SHOW_SOLVE, true)

        val view = inflater.inflate(R.layout.fragment_scramble_gen, container, false)

        Log.v (TAG, "FragmentScrambleGen onCreateView - hide ProgressBar & ProgressText")
        progressBar = view!!.findViewById(R.id.progressBar)
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
            if (scrambleLength > 30) {
                scrambleLength = 30
            }
            textScrambleLen.text = scrambleLength.toString()
            saveInt2SP(scrambleLength, SCRAMBLE_LEN, v!!.context)
        }

        // Кнопка -
        buttonMinus = view.findViewById(R.id.button_minus)
        buttonMinus.onClick { v ->
            scrambleLength--
            if (scrambleLength < 3) {
                scrambleLength = 3
            }
            textScrambleLen.text = scrambleLength.toString()
            saveInt2SP(scrambleLength, SCRAMBLE_LEN, v!!.context)
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
                            val scrambleString = setScramble.text.toString()
                            textScramble.text = scrambleString
                            saveString2SP(scrambleString,SCRAMBLE, v!!.context)
                            val cube = runScramble(resetCube(), scrambleString)
                            showCube(cube)
                            textSolve.text = showSolve(cube)
                        }
                        negativeButton("Отмена") {}
                    }
                }
            }.show()
        }

        val timerButton = view.find<Button> (R.id.timerButton)
        timerButton.setOnClickListener {
            Log.v (TAG, "TimerButton Click")
            startActivity<TimerActivity>()
        }

        // Чекбокс отображать или нет решение скрамбла
        checkBoxShowSolve = view.find(R.id.checkBox_solve)
        checkBoxShowSolve.isChecked = chkShowSolve
        checkBoxShowSolve.onCheckedChange { buttonView, isChecked ->
            Log.v (TAG, "CheckBoxShowSolve changed")
            chkShowSolve = isChecked
            val cube = runScramble(resetCube(), textScramble.text as String)
            textSolve.text = showSolve(cube)
            saveBoolean2SP(chkShowSolve, CHK_SHOW_SOLVE, buttonView!!.context)
        }

        val currentCube = runScramble(resetCube(), scramble)
        //Текст с решением скрамбла
        textSolve = view.findViewById(R.id.solve_text)
        textSolve.text = showSolve(currentCube)

        val gridList = prepareCubeToShowInGridView(currentCube)        //подготавливаем текущий кубик для вывода в GridView

        //находим GridView и выводим в него текущий кубик
        val gridView = view.find<GridView>(R.id.scram_gridView)
        gridAdapter = MyGridAdapter(view.context, gridList)
        gridView.adapter = gridAdapter

        return view
    }

    private fun showSolve(cube: IntArray): String {
        val st = getSolve(cube, requireContext()).first
        return if (checkBoxShowSolve.isChecked) {
            st
        } else {
            st.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray().size.toString()
        }
    }

    private fun showCube(cube: IntArray) {
        Log.v (TAG, "FragmentScrambleGen showCube")
        gridAdapter.gridList = prepareCubeToShowInGridView(cube)
        gridAdapter.notifyDataSetChanged()
    }


    private suspend fun scrambleGenerate(chkEdgesBuffer: Boolean, chkCornersBuffer: Boolean, scrambleLength: Int) {
        Log.v(TAG, "FragmentScrambleGen scrambleGenerate")
        // делаем кнопку "Генерерировать" не активной, прогресбар активным и убираем решение скрамбла
        button_generate.isEnabled = false
        progressBar.visibility = View.VISIBLE
        progressText.visibility = View.VISIBLE
        textSolve.text = ""
        //запускаем в бэкграунде поиск скрамбла удовлетворяющего условиям
        scope.launch (Dispatchers.Main) {
            val scramble = generateScrambleWithParam(chkEdgesBuffer, chkCornersBuffer, scrambleLength, requireContext())
            showScrambleGenResult (scramble)
        }
    }

    private fun showScrambleGenResult(genRes: String) {
        Log.v(TAG, "FragmentScrambleGen showScrambleGenResult $genRes")
        button_generate.isEnabled = true
        progressBar.visibility = View.INVISIBLE
        progressText.visibility = View.INVISIBLE
        textScramble.text = genRes
        saveString2SP(genRes, SCRAMBLE, ctx)
        val cube = runScramble(resetCube(), genRes)
        showCube(cube)
        textSolve.text = showSolve(cube)

    }

    override fun onAttach(context: Context?) {
        Log.v (DebugTag.TAG, "FragmentScrambleGen onAttach")
        super.onAttach(context)
        if (context is FragmentScrambleGen.OnScrambleGenInteractionListener) {
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

    interface OnScrambleGenInteractionListener {
        fun onScrambleGenInteraction(button: String) {
            Log.v(DebugTag.TAG, "FragmentScrambleGen onListViewInteraction")

        }
    }

    companion object {
        fun newInstance(): FragmentScrambleGen {
            Log.v(DebugTag.TAG, "FragmentScrambleGen newInstance")
            return FragmentScrambleGen()
        }

        fun generateScrambleWithParam(checkEdge: Boolean, checkCorner: Boolean, lenScramble: Int, context: Context): String {
            Log.v(TAG, "Ищем скрамбл подходящий по параметрам переплавок буфера и длине")
            var scramble: String
            do {
                var result = true
                //сгенерируем скрамбл длинны указанной в поле ScrambleLength
                scramble = generateScramble(lenScramble)
                //scramble = "B2 D' B2 R U' D2 F B2 U' R2 B' D2 F2 R'"
                //разбираем кубик по скрамблу
                val genScrambleCube = runScramble(resetCube(), scramble)
                // получаем решение кубика (solve,isEdgeMelted,isCornerMelted)
                val triple = getSolve(genScrambleCube, context)
                val isEdgeMelted = triple.second
                val isCornerMelted = triple.third

                Log.v(TAG, "Проверка Scramble $scramble, Переплавка буфера ребер - ${triple.second} , Переплавка буфера углов - ${triple.third}")
                if (isEdgeMelted && checkEdge) { result = false }
                if (isCornerMelted && checkCorner) { result = false }
            } while (!result)
            Log.v(TAG, "Таки скрамбл $scramble подошел под наши условия")

            return scramble
        }

        //Возвращаем решение, была ли переплавка буф.ребер, была ли переплавка буф.углов
        private fun getSolve(mainCube: IntArray, context: Context):  Triple<String, Boolean, Boolean>  {
            var solve = "("
            var cube = mainCube.clone()
            var isEdgeMelted = false        //изначально считаем, что переплавок не было
            var isCornerMelted = false

            //решаем ребра
            do {
                //сначала ребра: смотрим что в буфере ребер
                val sumColor = getColorOfElement(cube, 23, 30)
                //если там буферный элемент бело-красный или красно-белый, то ставим признак переплавки
                if ((sumColor == 43) or (sumColor == 34)) { isEdgeMelted = true }
                // ставим на место ребро из буфера
                val sc = edgeBufferSolve(cube, mainEdge[sumColor]!!, solve, context)
                // сохраняем результаты выполнения одной "буквы"
                solve = sc.solve
                cube = sc.cube
                // выполняем пока все ребра не будут на своих местах
            } while (!isAllEdgesOnItsPlace(cube).allComplete)

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
                //13 = сине-белый, 32 = бело-оранжевый, 21 = оранжево-синий
                if ((sumColor == 13) or (sumColor == 32) or (sumColor == 21)) { isCornerMelted = true }
                // ставим на место угол из буфера
                val sc = cornerBufferSolve(cube,  mainCorner[sumColor]!!, solve, context)
                // сохраняем результаты выполнения одной "буквы"
                solve = sc.solve
                cube = sc.cube
                // выполняем пока все углы не будут на своих местах
            } while (!isAllCornersOnItsPlace(cube).allComplete)

            solve = solve.trim { it <= ' ' }
            solve += ")"
            return Triple(solve, isEdgeMelted, isCornerMelted)
        }


        // Установка на свое место элемента цвета elementPosition находящегося в буфере ребер
        // Возвращает SolveCube = куб после выполнения установки и решение solve + текущий ход
        private fun edgeBufferSolve(cube: IntArray, elementPosition: Int, solve: String, context: Context): SolveCube {
            var tmpCube = cube
            var positionOfElem = elementPosition
            var solv = solve
            val listPagerLab = ListPagerLab.get(context)
            val azbuka = listPagerLab.getCurrentAzbuka()

            if (!((positionOfElem == 23) or (positionOfElem == 30))) {           //проверяем, не буфер ли?, если нет, то добоавляем букву к решению
                solv += azbuka[positionOfElem] + " "        //если буфер, то будем его переплавлять и букву уже
            }                                               //подставим в рекурсии
            when (positionOfElem) {
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
                23 -> {                    // для бело-красного ребра
                    val pair4Melting = isAllEdgesOnItsPlace(tmpCube)
                    if (!pair4Melting.allComplete) {
                        val sc = meltingEdge(tmpCube, solv, pair4Melting.elementsNotOnPlace, context)
                        solv = sc.solve
                        tmpCube = sc.cube
                    }}
                25 -> tmpCube = blinde25(tmpCube)
                28 -> tmpCube = blinde28(tmpCube)
                30 -> {                      //для красно-белого ребра
                    val pair4Melting = isAllEdgesOnItsPlace(tmpCube)
                    if (!pair4Melting.allComplete) {
                        //переплавляем буфер (рекурсия)
                        val sc = meltingEdge(tmpCube, solv, pair4Melting.elementsNotOnPlace, context)
                        solv = sc.solve
                        tmpCube = sc.cube
                    }}
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

        // Установка на свое место элемента цвета elementPosition находящегося в буфере углов
        // Возвращает SolveCube = куб после выполнения установки и решение solve + текущий ход
        private fun cornerBufferSolve(cube: IntArray, elementPosition: Int, solve: String, context: Context): SolveCube {
            var tmpCube = cube
            var positionOfElem = elementPosition
            var solv = solve
            val listPagerLab = ListPagerLab.get(context)
            val azbuka = listPagerLab.getCurrentAzbuka()

            if (!(positionOfElem == 18 || positionOfElem == 11 || positionOfElem == 6)) {           //если с не равно 18,11 или 6, то буфер не на месте и добавляем букву к решению.
                solv += azbuka[positionOfElem] + " "
            }
            when (elementPosition) {
                0 -> tmpCube = blinde0(tmpCube)
                2 -> tmpCube = blinde2(tmpCube)
                6 -> {
                    val pair4Melting = isAllCornersOnItsPlace(tmpCube)
                    if (!pair4Melting.allComplete) {
                        val sc = meltingCorner(tmpCube, solv, pair4Melting.elementsNotOnPlace, context)
                        solv = sc.solve
                        tmpCube = sc.cube
                    }
                }
                8 -> tmpCube = blinde8(tmpCube)
                9 -> tmpCube = blinde9(tmpCube)
                11 -> {
                    val pair4Melting = isAllCornersOnItsPlace(tmpCube)
                    if (!pair4Melting.allComplete) {
                        val sc = meltingCorner(tmpCube, solv, pair4Melting.elementsNotOnPlace, context)
                        solv = sc.solve
                        tmpCube = sc.cube
                    }
                }
                15 -> tmpCube = blinde15(tmpCube)
                17 -> tmpCube = blinde17(tmpCube)
                18 -> {
                    val pair4Melting = isAllCornersOnItsPlace(tmpCube)
                    if (!pair4Melting.allComplete) {
                        val sc = meltingCorner(tmpCube, solv, pair4Melting.elementsNotOnPlace, context)
                        solv = sc.solve
                        tmpCube = sc.cube
                    }
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

        private fun meltingEdge(tmpCube: IntArray, solv: String, edgesListNotOnPlace: SortedMap<Int,Int>, context: Context ): SolveCube {
            var positionOfElement = 0
            // цикл поиска свободной корзины
            var j = 0
            while (positionOfElement == 0) {
                var i = 0
                do {
                    if (edgePriority[j] == edgesListNotOnPlace[i]) {
                        positionOfElement = edgePriority[j]!!
                    } //ищем ребра на своем месте по приоритету edgePriority
                    i++
                } while (edgesListNotOnPlace[i] != null)
                j++
            }
            //переплавляем буфер (рекурсия)
            return edgeBufferSolve(tmpCube, positionOfElement, solv, context)
        }

        private fun meltingCorner(tmpCube: IntArray, solv: String, cornersListNotOnPlace: SortedMap<Int,Int>, context: Context): SolveCube {
            var positionOfElement = 0
            // цикл поиска свободной корзины
            var j = 0
            while (positionOfElement == 0) {
                var i = 0
                do {
                    if (cornerPriority[j] == cornersListNotOnPlace[i]) {
                        positionOfElement = cornerPriority[j]!!
                    } //ищем ребра на своем месте по приоритету cornerPriority
                    i++
                } while (cornersListNotOnPlace[i] != null)
                j++
            }
            //переплавляем буфер (рекурсия)
            return cornerBufferSolve(tmpCube, positionOfElement, solv, context)
        }

        private fun isAllEdgesOnItsPlace(cube: IntArray): Pair4Melting {    //проверяем все ли грани на своих местах
            //предположим что все на местах
            var result = true
            //Обнуляем список ребер стоящих на местах
            var edgesListNotOnPlace: SortedMap<Int,Int> = sortedMapOf(0 to 0)
            edgesListNotOnPlace.clear()
            var j = 0
            for (i in 0..52) {
                val secColor = dopEdge[i]
                if (secColor != null) {
                    val firstColor = getColorOfElement(cube,i,secColor)
                    if (mainEdge[firstColor] != i) {
                        edgesListNotOnPlace[j] = i
                        j++
                        result = false
                    }
                }
            }
            return Pair4Melting(result, edgesListNotOnPlace)
        }

        private fun isAllCornersOnItsPlace(cube: IntArray): Pair4Melting {    //проверяем все ли углы на своих местах
            //предположим что все на местах
            var result = true
            //Обнуляем список углов стоящих на своих местах
            var cornersListNotOnPlace: SortedMap<Int,Int> = sortedMapOf(0 to 0)
            cornersListNotOnPlace.clear()
            var j = 0
            //Будем проверять все элементы кубика
            for (i in 0..52) {
                //Проверяем данный элемент угол или ребро
                val secColor = dopCorner[i]
                if (secColor != null) {
                    val fcolor = getColorOfElement(cube,i,secColor)
                    if (mainCorner[fcolor] != i) {
                        cornersListNotOnPlace[j] = i
                        j++
                        result = false
                    }
                }
            }
            return Pair4Melting(result, cornersListNotOnPlace)
        }

    }


}
