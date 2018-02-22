package ru.tohaman.rg2.activities

import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.content.ContextCompat
import android.widget.ImageView
import android.widget.LinearLayout
import org.jetbrains.anko.ctx
import org.jetbrains.anko.find
import org.jetbrains.anko.image
import java.util.*
import android.support.v4.graphics.drawable.DrawableCompat
import android.util.Log
import android.view.View
import android.widget.Button
import kotlinx.android.synthetic.main.activity_all_test_game.*
import ru.tohaman.rg2.*
import ru.tohaman.rg2.data.ListPager
import ru.tohaman.rg2.data.ListPagerLab
import ru.tohaman.rg2.util.*


class OllTestGame : MyDefaultActivity() {
    private val random = Random()
    private lateinit var guessLinearLayouts : Array<LinearLayout>
    private val ollRnd = ArrayList<Int>()
    private lateinit var listPagers : List<ListPager>
    private lateinit var imgView: ImageView
    private var guessRows = 2
    private var CorrectAnswer = "Снежинка"
    private var correctAnswersCount = 0
    private var unCorrectAnswersCount = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        Log.v (DebugTag.TAG, "OLLTestGame onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_test_game)
        actionBar?.setDisplayHomeAsUpEnabled(true)
        imgView = findViewById(R.id.test_image)

        guessLinearLayouts = arrayOf(
                find(R.id.row1LinearLayout),
                find(R.id.row2LinearLayout),
                find(R.id.row3LinearLayout),
                find(R.id.row4LinearLayout))

        // настраиваем слушателя, для каждой кнопки
        for (row in guessLinearLayouts) {
            (0 until row.childCount)
                    .map { row.getChildAt(it) as Button }
                    .forEach { it.setOnClickListener(guessButtonListener) }
        }
        //Скрываем ненужные кнопки
        val sp = PreferenceManager.getDefaultSharedPreferences(ctx)
        guessRows = sp.getInt(TEST_GAME_ROW_COUNT, 6) / 2
        updateGuessRows(guessRows, guessLinearLayouts)

        val listPagerLab = ListPagerLab.get(ctx)
        listPagers = listPagerLab.getPhaseList("OLLTEST")
        // Если еще нет текущих значений, значит они равны названиям Доллгожданного OLL
        if (listPagers[0].comment == "") {
            listPagers.forEach {
                it.comment = it.title
                listPagerLab.updateListPager(it)
            }
        }
        ollRnd.clear()
        //записываем в ollRnd значения от 0 до listPagers.size (0.1.2.3.4.5.6.7...)
        listPagers.indices.mapTo(ollRnd) { it }

        loadNextOLL(guessRows)
    }

    private fun loadNextOLL(guessRows: Int){
        // перемешиваем алгоритмы
        Collections.shuffle(ollRnd)

        // последний в списке считаем верным
        val correct =  ollRnd[ollRnd.size - 1]
        CorrectAnswer = listPagers[correct].comment

        // add 2, 4, 6 or 8 кнопок в зависимости от значения guessRows
        // и заполняем эти кнопки случайными заведомо неверными названиями алгоритмов,
        // т.к. верное название у нас последнее в rndAzbuka (списке)
        for (row in 0 until guessRows) {
            // place Buttons in currentTableRow
            for (column in 0 until guessLinearLayouts[row].childCount) {
                // получить ссылку на Button для конфигурации
                val newGuessButton = guessLinearLayouts[row].getChildAt(column) as Button
                newGuessButton.isEnabled = true  // активируем кнопку
                //newGuessButton.lines = 2
                // пишем текст а названием алгоритма на кнопку
                newGuessButton.text = listPagers[ollRnd[row * 2 + column]].comment
            }
        }

        // заменяем случайную кнопку (текст) на правильный
        val row = random.nextInt(guessRows)
        val column = random.nextInt(2)
        val randomRow = guessLinearLayouts[row] // получить строку
        (randomRow.getChildAt(column) as Button).text = CorrectAnswer

        val scrm  = ollScramble[correct].toString()
        var scramble = addRotate2Scramble( scrm )
        imgView.image = getCompleteDrawable(scramble)
    }

    // возвращает внешний вид кубика разобранного по скрамблу (в виде 28-ми слойного Drawable)
    private fun getCompleteDrawable (scramble:String):LayerDrawable {
        Log.v (DebugTag.TAG, "BlindTestGame getCompleteDrawable")
        val genScrambleCube = runScramble(resetCube(), scramble)

        //цвет фона кубика
        genScrambleCube[27] = 6

        return LayerDrawable( Array(28, { i ->
            //получаем drawable по имени "z_2s_0$i"
            val drw = ContextCompat.getDrawable(ctx, resources.getIdentifier("z_2s_0$i", "drawable", this.packageName))
            //раскрашиваем цветом кубика
            DrawableCompat.setTint(drw!!, ContextCompat.getColor(ctx,cubeColor[genScrambleCube[27-i]]))
            drw
        }))
    }

    //Добавляем случайные вращения кубика перед выполнением скрабла и вращение крыши после
    private fun addRotate2Scramble(scrm: String):String {
        var scramble = scrm
        scramble = "x x $scramble"
        //крутим куб перед скрамблом, чтобы не всегда был зелено-оранжевой стороной к нам
        var i = 0
        val yCube = random.nextInt(4)
        while (i < yCube) {
            scramble = "y $scramble"
            i += 1
        }
        //крутим крышу на случайное кол-во поворотов, после исполнения скрамбла разборки
        val uCube = random.nextInt(4)
        i = 0
        while (i < uCube) {
            scramble = "$scramble U"
            i += 1
        }
        return scramble
    }


    private fun updateGuessRows(guessRows: Int, guessLinearLayouts: Array<LinearLayout>) {
        Log.v (DebugTag.TAG, "BlindTestGame updateGuessRows")
        // Сначала скрываем все кнопки (точнее ряды) скрытыми
        for (layout in guessLinearLayouts)
            layout.visibility = View.GONE
        // Делаем видимыми только нужное кол-во рядов
        for (row in 0 until guessRows)
            guessLinearLayouts[row].visibility = View.VISIBLE
    }


    //Назначаем обработчик на кнопки
    private val guessButtonListener = View.OnClickListener { v ->
        val guessButton = v as Button

        val guess = guessButton.text.toString()
        if (guess == CorrectAnswer) {   //верный ответ
            correctAnswersCount += 1
            correct_text.text = correctAnswersCount.toString()
            loadNextOLL(guessRows)
        } else {    //неправильный ответ
            unCorrectAnswersCount += 1
            uncorrect_text.text = unCorrectAnswersCount.toString()
            guessButton.isEnabled = false
        }
    }

}
