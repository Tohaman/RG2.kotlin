package ru.tohaman.rg2.ui

import android.graphics.drawable.Drawable
import android.os.Build
import android.text.Html
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.LinearLayout
import org.jetbrains.anko.*
import ru.tohaman.rg2.DebugTag.TAG
import kotlinx.android.synthetic.main.fragment_azbuka.view.*
import org.jetbrains.anko.sdk15.coroutines.onClick
import ru.tohaman.rg2.*
import ru.tohaman.rg2.adapters.MyGridAdapter
import ru.tohaman.rg2.data.ListPagerLab
import ru.tohaman.rg2.util.*


/**
 * Created by Test on 15.12.2017. Интерфейс (UI) выбора Азбуки
 * в этом фрагменте UI обходимся без findViewById, импортируем xml лэйаут
 * через [include<View>(R.layout.fragment_azbuka)] и далее создаем обработчики
 * кнопок, адаптер к GridView и т.п.
 */
class AzbukaSelectUI<in Fragment> : AnkoComponentEx<Fragment>()  {
    private lateinit var gridAdapter : MyGridAdapter

    override fun create(ui: AnkoContext<Fragment>): View = with(ui) {
        Log.v(TAG, "AzbukaSelectUI create start with ScreenSize = ")

        val imgGetter  = Html.ImageGetter { _ ->
            val drawable: Drawable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                resources.getDrawable (R.drawable.ic_warning, null)
            } else {
                @Suppress("DEPRECATION")
                resources.getDrawable (R.drawable.ic_warning)
            }
            drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
            drawable
        }

        val listPagerLab = ListPagerLab.get(ctx)

        linearLayout {
            linearLayout {
                include<View>(R.layout.fragment_azbuka) {
//                    backgroundColorResource = R.color.blue
                }.lparams(matchParent, matchParent)

                val gridList = prepareAzbukaToShowInGridView(listPagerLab.getCurrentAzbuka())
                gridAdapter = MyGridAdapter(ctx, gridList)
                azbuka_gridView.adapter = gridAdapter

                button_max_azbuka.onClick {
                    val azbuka = listPagerLab.getMaximAzbuka()
                    gridAdapter.gridList = prepareAzbukaToShowInGridView(azbuka)
                    listPagerLab.updateCurrentAzbuka(azbuka)
                    gridAdapter.notifyDataSetChanged()
                }

                button_my_azbuka.onClick {
                    val azbuka = listPagerLab.getMyAzbuka()
                    gridAdapter.gridList = prepareAzbukaToShowInGridView(azbuka)
                    listPagerLab.updateCurrentAzbuka(azbuka)
                    gridAdapter.notifyDataSetChanged()
                }

                button_save_azbuka.onClick {
                    val azbuka = listPagerLab.getCurrentAzbuka()
                    listPagerLab.saveCustomAzbuka(azbuka)

                }

                button_load_azbuka.onClick {
                    val azbuka = listPagerLab.getCustomAzbuka()
                    gridAdapter.gridList = prepareAzbukaToShowInGridView(azbuka)
                    listPagerLab.updateCurrentAzbuka(azbuka)
                    gridAdapter.notifyDataSetChanged()
                }

                azbuka_gridView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                    var letter = gridAdapter.getItem(position)
                    if (!((letter == "") or (letter == "-"))) {
                        Log.v (TAG, "Letter select dialog")
                        alert {
                            customView {
                                verticalLayout {
                                    textView{
                                        text = "Выберите букву:"
                                        textSize = 24F
                                    }.lparams() {margin =  16.dp}
                                    linearLayout {
                                        gravity = Gravity.CENTER
                                        orientation = LinearLayout.HORIZONTAL
                                        val buttonMinus = button ( "-")
                                        val letterText = textView {
                                            text = letter
                                            textSize = 18F
                                        }.lparams {margin = 16.dp}
                                        val buttonPlus = button ("+")

                                        buttonMinus.onClick {
                                            var ch = letter[0]
                                            ch--
                                            when {
                                                (ch == 'Е') -> { ch = 'Ё' }
                                                (ch < 'Ё') -> { ch = 'Е' }
                                                (ch < 'А') and (ch != 'Ё') -> { ch = 'Я' }

                                            }
                                            letter = ch.toString()
                                            letterText.text = letter
                                        }

                                        buttonPlus.onClick {
                                            var ch = letter[0]
                                            ch++
                                            when {
                                                (ch > 'Я') -> { ch = 'А' }
                                                (ch == 'Ж') -> { ch = 'Ё' }
                                                //между Ё и А, т.к. Ё в таблице символов идет раньше А, а не после Е
                                                (ch in 'Ђ'..'Џ') -> { ch = 'Ж' }
                                            }
                                            letter = ch.toString()
                                            letterText.text = letter
                                        }
                                    }.lparams(matchParent, wrapContent) {margin = 8.dp}
                                    positiveButton("OK") {
                                        gridAdapter.gridList[position].letter = letter
                                        val azbuka = getAzbukaFromAdapter(gridAdapter)
                                        listPagerLab.updateCurrentAzbuka(azbuka)
                                        gridAdapter.notifyDataSetChanged()
                                    }
                                    negativeButton("Отмена") {}
                                }
                            }
                        }.show()
                    }
                }


                azbuka_textView.text = spannedString(resources.getString(R.string.azbuka2), imgGetter)
            }.lparams(matchParent, matchParent)
        }

    }

    private fun getAzbukaFromString(st: String) = st.split(" ") as ArrayList<String>

    private fun getAzbukaFromAdapter(gridAdapter: MyGridAdapter) : Array<String> {
        val azbuka = Array(54, { "" })
        for (i in 0..8) {
            azbuka[i] = gridAdapter.getItem(i / 3 * 12 + 3 + i % 3)
            azbuka[i + 9] = gridAdapter.getItem((i / 3 + 3) * 12 + i % 3)
            azbuka[i + 18] = gridAdapter.getItem((i / 3 + 3) * 12 + 3 + i % 3)
            azbuka[i + 27] = gridAdapter.getItem((i / 3 + 3) * 12 + 6 + i % 3)
            azbuka[i + 36] = gridAdapter.getItem((i / 3 + 3) * 12 + 9 + i % 3)
            azbuka[i + 45] = gridAdapter.getItem((i / 3 + 6) * 12 + 3 + i % 3)
        }
        return azbuka
    }

}
