package ru.tohaman.rg2.activities

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.GravityCompat
import android.support.v4.view.ViewPager
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.ListView
import com.google.gson.GsonBuilder
import ru.tohaman.rg2.DebugTag.TAG

import ru.tohaman.rg2.adapters.MyListAdapter
import ru.tohaman.rg2.data.ListPager
import ru.tohaman.rg2.data.ListPagerLab
import java.util.ArrayList
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_sliding.*
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onItemClick
import org.jetbrains.anko.support.v4.onPageChangeListener
import ru.tohaman.rg2.*
import ru.tohaman.rg2.data.Favorite
import ru.tohaman.rg2.fragments.FragmentPagerItem
import ru.tohaman.rg2.util.getThemeFromSharedPreference
import ru.tohaman.rg2.util.saveInt2SP
import ru.tohaman.rg2.util.saveString2SP

/*
    Активность которая отображает слайдинг таб с обучалками. Слева выезжающее меню этапов, справа Избранное
*/


class SlidingTabsActivity : MyDefaultActivity(),
        SharedPreferences.OnSharedPreferenceChangeListener,
        FragmentPagerItem.OnViewPagerInteractionListener
{

    private var curPhase = "BEGIN"
    private var curId = 0
    private var changedPhase = "BEGIN"
    private var changedId = 0
    private lateinit var rightDrawerListView: ListView
    private lateinit var mListPagerLab: ListPagerLab
    private lateinit var favList: ArrayList<ListPager>
    private lateinit var mViewPagerSlidingTabs: ViewPager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v (TAG, "SlidingTabActivity onCreate")
        setContentView(R.layout.activity_sliding_tabs)
        setSupportActionBar(toolbar)
        val sp = PreferenceManager.getDefaultSharedPreferences(ctx)
        if (!sp.getBoolean("fab_on", false)) {
            fab_sl.visibility = View.GONE
        } else {
            fab_sl.visibility = View.VISIBLE
        }
        fab_sl.setOnClickListener {
            onBackPressed()
        }

        //Инициируем фазу и номер этапа, должны быть переданы из другой активности, если нет, то используем значения по-умолчанию
        if (intent.hasExtra(RUBIC_PHASE)){
            curPhase = intent.extras.getString(RUBIC_PHASE)
        }
        if (intent.hasExtra(EXTRA_ID)){
            curId = intent.extras.getInt(EXTRA_ID)
        }

        //Регистрируем обработчик изменений в SharedPreference
        sp.registerOnSharedPreferenceChangeListener(this)

        //Чтобы при запуске активности в onResume не пришлось менять фазу
        changedPhase = curPhase
        changedId = curId

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        Log.v (TAG, "SlidingTabActivity onCreate Инициализируем ListPagers и передаем его адаптерам")
        mListPagerLab = ListPagerLab.get(this)
        val mListPagers : ArrayList<ListPager> = mListPagerLab.getPhaseList(curPhase).filter { it.url != "submenu" } as ArrayList<ListPager>

        Log.v (TAG, "SlidingTabActivity onCreate Настраиваем SlidingTab")
        mViewPagerSlidingTabs = findViewById<ViewPager>(R.id.viewPagerSlidingTabs)

        // подключим адаптер для слайдингтаба (основного текста)
        val adapter = object : FragmentStatePagerAdapter(supportFragmentManager) {

            override fun getPageTitle(position: Int): CharSequence {
                return mListPagers[position].title
            }

            override fun getCount(): Int {
                return mListPagers.size
            }

            override fun getItem(position: Int): Fragment {
                return FragmentPagerItem.newInstance(mListPagers[position])
            }

        }
        mViewPagerSlidingTabs.adapter = adapter
        mViewPagerSlidingTabs.currentItem = curId
        tabs.setViewPager(mViewPagerSlidingTabs)

        Log.v (TAG, "SlidingTabActivity onCreate Настраиваем ListView для списка слева ")
        // Настраиваем листвью для выезжающего слева списка
        val listAdapter = MyListAdapter(mListPagers)
        val drawerListView  = findViewById<ListView>(R.id.left_drawer)

        // подключим адаптер для выезжающего слева списка
        drawerListView.adapter = listAdapter
        drawerListView.setOnItemClickListener { _, _, position, _ ->
            mViewPagerSlidingTabs.currentItem = position
            drawer_layout.closeDrawer(GravityCompat.START)
        }

        favList = mListPagerLab.getPhaseList("FAVORITES")
        val rightDrawerAdapter = MyListAdapter(favList,1.3F)

        // подключим адаптер для выезжающего справа списка
        rightDrawerListView  = findViewById(R.id.right_drawer)
        rightDrawerListView.adapter = rightDrawerAdapter
        rightDrawerListView.setOnItemClickListener { _, _, i, _ ->
            //Меняем фазу для выхода в основную активность
            //Если фаза не меняется, то выходить не надо, просто открываем другую вкладку
            val changedId = favList[i].id
            val changedPhase = favList[i].url
            if (curPhase != changedPhase) {
                //вот такой "коллбэк", в основной активности обработчик onSharedPreferenceChanged
                //обработает данные изменения и при возврате в onResume запустит что надо
                saveInt2SP(changedId, "startId", ctx)
                saveString2SP(changedPhase, "startPhase", ctx)
                onBackPressed()
            } else {
                mViewPagerSlidingTabs.currentItem = changedId
            }
            drawer_layout.closeDrawer(GravityCompat.END)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.sliding_tab_menu, menu)
        return true
    }

    override fun onViewPagerCheckBoxInteraction() {
        favList = mListPagerLab.getPhaseList("FAVORITES")
        val adapter = MyListAdapter(favList,1.3F)
        rightDrawerListView.adapter = adapter
    }

    override fun onPause() {
        super.onPause()
        Log.v (TAG, "onPause - curPhase - $curPhase")
        changedPhase = curPhase
        changedId = curId
    }

    override fun onResume() {
        super.onResume()
        if (changedPhase != curPhase) {
            //Если за время между он Pause и onResume была сменена фаза, т.е. вызывалась ShowPagerActivity
            Log.v(DebugTag.TAG, "Phase changed from $curPhase to $changedPhase")
            finish()
        } else {
            if (changedId != curId) {
                mViewPagerSlidingTabs.currentItem = changedId
            }
        }
    }

    // Слушаем изменения в настройках программы
    override fun onSharedPreferenceChanged(sp: SharedPreferences, key: String?) {
        //Если поменяли фазу, значит меняем значение changedPhase
        when (key) {
            "startPhase" -> {
                val phase = sp.getString(key, "ROZOV")
                if (curPhase !=  phase) { changedPhase = phase }
                val id = sp.getInt("startId", 0)
                if (curId != id) { changedId = id}
            }
        }

    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //Если нажали на ? в правом верхнем углу, то вызываем АлертДиалог
        //со списком основных движений для соответствующей головоломки
        Log.v (TAG, "onOptionsItemSelected")
        when (item.itemId) {
            R.id.basic_move_help -> {
                alert {
                    customView {
                        positiveButton("Закрыть окно") {
                        }
                        verticalLayout {
                            val listPagers : ArrayList<ListPager> =
                                    //если не одна из перечисленных головоломок, то вызываем движения для кубика 3х3
                                    when (curPhase) {
                                        "PYRAMINX" -> {ListPagerLab.get(ctx).getPhaseList("BASIC_PYR")}
                                        "SKEWB" -> {ListPagerLab.get(ctx).getPhaseList("BASIC_SKEWB")}
                                        "BEGIN4X4" -> {ListPagerLab.get(ctx).getPhaseList("BASIC4X4")}
                                        "BEGIN5X5" -> {ListPagerLab.get(ctx).getPhaseList("BASIC5X5")}
                                        "SQUARE" -> {ListPagerLab.get(ctx).getPhaseList("BASIC_SQ1")}
                                        "SQ_STAR" -> {ListPagerLab.get(ctx).getPhaseList("BASIC_SQ1")}
                                        "IVY" -> {ListPagerLab.get(ctx).getPhaseList("BASIC_IVY")}
                                        else -> {ListPagerLab.get(ctx).getPhaseList("BASIC3X3")}
                                    }
                            val lstView = listView {
                                adapter = MyListAdapter(listPagers)
                            }
                            lstView.onItemClick { _, _, p2, _ ->
                                toast(listPagers[p2].description)
                            }
                        }
                    }
                }.show()
                return true
            }
            R.id.sliding_tab_favorite -> {
                drawer_layout.openDrawer(GravityCompat.END)
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }


}
