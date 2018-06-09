package ru.tohaman.rg2.data

import android.content.Context
import android.support.v7.preference.PreferenceManager
import ru.tohaman.rg2.R
import com.google.gson.GsonBuilder
import ru.tohaman.rg2.FAVORITES
import ru.tohaman.rg2.util.saveString2SP
import com.google.gson.reflect.TypeToken
import java.util.*
import kotlin.collections.ArrayList


/**
 * Created by Toha on 21.05.2017. Синглетный класс, при первом вызове создающий сиглет, хранящмй всю информацию о всех фазах
 * в вие коллекции объектов типа ListPager, данные считывает из SQLite базы, где хранятся комменты к каждому этапу фазы.
 * если в базе нет записи, она создается со значениями по-умолчанию (0,"").
 */

class ListPagerLab private constructor(context: Context){
    private val mDatabase = BaseHelper(context)
    private var listPagers = arrayListOf<ListPager>()
    var favorites = mutableSetOf<Favorite>()

    init { // тут пишем то, что выполнится при инициализации синглета
        phaseInit("BEGIN2X2",R.array.begin2x2_title,R.array.begin2x2_icon,R.array.begin2x2_descr,R.array.begin2x2_url,context)
        phaseInit("ADV2X2",R.array.adv2x2_title,R.array.adv2x2_icon,R.array.adv2x2_descr,R.array.adv2x2_url,context)
        phaseInit("G2F", R.array.g2f_title,R.array.g2f_icon,R.array.g2f_descr,R.array.g2f_url,context)
        phaseInit("MAIN2X2", R.array.main2x2_title,R.array.main2x2_icon,R.array.main2x2_descr,R.array.main2x2_url,context)
        phaseInit("MAIN3X3", R.array.main3x3_title,R.array.main3x3_icon,R.array.main3x3_descr,R.array.main3x3_url,context)
        phaseInit("OTHER", R.array.other_title,R.array.other_icon,R.array.other_descr,R.array.other_url,context)
        phaseInit("BEGIN",R.array.begin_title,R.array.begin_icon,R.array.begin_descr,R.array.begin_url,context)
        phaseInit("RECOMEND",R.array.recomend_title,R.array.recomend_icon,R.array.recomend_descr,R.array.recomend_url,context)
        phaseInit("BASIC",R.array.basic_title,R.array.basic_icon,R.array.basic_descr,R.array.basic_null,context)
        phaseInit("BASIC3X3",R.array.basic_3x3_title,R.array.basic_3x3_icon,R.array.basic_3x3_descr,R.array.basic_3x3_url,context)
        phaseInit("BASIC4X4",R.array.basic_4x4_title,R.array.basic_4x4_icon,R.array.basic_4x4_descr,R.array.basic_4x4_url,context)
        phaseInit("BASIC5X5",R.array.basic_5x5_title,R.array.basic_5x5_icon,R.array.basic_5x5_descr,R.array.basic_5x5_url,context)
        phaseInit("BASIC_PYR",R.array.basic_pyr_title,R.array.basic_pyr_icon,R.array.basic_pyr_descr,R.array.basic_pyr_url,context)
        phaseInit("BASIC_SKEWB",R.array.basic_skewb_title,R.array.basic_skewb_icon,R.array.basic_skewb_descr,R.array.basic_skewb_url,context)
        phaseInit("BASIC_SQ1",R.array.basic_square_title,R.array.basic_square_icon,R.array.basic_square_descr,R.array.basic_square_url,context)
        phaseInit("ACCEL",R.array.accel_title,R.array.accel_icon,R.array.accel_descr,R.array.accel_url,context)
        phaseInit("CROSS",R.array.cross_title,R.array.cross_icon,R.array.cross_descr,R.array.cross_url,context)
        phaseInit("F2L",R.array.f2l_title,R.array.f2l_icon,R.array.f2l_descr,R.array.f2l_url,context)
        phaseInit("ADVF2L",R.array.advf2l_title,R.array.advf2l_icon,R.array.advf2l_descr,R.array.advf2l_url,context)
        phaseInit("OLL",R.array.oll_title,R.array.oll_icon,R.array.oll_descr,R.array.oll_url,context)
        phaseInit("PLL",R.array.pll_title,R.array.pll_icon,R.array.pll_descr,R.array.pll_url,context)
        phaseInit("COLL",R.array.coll_title,R.array.coll_icon,R.array.coll_descr,R.array.coll_url,context,R.array.coll_comment)
        phaseInit("ROUX",R.array.roux_title,R.array.roux_icon,R.array.roux_descr,R.array.roux_url,context)
        phaseInit("ORTEGA",R.array.ortega_title,R.array.ortega_icon,R.array.ortega_descr,R.array.ortega_url,context)
        phaseInit("BEGIN4X4",R.array.begin4_title,R.array.begin4_icon,R.array.begin4_descr,R.array.begin4_url,context)
        phaseInit("BEGIN5X5",R.array.begin5_title,R.array.begin5_icon,R.array.begin5_descr,R.array.begin5_url,context)
        phaseInit("PATTERNS",R.array.patterns_title,R.array.patterns_icon,R.array.patterns_descr,R.array.patterns_url,context, R.array.patterns_comment)
        phaseInit("AZBUKA", R.array.azbuka_title, R.array.azbuka_icon,R.array.azbuka_descr,R.array.azbuka_url,context)
        phaseInit("BLIND", R.array.blind_title, R.array.blind_icon,R.array.blind_descr,R.array.blind_url,context)
        phaseInit("BLINDACC", R.array.blindacc_title, R.array.blindacc_icon,R.array.blindacc_descr,R.array.blindacc_url,context)
        phaseInit("PYRAMINX", R.array.pyraminx_title, R.array.pyraminx_icon,R.array.pyraminx_descr,R.array.pyraminx_url,context)
        phaseInit("MEGAMINX", R.array.megaminx_title, R.array.megaminx_icon,R.array.megaminx_descr,R.array.megaminx_url,context)
        phaseInit("SKEWB", R.array.skewb_title, R.array.skewb_icon,R.array.skewb_descr,R.array.skewb_url,context)
        phaseInit("SQUARE", R.array.square_title, R.array.square_icon,R.array.square_descr,R.array.square_url,context)
        phaseInit("MIRROR", R.array.mirror_title, R.array.mirror_icon,R.array.mirror_descr,R.array.mirror_url,context)
        phaseInit("AXIS", R.array.axis_title, R.array.axis_icon,R.array.axis_descr,R.array.axis_url,context)
        phaseInit("PYRAMORPHIX", R.array.pyramorphix_title, R.array.pyramorphix_icon,R.array.pyramorphix_descr,R.array.pyramorphix_url,context)
        phaseInit("SUDOKU", R.array.sudoku_title, R.array.sudoku_icon,R.array.sudoku_descr,R.array.sudoku_url,context)
        phaseInit("WINDMILL", R.array.windmill_title, R.array.windmill_icon,R.array.windmill_descr,R.array.windmill_url,context)
        phaseInit("PLLTEST", R.array.pll_test_phases, R.array.pll_test_icon,R.array.pll_test_descr,R.array.pll_test_url,context)
        phaseInit("PLLTEST_CUSTOM", R.array.pll_test_phases, R.array.pll_test_icon,R.array.pll_test_descr,R.array.pll_test_url,context)
        phaseInit("OLLTEST", R.array.oll_test_phases, R.array.oll_icon, R.array.oll_descr, R.array.oll_test_url, context)
        phaseInit("OLLTEST_CUSTOM", R.array.oll_test_phases, R.array.oll_icon,R.array.oll_descr,R.array.oll_test_url,context)
        phaseInit("THANKS", R.array.thanks_title, R.array.thanks_icon,R.array.thanks_descr,R.array.thanks_url,context)
        favoritesInit(context)
    }

    //собственно сам синглет, точнее Холдер, который держит сиглетную ссылку (INSTANCE) на экземпляр класса
    companion object Holder {
        private var instance : ListPagerLab? = null
        fun get(context: Context) : ListPagerLab {
            if (instance == null) {
                instance = ListPagerLab(context) //Если null, т.е. не иницилизирован, то запускаем конструктор
            }
            return instance!!   // !! - соглашаемся что теоретически может вернуть null, ведь if как
                                // раз и проверяет, чтобы это был не null
        }
    }

    //--------функции класса ----------------

    // Инициализация фазы, с заданными массивами Заголовков, Иконок, Описаний, ютуб-ссылок
    private fun phaseInit(phase: String, titleArray: Int, iconArray: Int, descrArray: Int, urlArray: Int, context: Context, comment : Int = 0) {
        val titles =  context.resources.getStringArray(titleArray)
        val icon = context.resources.obtainTypedArray (iconArray)
        val descr = context.resources.obtainTypedArray (descrArray)
        val url = context.resources.getStringArray(urlArray)
        val cmnt = if (comment != 0) { context.resources.getStringArray(comment) } else { context.resources.getStringArray(R.array.empty_comment)}
        for (i in titles.indices) {
            var listPager = mDatabase.getListPagerFromBase(i, phase)
            if (listPager == null) {
                listPager = ListPager(phase, i, titles[i], icon.getResourceId(i, 0), descr.getResourceId(i, 0), url[i], cmnt[i])
                mDatabase.addListPager2Base(listPager)
            } else {
                listPager.title = titles[i]
                listPager.icon= icon.getResourceId(i,0)
                listPager.description = descr.getResourceId(i,0)
                listPager.url = url[i]
            }
            listPagers.add(listPager)
            if ((comment != 0) and (listPager.comment == "") and (cmnt[i] != "")) {
                listPager.comment = cmnt[i]
                updateListPager(listPager)
            }
        }
        icon.recycle()
        descr.recycle()
    }

    private fun favoritesInit(context: Context) {
        val setOfFavorite = getFavoriteListFromSharedPref(context)
        for (i in setOfFavorite.indices) {
            val lp = makeListPagerFromFavorite(setOfFavorite.elementAt(i))
            listPagers.add(lp)
        }
    }

    private fun makeListPagerFromFavorite(favorite:Favorite): ListPager {
        val lp = getPhaseItem(favorite.id, favorite.phase).copy()
        lp.comment = favorite.comment
        lp.url = lp.phase
        lp.phase = "FAVORITES"
        return lp
    }

    private fun getFavoriteListFromSharedPref(context: Context) : MutableSet<Favorite> {
        //var defaultString = """[{"comment":"С этого стоит начать","id":0,"phase":"BEGIN"},{"comment":"Тут описаны пиф-паф, дяди и тети","id":3,"phase":"BEGIN"},{"comment":"Ускоряем сборку кубика","id":1,"phase":"ACCEL"},{"comment":"Очень полезный алгоритм","id":2,"phase":"BLIND"}]"""
        val defaultString = context.getString(R.string.def_favorites)
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        val json = sp.getString(FAVORITES, defaultString)
        val gson = GsonBuilder().create()
        val itemsListType = object : TypeToken<MutableSet<Favorite>>() {}.type
        favorites = gson.fromJson(json, itemsListType)
//        favorites = gson.fromJson(defaultString, itemsListType)

        return favorites
    }

    private fun setFavoriteListToSharedPref(context: Context) {
        val gson = GsonBuilder().create()
        val json = gson.toJson(favorites)
        saveString2SP(json, FAVORITES, context)
    }

    fun addFavorite(favorite: Favorite, context: Context) {
        favorites.add(favorite)
        val lp = makeListPagerFromFavorite(favorite)
        listPagers.add(lp)
        setFavoriteListToSharedPref(context)
    }

    fun removeFavorite(phase: String, id: Int, context: Context) {
        val foundFav = favorites.first {
            (it.phase == phase) and  (it.id == id)
        }
        favorites.remove(foundFav)
        val lp = makeListPagerFromFavorite(foundFav)
        listPagers.remove(lp)
        setFavoriteListToSharedPref(context)
    }

    //возвращает из ListPagerLab список ListPager'ов с заданной фазой (все записи для данной фазы)
    fun getPhaseList(phase: String): ArrayList<ListPager> {
        return listPagers.filterTo(ArrayList()) { phase == it.phase }
    }

    //возвращает из ListPagerLab один ListPager с заданными фазой и номером
    fun getPhaseItem(id: Int, phase: String): ListPager {
        var listPager = ListPager("", 0, "", 0)

        listPagers
                .filter { (phase == it.phase) and (id == it.id) }
                .forEach { listPager = it }
        return listPager
    }

    fun getBackPhase(phase: String, ctx : Context): String {
        var listPager = ListPager("", 0, "", 0)
        val submenuLP = listPagers
                .filter {("submenu" == it.url)}
        submenuLP
                .filter { (ctx.getString(it.description) == phase) }
                .forEach { listPager = it }

        return listPager.phase
    }

    fun getSubmenu (ctx: Context): ArrayList<String> {
        var stringArray: ArrayList<String> = arrayListOf()
        val submenuLP = listPagers
                .filter {("submenu" == it.url)}
                .forEach {
                    stringArray.add(ctx.getString(it.description))
                }

        return stringArray
    }

    //возвращает из ListPagerLab один ListPager с заданными фазой и title
    fun getPhaseItemByTitle(phase: String, title: String): ListPager {
        var pagerList = ListPager("", 0, "", 0)

        listPagers
                .asSequence()
                .filter { (phase == it.phase) and (title == it.title) }
                .forEach { pagerList = it }
        return pagerList
    }


    fun getMaximAzbuka(): Array<String> =  arrayOf(
            "М","Л","Л",
            "М","-","К",
            "И","И","К",

            "С","С","О",
            "Р","-","О",
            "Р","П","П",

            "А","А","Б",
            "Г","-","Б",
            "Г","В","В",

            "У","Ц","Ц",
            "У","-","Х",
            "Ф","Ф","Х",

            "Э","Ш","Ш",
            "Э","-","Я",
            "Ю","Ю","Я",

            "Е","Е","Ё",
            "З","-","Ё",
            "З","Ж","Ж"
    )

    fun getMyAzbuka() = arrayOf(
            "М","Л","Л",
            "М","-","К",
            "И","И","К",

            "Р","Р","Н",
            "П","-","Н",
            "П","О","О",

            "А","А","Б",
            "Г","-","Б",
            "Г","В","В",

            "С","Ф","Ф",
            "С","-","У",
            "Т","Т","У",

            "Ц","Х","Х",
            "Ц","-","Ш",
            "Ч","Ч","Ш",

            "Д","Д","Е",
            "З","-","Е",
            "З","Ж","Ж"
    )

    fun getCurrentAzbuka(): Array<String> {
        return getAzbuka(0)
    }

    fun getCustomAzbuka():Array<String> {
        return getAzbuka(1)
    }

    fun updateCurrentAzbuka(azbuka: Array<String>) {
        val listPager = ListPager("AZBUKA",0, comment =  azbuka.joinToString (" ","", ""))
        updateListPager(listPager)
    }

    fun saveCustomAzbuka(azbuka: Array<String>) {
        val listPager = ListPager("AZBUKA",1, comment = azbuka.joinToString (" ","", ""))
        updateListPager(listPager)
    }

    private fun getAzbuka(id:Int) :Array<String> {
        val azbuka : Array<String>
        val mListPager = getPhaseItem(id, "AZBUKA")
        azbuka = if (mListPager.comment == "") {
            getMaximAzbuka()
        } else {
            mListPager.comment.split(" ").toTypedArray()
        }
        return azbuka
    }

    //обновляем элемент ListPagerLab (свой комментарий)
    fun updateListPager(listPager: ListPager) {
//        // Проверяем, есть ли элемент в синглете, если нет, то добавляем
//        val mListPager = mDatabase.getListPagerFromBase(listPager.id, listPager.phase)
//        if (mListPager == null) {
//            listPagers.add(listPager)
//        }
        // Обновляем элемент ListPager в синглете listPagers
        for ((i, lp) in listPagers.withIndex()) {
            if ((listPager.phase == lp.phase) and (listPager.id == lp.id)) {
                listPagers[i] = listPager
            }
        }
        // Обновляем коммент в базе, если его нет, то создаем
        if (mDatabase.getListPagerFromBase(listPager.id, listPager.phase) == null) {
            mDatabase.addListPager2Base(listPager)
        } else {
            mDatabase.updateListPagerInBase(listPager)
        }

    }

}
