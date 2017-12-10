package ru.tohaman.rg3.listpager

import android.content.Context
import org.jetbrains.anko.db.insert
import ru.tohaman.rg3.R
import ru.tohaman.rg3.database.BaseHelper
import ru.tohaman.rg3.database.BaseHelper.Companion.COMMENT
import ru.tohaman.rg3.database.BaseHelper.Companion.ID
import ru.tohaman.rg3.database.BaseHelper.Companion.PHASE
import ru.tohaman.rg3.database.BaseHelper.Companion.TABLE_NAME
import java.util.ArrayList

/**
 * Created by Toha on 21.05.2017. Синглетный класс, при первом вызове создающий сиглет, хранящмй всю информацию о всех фазах
 * в вие коллекции объектов типа ListPager, данные считывает из SQLite базы, где хранятся комменты к каждому этапу фазы.
 * если в базе нет записи, она создается со значениями по-умолчанию (0,"").
 */

class ListPagerLab private constructor(context: Context){
    private val mDatabase = BaseHelper(context)
    var listPagers = arrayListOf<ListPager>()

    init { // тут пишем то, что выполнится при инициализации синглета
        //menuAdd("MAIN", R.array.main_title,R.array.main_icon,context)
       // menuAdd("MENU2X2",R.array.menu2x2_title,R.array.menu2x2_icon,context)
       // menuAdd("MENU3X3",R.array.menu3x3_title,R.array.menu3x3_icon,context)
//        phaseInit("PLL",R.array.pll_title,R.array.pll_icon,R.array.pll_descr,R.array.pll_url,context)
        phaseInit("BEGIN",R.array.begin_title,R.array.begin_icon,R.array.begin_descr,R.array.begin_url,context)
        phaseInit("BASIC",R.array.basic_title,R.array.basic_icon,R.array.basic_descr,R.array.basic_url,context)
//        phaseInit("BEGIN4X4",R.array.begin4_title,R.array.begin4_icon,R.array.begin4_descr,R.array.begin4_url,context)
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

    private fun menuAdd(phase:String, titleArray:Int, iconArray:Int, context: Context){
        val mTitles =  context.resources.getStringArray(titleArray)
        val resID = context.resources.obtainTypedArray (iconArray)
        for (i in mTitles.indices) {
            listPagers.add( ListPager(phase, i, mTitles[i],resID.getResourceId(i, 0)))
        }
        resID.recycle()
    }

    // Инициализация фазы, с заданными массивами Заголовков, Иконок, Описаний, ютуб-ссылок
    private fun phaseInit(phase: String, titleArray: Int, iconArray: Int, descrArray: Int, urlArray: Int, context: Context) {
        val mTitles =  context.resources.getStringArray(titleArray)
        val mIcon = context.resources.obtainTypedArray (iconArray)
        val mDescr = context.resources.obtainTypedArray (descrArray)
        val mUrl = context.resources.getStringArray(urlArray)
        var mListPager: ListPager?
        for (i in mTitles.indices) {
            mListPager = mDatabase.getListPagerFromBase(i, phase)
            if (mListPager == null) {
                mListPager = ListPager(phase, i, mTitles[i], mIcon.getResourceId(i,0), mDescr.getResourceId(i,0), mUrl[i])
                addListPager2Base(mListPager)
            } else {
                mListPager.title = mTitles[i]
                mListPager.icon= mIcon.getResourceId(i,0)
                mListPager.description = mDescr.getResourceId(i,0)
                mListPager.url = mUrl[i]
            }
            listPagers.add(mListPager)
        }
        mIcon.recycle()
        mDescr.recycle()
    }

    // получить комментарий для заданных фазы и номера из базы в виде объекта ListPager (с пустыми остальными полями)

    fun addListPager2Base(listPager: ListPager) {
        mDatabase.writableDatabase.insert(TABLE_NAME,
                    PHASE to listPager.phase,
                    ID to listPager.id,
                    COMMENT to listPager.comment)
    }

    //возвращает из ListPagerLab список ListPager'ов с заданной фазой (все записи для данной фазы)
    fun getPhaseList(phase: String): ArrayList<ListPager> {
        val pagerLists = ArrayList<ListPager>()
        for (listPager in listPagers) {
            if (phase == listPager.phase) {
                pagerLists.add(listPager)
            }
        }
        return pagerLists
    }

    //возвращает из ListPagerLab один ListPager с заданными фазой и номером
    fun getPhaseItem(id: Int, phase: String): ListPager? {
        var mPagerList: ListPager? = null

        for (listPager in listPagers) {
            if ((phase == listPager.phase) and (id == listPager.id)) {
                mPagerList = listPager
            }
        }
        return mPagerList
    }


    fun getMaximAzbuka() =  arrayOf(
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

}
