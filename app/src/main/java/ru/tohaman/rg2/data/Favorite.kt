package ru.tohaman.rg2.data

/**
 * Класс для одной записи в Избранном
 */
data class Favorite constructor(
        val phase: String,
        val id: Int,
        var comment: String = ""
    )

data class FavoriteList constructor(
        val favorites: ArrayList<Favorite>
)