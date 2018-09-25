package ru.tohaman.rg2.data


/**
 * Класс для одной записи в Избранном
 */
data class Favorite constructor(
        val phase: String,
        val id: Int,
        var comment: String = ""
    ) {

    override
    fun equals(other: Any?): Boolean {
        //Переопределяем сравнение. При сравнение объектов не учитываем комментарий
        return if (other is Favorite) {
            (this.phase == other.phase) and (this.id == other.id)
        } else {
            false
        }
    }
}
