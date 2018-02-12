package ru.tohaman.rg2.activities

import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.widget.ImageView
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.nav_header_main.*
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.ctx
import org.jetbrains.anko.find
import org.jetbrains.anko.image
import ru.tohaman.rg2.MyDefaultActivity
import ru.tohaman.rg2.R
import ru.tohaman.rg2.data.ListPager
import java.util.*
import android.graphics.Color
import android.graphics.drawable.shapes.OvalShape
import android.graphics.drawable.ShapeDrawable
import android.view.View
import android.widget.Button
import ru.tohaman.rg2.util.getNameFromListPagers


class BlindGameActivity : MyDefaultActivity() {
    private val random = Random()
    private lateinit var listPagers : List<ListPager>
    private lateinit var guessLinearLayouts : Array<LinearLayout>
    private var guessRows = 2
    private lateinit var imgView: ImageView
    val DEFAULT_DRAWABLE_SIZE = 1
    private var correctAnswer = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pll_test_game)
        actionBar?.setDisplayHomeAsUpEnabled(true)

        guessRows = 2

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

        updateGuessRows(guessRows, guessLinearLayouts)

        imgView = findViewById(R.id.test_image)
        imgView.image = maskedDrawable(200)

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
            //loadNextPLL(guessRows)
        } else {    //неправильный ответ
            guessButton.isEnabled = false
        }
    }


    private fun maskedDrawable(width:Int): LayerDrawable? {
        var drw1 = ContextCompat.getDrawable(ctx, R.drawable.z_2s_complete)
        //val scaledBitmap:Bitmap = BitmapFactory.decodeResource(this.resources, R.drawable.wait)
        val scaledBitmap = getBitmapFromDrawable(drw1!!, width)
        val targetBitmap = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(targetBitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        //заливаем канву полупрозрачным
        canvas.drawARGB(100, 0, 0, 0) //прозрачность 100 (0 - непрозрачный, 255 - полностью прозрачный)

        //правый угол (координаты x - 0.57, y - 0.42, radius - 0.16)
        //canvas.drawCircle(width*0.57.toFloat(), width*0.42.toFloat(), width*0.16.toFloat(), paint)
        //передний угол
        //canvas.drawCircle(width*0.43.toFloat(), width*0.41.toFloat(), width*0.16.toFloat(), paint)
        //верхний угол
        //canvas.drawCircle(width*0.5.toFloat(), width*0.32.toFloat(), width*0.15.toFloat(), paint)

        //верхнее правое ребро
        //canvas.drawCircle(width*0.67.toFloat(), width*0.25.toFloat(), width*0.13.toFloat(), paint)
        //нижнее правое ребро
        //canvas.drawCircle(width*0.74.toFloat(), width*0.34.toFloat(), width*0.14.toFloat(), paint)

        //верхнее левое ребро
        canvas.drawCircle(width*0.3.toFloat(), width*0.25.toFloat(), width*0.13.toFloat(), paint)
        //нижнее левое ребро
        //canvas.drawCircle(width*0.25.toFloat(), width*0.32.toFloat(), width*0.14.toFloat(), paint)

        //накладываем маску из цвета фона (полупрозрачного) и непрозрачного кружка
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        val rect = Rect(0, 0, scaledBitmap.width, scaledBitmap.height)
        canvas.drawBitmap(scaledBitmap, rect, rect, paint)

        val bitmapDrawable = BitmapDrawable(resources, targetBitmap)

        //val drawableArray =  arrayOf (drw1, BitmapDrawable(resources, targetBitmap))
        val drawableArray =  arrayOf (bitmapDrawable)
        return LayerDrawable(drawableArray)
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
