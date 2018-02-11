package ru.tohaman.rg2.activities

import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.nav_header_main.*
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.ctx
import org.jetbrains.anko.find
import ru.tohaman.rg2.MyDefaultActivity
import ru.tohaman.rg2.R
import ru.tohaman.rg2.data.ListPager
import java.util.*

class BlindGameActivity : MyDefaultActivity() {
    private val random = Random()
    private lateinit var listPagers : List<ListPager>
    private lateinit var guessLinearLayouts : Array<LinearLayout>
    private var guessRows = 2
    private lateinit var imgView: ImageView


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

        imgView = findViewById(R.id.test_image)
        imgView.setImageResource(R.drawable.z_2s_complete)
    }
}
