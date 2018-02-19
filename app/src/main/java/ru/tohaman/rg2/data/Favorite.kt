package ru.tohaman.rg2.data

import android.R.attr.name



/**
 * Класс для одной записи в Избранном
 */
data class Favorite constructor(
        val phase: String,
        val id: Int,
        var comment: String = ""
    ) {

    override
    fun equals(obj: Any?): Boolean {
        //Переопределяем сравнение. При сравнение объектов не учитываем комментарий
        return if (obj is Favorite) {
            (this.phase == obj.phase) and (this.id == obj.id)
        } else {
            false
        }
    }
}
