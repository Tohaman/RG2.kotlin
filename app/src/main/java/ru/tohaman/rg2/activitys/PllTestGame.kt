package ru.tohaman.rg2.activitys

import android.os.Bundle
import android.app.Activity
import android.preference.PreferenceManager
import android.widget.ImageView
import ru.tohaman.rg2.R

import kotlinx.android.synthetic.main.activity_pll_test_game.*
import org.jetbrains.anko.ctx
import ru.tohaman.rg2.PLL_TEST_ROW_COUNT

class PllTestGame : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pll_test_game)
        actionBar?.setDisplayHomeAsUpEnabled(true)
        val sp = PreferenceManager.getDefaultSharedPreferences(ctx)

        val imgView = findViewById<ImageView>(R.id.test_image)
        val rows = sp.getInt(PLL_TEST_ROW_COUNT, 6)

    }

}
