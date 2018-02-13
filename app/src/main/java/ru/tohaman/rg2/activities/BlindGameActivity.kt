package ru.tohaman.rg2.activities

import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.widget.ImageView
import android.widget.LinearLayout
import org.jetbrains.anko.ctx
import org.jetbrains.anko.find
import org.jetbrains.anko.image
import ru.tohaman.rg2.MyDefaultActivity
import ru.tohaman.rg2.R
import ru.tohaman.rg2.data.ListPager
import java.util.*
import android.support.v4.graphics.drawable.DrawableCompat
import android.util.Log
import android.view.View
import android.widget.Button
import ru.tohaman.rg2.DebugTag
import ru.tohaman.rg2.data.ListPagerLab
import ru.tohaman.rg2.util.*


class BlindGameActivity : MyDefaultActivity() {
    private val random = Random()
    private lateinit var listPagers : List<ListPager>
    private lateinit var guessLinearLayouts : Array<LinearLayout>
    private val azbukaRnd = mutableSetOf<String>()
    private lateinit var imgView: ImageView
    val DEFAULT_DRAWABLE_SIZE = 1
    private var correctAnswer = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.v (DebugTag.TAG, "BlindGameActivity onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pll_test_game)
        actionBar?.setDisplayHomeAsUpEnabled(true)

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
        val guessRows = 2
        updateGuessRows(guessRows, guessLinearLayouts)

        val listPagerLab = ListPagerLab.get(ctx)
        val azbuka = listPagerLab.getCurrentAzbuka()
        Log.v (DebugTag.TAG, "BlindGameActivity onCreate azbuka = $azbuka")
        azbukaRnd.clear()
        //берем из азбуки только уникальные значения
        azbukaRnd += azbuka
        azbukaRnd.remove("-")

        //сгенерируем скрамбл длинны указанной в поле ScrambleLength
        var scramble = generateScramble(14)
        //TODO развернуть кубик случайным образом, пока белозеленокрасной стороной к себе
        scramble += " y y"
        //разбираем кубик по скрамблу
        val scrambledCube = runScramble(resetCube(), scramble)

        //выбираем случайный слот и смотрим, какой там элемент (буква)
        val slot = random.nextInt(0..6)
        val colorOfElement = getColorOfElement(scrambledCube, slotElementNumbers[slot]!!.first, slotElementNumbers[slot]!!.second)
        val letter = azbuka[mainEdge[colorOfElement]!!]
        Log.v (DebugTag.TAG, "BlindGameActivity onCreate letter = $letter")

        imgView = findViewById(R.id.test_image)
        imgView.image = maskedDrawable(scramble, slot)

    }

    fun loadNextBlind (guessRows: Int){
        val listPagerLab = ListPagerLab.get(ctx)
        val azbuka = listPagerLab.getCurrentAzbuka()
        //сгенерируем скрамбл длинны указанной в поле ScrambleLength
        var scramble = generateScramble(14)
        //TODO развернуть кубик случайным образом, пока белозеленокрасной стороной к себе
        scramble += " y y"
        //разбираем кубик по скрамблу
        val scrambledCube = runScramble(resetCube(), scramble)

        //выбираем случайный слот и смотрим, какой там элемент (буква)
        val slot = random.nextInt(0..6)
        val colorOfElement = getColorOfElement(scrambledCube, slotElementNumbers[slot]!!.first, slotElementNumbers[slot]!!.second)
        val letter = azbuka[mainEdge[colorOfElement]!!]
        Log.v (DebugTag.TAG, "BlindGameActivity onCreate letter = $letter")

        if (letter in azbukaRnd) {

        }

    }

    // возвращает внешний вид кубика разобранного по скрамблу (в виде 28-ми слойного Drawable)
    private fun getCompleteDrawable (scramble:String):LayerDrawable {
        Log.v (DebugTag.TAG, "BlindGameActivity getCompleteDrawable")
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

    private fun updateGuessRows(guessRows: Int, guessLinearLayouts: Array<LinearLayout>) {
        Log.v (DebugTag.TAG, "BlindGameActivity updateGuessRows")
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
            //loadNextPLL(guessRows)
        } else {    //неправильный ответ
            guessButton.isEnabled = false
        }
    }


    private fun maskedDrawable(scramble: String, slot: Int): LayerDrawable {
        //ширина картинки 200dp
        val width = 200
        //var drw1 = ContextCompat.getDrawable(ctx, R.drawable.z_2s_complete)
        var drw1 = getCompleteDrawable(scramble)
        //val scaledBitmap:Bitmap = BitmapFactory.decodeResource(this.resources, R.drawable.wait)
        val scaledBitmap = getBitmapFromDrawable(drw1, width)
        val targetBitmap = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(targetBitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        //заливаем канву (фон) полупрозрачным
        canvas.drawARGB(100, 0, 0, 0) //прозрачность 100 (0 - в итоге фон будет непрозрачный, 255 - полностью прозрачный)

        //получаем координаты и радиус круга подсветки для слота из массива констант ArraysForScramble.kt
        val tripleCords = slotLightingCoordinate[slot]
        //рисуем круг подсветки
        canvas.drawCircle(width*tripleCords!!.first.toFloat(),
                        width*tripleCords.second.toFloat(),
                    width*tripleCords.third.toFloat(),
                    paint)

        //накладываем маску из цвета фона (полупрозрачного) и непрозрачного кружка
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        val rect = Rect(0, 0, scaledBitmap.width, scaledBitmap.height)
        canvas.drawBitmap(scaledBitmap, rect, rect, paint)

        val bitmapDrawable = BitmapDrawable(resources, targetBitmap)

        //val drawableArray =  arrayOf (drw1, BitmapDrawable(resources, targetBitmap))
        val drawableArray =  arrayOf (bitmapDrawable)
        return LayerDrawable (drawableArray)
    }

    /**
     * Возвращает Bitmap из Drawable и масштабирует до expectedSize
     * Extract the Bitmap from a Drawable and resize it to the expectedSize conserving the ratio.
     *
     * @param drawable   Drawable used to extract the Bitmap. Can't be null.
     * @param expectSize Expected size for the Bitmap. Use @link #DEFAULT_DRAWABLE_SIZE to
     * keep the original [Drawable] size.
     * @return The Bitmap associated to the Drawable or null if the drawable was null.
     * @see <html>[Stackoverflow answer](https://stackoverflow.com/a/10600736/1827254)</html>
     */

    private fun getBitmapFromDrawable(drawable: Drawable, expectSize: Int): Bitmap {
        val bitmap: Bitmap

        if (drawable is BitmapDrawable) {
            val bitmapDrawable = drawable as BitmapDrawable?
            if (bitmapDrawable!!.bitmap != null) {
                return bitmapDrawable.bitmap
            }
        }

        bitmap = if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
            Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888) // Single color bitmap will be created of 1x1 pixel
        } else {
            val ratio = if (expectSize != DEFAULT_DRAWABLE_SIZE)
                calculateRatio(drawable.intrinsicWidth, drawable.intrinsicHeight, expectSize)
            else
                1f

            val width = (drawable.intrinsicWidth * ratio).toInt()
            val height = (drawable.intrinsicHeight * ratio).toInt()

            Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        }

        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        return bitmap
    }

    /**
     * Calculate the ratio to multiply the Bitmap size with, for it to be the maximum size of
     * "expected".
     *
     * @param height   Original Bitmap height
     * @param width    Original Bitmap width
     * @param expected Expected maximum size.
     * @return If height and with equals 0, 1 is return. Otherwise the ratio is returned.
     * The ration is base on the greatest side so the image will always be the maximum size.
     */
    private fun calculateRatio(height: Int, width: Int, expected: Int): Float {
        if (height == 0 && width == 0) {
            return 1f
        }
        return if (height > width)
            expected / width.toFloat()
        else
            expected / height.toFloat()
    }
}
