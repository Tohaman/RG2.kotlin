package ru.tohaman.rg2.activitys

import android.os.Bundle
import android.app.Activity
import android.graphics.drawable.LayerDrawable
import android.preference.PreferenceManager
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import ru.tohaman.rg2.R

import org.jetbrains.anko.ctx
import org.jetbrains.anko.find
import ru.tohaman.rg2.PLL_TEST_3SIDE
import ru.tohaman.rg2.PLL_TEST_ROW_COUNT
import ru.tohaman.rg2.data.ListPager
import ru.tohaman.rg2.data.ListPagerLab
import ru.tohaman.rg2.util.*
import java.util.*

class PllTestGame : Activity() {
    private val random = Random()
    private lateinit var listPagers : List<ListPager>
    private lateinit var guessLinearLayouts : Array<LinearLayout>
    private val pllRnd = ArrayList<String>()
    private var correctAnswer = 1
    private lateinit var imgView: ImageView
    private var guessRows = 3
    private var is3side = true

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(getThemeFromSharedPreference(ctx))
        super.onCreate(savedInstanceState)
        val sp = PreferenceManager.getDefaultSharedPreferences(ctx)
        setContentView(R.layout.activity_pll_test_game)
        actionBar?.setDisplayHomeAsUpEnabled(true)
        guessRows = sp.getInt(PLL_TEST_ROW_COUNT, 6) / 2
        is3side = sp.getBoolean(PLL_TEST_3SIDE, true)

        imgView = findViewById<ImageView>(R.id.test_image)
        imgView.setImageDrawable(genDrawable3sidePll(10))

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

        val listPagerLab = ListPagerLab.get(ctx)
        listPagers = listPagerLab.getPhaseList("PLLTEST")
        // Если еще нет текущих значений, значит они равны названиям Максимкиного PLL
        if (listPagers[0].comment == "") {
            listPagers.forEach {
                it.comment = it.title
                listPagerLab.updateListPager(it)
            }
        }
        pllRnd.clear()
        listPagers.indices.mapTo(pllRnd) { it.toString() }

        updateGuessRows(guessRows, guessLinearLayouts)
        loadNextPLL(guessRows)

    }

    private fun updateGuessRows(guessRows: Int, guessLinearLayouts: Array<LinearLayout>) {
        // Сначала скрываем все кнопки (точнее ряды) скрытыми
        for (layout in guessLinearLayouts)
            layout.visibility = View.GONE
        // Делаем видимыми только нужное кол-во рядов
        for (row in 0 until guessRows)
            guessLinearLayouts[row].visibility = View.VISIBLE
    }


    private val guessButtonListener = View.OnClickListener { v ->
        val guessButton = v as Button
        
        val guess = guessButton.text.toString()
        if (guess == getNameFromListPagers(listPagers, correctAnswer)) {   //верный ответ
            loadNextPLL(guessRows)
        } else {    //неправильный ответ
            guessButton.isEnabled = false
        }
    }

    private fun loadNextPLL(guessRows:Int) {
        // выбираем случайный алгоритм
        correctAnswer = random.nextInt(21)
        // перемешиваем алгоритмы
        Collections.shuffle(pllRnd) // shuffle file names

        // находим наш случаный в перемешенном списке и помещаем его в конец списка
        val correct = pllRnd.indexOf(correctAnswer.toString())
        pllRnd.add(pllRnd.removeAt(correct))

        // add 2, 4, 6 or 8 кнопок в зависимости от значения guessRows
        // и заполняем эти кнопки случайными заведомо неверными названиями алгоритмов,
        // т.к. верное название у нас последнее в pllrnd (списке)
        for (row in 0 until guessRows) {
            // place Buttons in currentTableRow
            for (column in 0 until guessLinearLayouts[row].getChildCount()) {
                // получить ссылку на Button для онфигурации
                val newGuessButton = guessLinearLayouts[row].getChildAt(column) as Button
                newGuessButton.isEnabled = true  // активируем кнопку
                // пишем текст а названием алгоритма на кнопку
                val numOfalg = Integer.parseInt(pllRnd.get(row * 2 + column))
                newGuessButton.text = getNameFromListPagers(listPagers, numOfalg)
            }
        }

        // заменяем случайную кнопку (текст) на правильный
        val row = random.nextInt(guessRows)
        val column = random.nextInt(2)
        val randomRow = guessLinearLayouts[row] // получить строку
        val algName = getNameFromListPagers(listPagers, correctAnswer)
        (randomRow.getChildAt(column) as Button).text = algName

        val drw0 = if (is3side) {genDrawable3sidePll(correctAnswer)} else { genDrawable2sidePll(correctAnswer)}
        imgView.setImageDrawable(drw0)

    }

    private fun genDrawable3sidePll(correctAnswer: Int): LayerDrawable {
        val stringOfTopColorOfPll = pllTopLayerColor[correctAnswer]

        val drw0 = ContextCompat.getDrawable(ctx, R.drawable.z_3s_background)
        val drw1 = ContextCompat.getDrawable(ctx, R.drawable.z_3s_up)
        val drw2 = ContextCompat.getDrawable(ctx, R.drawable.z_3s_left)
        val drw3 = ContextCompat.getDrawable(ctx, R.drawable.z_3s_front)
        val drw4 = ContextCompat.getDrawable(ctx, R.drawable.z_3s_right)

        val drw5 = ContextCompat.getDrawable(ctx, R.drawable.z_3s_left_1)
        val drw6 = ContextCompat.getDrawable(ctx, R.drawable.z_3s_left_2)
        val drw7 = ContextCompat.getDrawable(ctx, R.drawable.z_3s_left_3)
        val drw8 = ContextCompat.getDrawable(ctx, R.drawable.z_3s_front_1)
        val drw9 = ContextCompat.getDrawable(ctx, R.drawable.z_3s_front_2)
        val drw10 = ContextCompat.getDrawable(ctx, R.drawable.z_3s_front_3)
        val drw11 = ContextCompat.getDrawable(ctx, R.drawable.z_3s_right_1)
        val drw12 = ContextCompat.getDrawable(ctx, R.drawable.z_3s_right_2)
        val drw13 = ContextCompat.getDrawable(ctx, R.drawable.z_3s_right_3)

        val f2lColorOffSet = random.nextInt(4)                      //генерируем число от 0 до 3 смещение цвета для боковых граней
        val topLayColorOffSet = random.nextInt(4)                   //генерируем число от 0 до 3 смещение цвета для 3-го этажа
        val topLayerOffset = random.nextInt(4)                      //генерируем число от 0 до 3 смещение грани для 3-го этажа

        val doubleStringOfTopColorOfPll = stringOfTopColorOfPll + stringOfTopColorOfPll

        //инициализируем массив чисел из двойной строки(чтобы не было переполнения) со случайным смещением [topLayerOffset]
        val cube3 = Array(9,{ i -> Integer.parseInt(Character.toString(doubleStringOfTopColorOfPll[3 * topLayerOffset + i])) })
//      т.е. по сути делаем вот это:
//        for (i in 0..8) {
//            val temp = doubleStringOfTopColorOfPll[3 * topLayerOffset + i]
//            cube3[i] = Integer.parseInt(Character.toString(temp))
//        }

        DrawableCompat.setTint(drw1, ContextCompat.getColor(ctx,cubeColor[4]))  //верх желтый
        DrawableCompat.setTint(drw2, ContextCompat.getColor(ctx,cubeColor4PLL[f2lColorOffSet]))
        DrawableCompat.setTint(drw3, ContextCompat.getColor(ctx,cubeColor4PLL[1 + f2lColorOffSet]))
        DrawableCompat.setTint(drw4, ContextCompat.getColor(ctx,cubeColor4PLL[2 + f2lColorOffSet]))

        DrawableCompat.setTint(drw5, ContextCompat.getColor(ctx,cubeColor4PLL[cube3[0] + topLayColorOffSet]))
        DrawableCompat.setTint(drw6, ContextCompat.getColor(ctx,cubeColor4PLL[cube3[1] + topLayColorOffSet]))
        DrawableCompat.setTint(drw7, ContextCompat.getColor(ctx,cubeColor4PLL[cube3[2] + topLayColorOffSet]))
        DrawableCompat.setTint(drw8, ContextCompat.getColor(ctx,cubeColor4PLL[cube3[3] + topLayColorOffSet]))
        DrawableCompat.setTint(drw9, ContextCompat.getColor(ctx,cubeColor4PLL[cube3[4] + topLayColorOffSet]))
        DrawableCompat.setTint(drw10, ContextCompat.getColor(ctx,cubeColor4PLL[cube3[5] + topLayColorOffSet]))
        DrawableCompat.setTint(drw11, ContextCompat.getColor(ctx,cubeColor4PLL[cube3[6] + topLayColorOffSet]))
        DrawableCompat.setTint(drw12, ContextCompat.getColor(ctx,cubeColor4PLL[cube3[7] + topLayColorOffSet]))
        DrawableCompat.setTint(drw13, ContextCompat.getColor(ctx,cubeColor4PLL[cube3[8] + topLayColorOffSet]))

        val drawableArray = arrayOf(drw0, drw1, drw2, drw3, drw4, drw5, drw6, drw7, drw8, drw9, drw10, drw11, drw12, drw13)
        return LayerDrawable(drawableArray)
    }

    private fun genDrawable2sidePll(correctAnswer: Int): LayerDrawable {
        val stringOfTopColorOfPll = pllTopLayerColor[correctAnswer]

        val f2lColorOffSet = random.nextInt(4)                      //генерируем число от 0 до 3 смещение цвета для боковых граней
        val topLayColorOffSet = random.nextInt(4)                   //генерируем число от 0 до 3 смещение цвета для 3-го этажа
        val topLayerOffset = random.nextInt(4)                      //генерируем число от 0 до 3 смещение грани для 3-го этажа

        val drw0 = ContextCompat.getDrawable(ctx, R.drawable.z_cube_back_black)
        val drw1 = ContextCompat.getDrawable(ctx, R.drawable.z_cube_up_y)
        val drw2 = ContextCompat.getDrawable(ctx, R.drawable.z_cube_left_r)
        val drw3 = ContextCompat.getDrawable(ctx, R.drawable.z_cube_right_g)

        val drw4 = ContextCompat.getDrawable(ctx, R.drawable.z_3d_up_41_y)
        val drw5 = ContextCompat.getDrawable(ctx, R.drawable.z_3d_up_42_y)
        val drw6 = ContextCompat.getDrawable(ctx, R.drawable.z_3d_up_43_y)
        val drw7 = ContextCompat.getDrawable(ctx, R.drawable.z_3d_up_44_y)
        val drw8 = ContextCompat.getDrawable(ctx, R.drawable.z_3d_up_45_y)
        val drw9 = ContextCompat.getDrawable(ctx, R.drawable.z_3d_up_46_y)

        val doubleStringOfTopColorOfPll = stringOfTopColorOfPll + stringOfTopColorOfPll

        val cube3 = Array(6,{ i -> Integer.parseInt(Character.toString(doubleStringOfTopColorOfPll[3 * topLayerOffset + i])) })

//        val cube3 = IntArray(6)
//        for (i in 0..5) {
//            val temp = doubleStringOfTopColorOfPll[3 * topLayerOffset + i]
//            cube3[i] = Integer.parseInt(Character.toString(temp))
//        }

        DrawableCompat.setTint(drw1, ContextCompat.getColor(ctx,cubeColor[4]))  //верх желтый
        DrawableCompat.setTint(drw2, ContextCompat.getColor(ctx,cubeColor4PLL[f2lColorOffSet]))
        DrawableCompat.setTint(drw3, ContextCompat.getColor(ctx,cubeColor4PLL[f2lColorOffSet + 1]))

        DrawableCompat.setTint(drw4, ContextCompat.getColor(ctx,cubeColor4PLL[cube3[0] + topLayColorOffSet]))
        DrawableCompat.setTint(drw5, ContextCompat.getColor(ctx,cubeColor4PLL[cube3[1] + topLayColorOffSet]))
        DrawableCompat.setTint(drw6, ContextCompat.getColor(ctx,cubeColor4PLL[cube3[2] + topLayColorOffSet]))
        DrawableCompat.setTint(drw7, ContextCompat.getColor(ctx,cubeColor4PLL[cube3[3] + topLayColorOffSet]))
        DrawableCompat.setTint(drw8, ContextCompat.getColor(ctx,cubeColor4PLL[cube3[4] + topLayColorOffSet]))
        DrawableCompat.setTint(drw9, ContextCompat.getColor(ctx,cubeColor4PLL[cube3[5] + topLayColorOffSet]))

        val drawableArray = arrayOf(drw0, drw1, drw2, drw3, drw4, drw5, drw6, drw7, drw8, drw9)

        return LayerDrawable(drawableArray)
    }

}
