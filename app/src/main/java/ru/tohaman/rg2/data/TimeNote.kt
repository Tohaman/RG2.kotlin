package ru.tohaman.rg2.data

data class TimeNote constructor (
        var time: String = "0:05:57",
        val dateTime: String = "28/03/18 10:00",
        var scramble: String = "",
        var comment: String = "",
        var uuid: String = "0" ) {

    companion object {
        val TABLE_TIME : String = "timeTable"
        val UUID: String = "uuid"
        val CUR_TIME: String = "currentTime"
        val NOTE_DATE: String = "dateOfNOte"
        val TIME_COMMENT = "timeComment"
        val SCRAMBLE = "scramble"
    }

    //Благодаря возможности Котлина задать значения по-умолчанию в кострукторе, можно обойти без вот такого создания доп.конструкторов
}



