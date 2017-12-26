package ru.tohaman.rg3.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import org.jetbrains.anko.ctx
import org.jetbrains.anko.db.*

class BaseHelper(context: Context) : ManagedSQLiteOpenHelper(context, DATABASE_NAME, null, VERSION) {

    companion object {
        private val VERSION = 1
        private val DATABASE_NAME = "base.db"
        val TABLE_NAME : String = "baseTable"
        val PHASE : String = "phase"
        val ID : String = "id"
        val COMMENT : String = "comment"
        private var instance: BaseHelper? = null

        @Synchronized
        fun getInstance(ctx: Context): BaseHelper {
            if (instance == null) {
                instance = BaseHelper(ctx)
            }
            return instance!!
        }
    }

    // В базе храним только Фазу, номер этапа в фазе и комент для этапа... остальное инициализируем в ListPagerLab
    // Поскольку используем анко-функции для доступа к базе, то данная строка в принципе не нужна
    val DATABASE_CREATE =
            "CREATE TABLE if not exists $TABLE_NAME (" +
                    "$PHASE TEXT, " +
                    "$ID INTEGER,"  +
                    "$COMMENT TEXT" +
                    ")"


    override fun onCreate(db: SQLiteDatabase) {
        //Используем ANKO функцию, равнозначную такой
        //db.execSQL("CREATE TABLE if not exists $TABLE_NAME ($PHASE TEXT,$ID INTEGER,$COMMENT TEXT)")
        db.createTable(TABLE_NAME,true,PHASE to TEXT,ID to INTEGER,COMMENT to TEXT)

    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

    }

    fun getPagersListFromBase (phase: String) : List<ListPager> {
        val mListPagers : List<ListPager> = arrayListOf()
        val db = this.readableDatabase
        val selectQuery = "SELECT * FROM $TABLE_NAME WHERE $PHASE = '$phase'"
        val curCursor = db.rawQuery(selectQuery, null)
        curCursor.use { cursor ->
            if (cursor.count != 0) {
                cursor.moveToFirst()
                if (cursor.count > 0) {
                    do {
                        val newPhase: String = cursor.getString(cursor.getColumnIndex(PHASE))
                        val newId: Int = cursor.getInt(cursor.getColumnIndex(ID))
                        val newComment: String = cursor.getString(cursor.getColumnIndex(COMMENT))
                        mListPagers.plus(ListPager(newPhase, newId, comment = newComment))
                    } while ((cursor.moveToNext()))
                }
            }
        }
        return mListPagers
    }

    fun getListPagerFromBase(id: Int, phase: String): ListPager? {
        var listPager: ListPager? = null
        val db = this.readableDatabase
        val selectQuery = "SELECT * FROM $TABLE_NAME WHERE $PHASE = '$phase' AND $ID = $id"
        val curCursor = db.rawQuery(selectQuery, null)
        curCursor.use { cursor ->
            if (cursor.count != 0) {
                cursor.moveToFirst()
                listPager = ListPager(cursor.getString(cursor.getColumnIndex(PHASE)),
                        cursor.getInt(cursor.getColumnIndex(ID)),
                        cursor.getString(cursor.getColumnIndex(COMMENT))
                )
            }
        }
        return listPager
    }

    fun addListPager2Base(listPager: ListPager) {
        this.writableDatabase.insert(TABLE_NAME,
                PHASE to listPager.phase,
                ID to listPager.id,
                COMMENT to listPager.comment)
    }

    fun updateListPagerInBase(listPager: ListPager) {
        this.writableDatabase.update(TABLE_NAME,
                PHASE to listPager.phase,
                ID to listPager.id,
                COMMENT to listPager.comment)
    }


}

// Access property for Context
val Context.database: BaseHelper
    get() = BaseHelper.getInstance(ctx)


