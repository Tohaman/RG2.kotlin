package ru.tohaman.rg2.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import org.jetbrains.anko.ctx
import org.jetbrains.anko.db.*
import kotlin.collections.ArrayList

class BaseHelper(context: Context) : ManagedSQLiteOpenHelper(context, DATABASE_NAME, null, VERSION) {

    companion object {
        private const val VERSION = 2       //Версия базы данных
        private const val DATABASE_NAME = "base.db"

        const val TABLE_TIME : String = "timeTable"
        const val UUID: String = "uuid"
        const val CUR_TIME: String = "currentTime"
        const val NOTE_DATE: String = "dateOfNOte"
        const val TIME_COMMENT = "timeComment"
        const val SCRAMBLE = "scramble"

        const val TABLE_MAIN : String = "baseTable"
        const val PHASE : String = "phase"
        const val ID : String = "id"
        const val COMMENT : String = "comment"
        const val SUB_ID : String = "subId"
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
            "CREATE TABLE if not exists $TABLE_MAIN (" +
                    "$PHASE TEXT, " +
                    "$ID INTEGER,"  +
                    "$COMMENT TEXT" +
                    ")"


    override fun onCreate(db: SQLiteDatabase) {
        //Используем ANKO функцию, равнозначную такой
        //db.execSQL("CREATE TABLE if not exists $TABLE_MAIN ($PHASE TEXT,$ID INTEGER,$COMMENT TEXT)")

        //Для верисии 1
        db.createTable(TABLE_MAIN,true,PHASE to TEXT, ID to INTEGER, COMMENT to TEXT)

        //Добалено в версии 2
        db.createTable(TABLE_TIME, true,
                UUID to INTEGER + PRIMARY_KEY + UNIQUE,
                CUR_TIME to TEXT,
                NOTE_DATE to TEXT,
                TIME_COMMENT to TEXT,
                SCRAMBLE to TEXT)
        //Для версии 3
//        db.createTable(TABLE_MAIN,true,
//                PHASE to TEXT,
//                ID to INTEGER,
//                COMMENT to TEXT,
//                SUB_ID to INTEGER)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        //Если апгрейдим первую версию, то добавляем таблицу
        if (oldVersion == 1) {
            db.createTable(TABLE_TIME, true,
                    UUID to INTEGER + PRIMARY_KEY + UNIQUE,
                    CUR_TIME to TEXT,
                    NOTE_DATE to TEXT,
                    TIME_COMMENT to TEXT,
                    SCRAMBLE to TEXT)
        }
        if (oldVersion < 3) {
            //db.createTable(TABLE_MAIN_V3,true,PHASE to TEXT, ID to INTEGER, COMMENT to TEXT, SUB_ID to INTEGER)
            val lps = db.select(TABLE_MAIN).parseList(object : MapRowParser<ListPager> {
                override fun parseRow(columns: Map<String, Any?>): ListPager {
                    val phase = columns.getValue(PHASE).toString()
                    val id = columns.getValue(ID).toString()
                    val comment = columns.getValue(COMMENT).toString()
                    return ListPager(phase = phase,id = id.toInt(),comment = comment)
                }
            })

            db.dropTable(TABLE_MAIN, true)
            db.createTable(TABLE_MAIN,true,PHASE to TEXT,
                    ID to INTEGER, COMMENT to TEXT, SUB_ID to INTEGER)
            for (i in lps.indices) {
                //this.writableDatabase.insert()
            }
        }
    }

    fun getPagersListFromBase (phase: String) : List<ListPager> {
        val mListPagers : List<ListPager> = arrayListOf()
        val db = this.readableDatabase
        val selectQuery = "SELECT * FROM $TABLE_MAIN WHERE $PHASE = '$phase'"
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
        val selectQuery = "SELECT * FROM $TABLE_MAIN WHERE $PHASE = '$phase' AND $ID = $id"
        val curCursor = db.rawQuery(selectQuery, null)
        curCursor.use { cursor ->
            if (cursor.count != 0) {
                cursor.moveToFirst()
                listPager = ListPager(phase = cursor.getString(cursor.getColumnIndex(PHASE)),
                        id = cursor.getInt(cursor.getColumnIndex(ID)),
                        comment = cursor.getString(cursor.getColumnIndex(COMMENT))
                )
            }
        }
        return listPager
    }

    fun getCommentFromBase (id: Int, phase: String) : String {
        var comment = getListPagerFromBase(id, phase)?.comment
        if (comment == null) { comment = ""}
        return comment
    }

    fun addListPager2Base(listPager: ListPager) {
        this.writableDatabase.insert(TABLE_MAIN,
                PHASE to listPager.phase,
                ID to listPager.id,
                COMMENT to listPager.comment)
    }

    fun updateListPagerInBase(listPager: ListPager) {
        this.writableDatabase.update(TABLE_MAIN,
                COMMENT to listPager.comment)
                .whereSimple("PHASE = ? AND ID = ?", listPager.phase, listPager.id.toString())
                .exec()
    }

    fun addTimeNote2Base (timeNote: TimeNote) {
        this.writableDatabase.insert(TABLE_TIME,
                CUR_TIME to timeNote.time,
                NOTE_DATE to timeNote.dateTime,
                SCRAMBLE to timeNote.scramble,
                TIME_COMMENT to timeNote.comment)
    }

    fun updateTimeNoteInBase(timeNote: TimeNote) {
        this.writableDatabase.update(TABLE_TIME,
                SCRAMBLE to timeNote.scramble,
                TIME_COMMENT to timeNote.comment )
                .whereSimple("UUID = ?", timeNote.uuid)
                .exec()
    }

    fun deleteTimeNoteInBase(uuid : Int) {
        this.writableDatabase.delete(TABLE_TIME,"UUID = {ID}", "ID" to uuid )
    }

    fun getTimeNoteFromBase(): List<TimeNote> {
        var timeNoteList = ArrayList<TimeNote>()
        val db = this.readableDatabase
        db.select(TABLE_TIME).parseList(object : MapRowParser<List<TimeNote>> {
            override fun parseRow(columns: Map<String, Any?>): List<TimeNote> {
                val uuid = columns.getValue(UUID).toString()
                val time = columns.getValue(CUR_TIME).toString()
                val date = columns.getValue(NOTE_DATE).toString()
                val scramble = columns.getValue(SCRAMBLE).toString()
                val comment = columns.getValue(TIME_COMMENT).toString()
                val tm = TimeNote(time, date, scramble, comment, uuid)
                timeNoteList.add(tm)
                return timeNoteList
            }
        })
        return timeNoteList
    }

    fun addColumn() {
        val dbw = this.writableDatabase
        val lps = dbw.select(TABLE_MAIN).parseList(object : MapRowParser<ListPager> {
            override fun parseRow(columns: Map<String, Any?>): ListPager {
                val phase = columns.getValue(PHASE).toString()
                val id = columns.getValue(ID).toString()
                val comment = columns.getValue(COMMENT).toString()
                return ListPager(phase = phase,id = id.toInt(),comment = comment)
            }
        })
        dbw.dropTable(TABLE_MAIN, true)
        dbw.createTable(TABLE_MAIN,true,PHASE to TEXT,
                ID to INTEGER, COMMENT to TEXT, SUB_ID to INTEGER)
        for (i in lps.indices) {
            //dbw.insert()

        }
    }

}

// Access property for Context
val Context.database: BaseHelper
    get() = BaseHelper.getInstance(ctx)


