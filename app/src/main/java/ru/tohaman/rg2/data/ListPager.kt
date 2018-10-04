package ru.tohaman.rg2.data

//        phase: String,         // фаза - PLL,OLL,Beginer,Blind etc
//        Id: Int,               // порядковый номер этапа в фазе
//        Title: String,         // Название этапа
//        Icon: Int,             // иконка этапа
//        Description: Int,      // описание этапа
//        Url: String,           // ссылка на ютубвидео
//        Comment: String,       // свой коммент к этапу
//        SubID: String)         // номер подэтапа

data class ListPager constructor (
        var phase: String,
        val id: Int,
        var title: String = "",
        var icon: Int = 0,
        var description: Int = 0,
        var url: String = "",
        var comment: String = "",
        var subID : String = "",
        var subTitle : String = "",
        var subLongTitle: String = "") {

    //Благодаря возможности Котлина задать значения по-умолчанию в кострукторе, можно обойти без вот такого создания доп.конструкторов
    //конструктор для объекта содержащего только фазу, номер в фазе, название этапа и иконку (например для главного меню)
    //constructor (phase: String, id: Int, title: String, icon: Int) : this (phase, id, title, icon,0,"",""){}
    //конструктор для объекта содержащего только фазу, номер в фазе и комментарий
    //constructor (phase: String, id: Int, comment: String) : this (phase, id, "", 0,0,"",comment){}
}





