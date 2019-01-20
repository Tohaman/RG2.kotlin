package ru.tohaman.rg2.util

import android.util.Log
import ru.tohaman.rg2.DebugTag
import ru.tohaman.rg2.R
import ru.tohaman.rg2.data.CubeAzbuka
import java.util.ArrayList

/**
 * Created by Toha on 22.12.2017.
 * Основные движения для кубика
 */

fun resetCube(): IntArray {
    Log.v (DebugTag.TAG, "resetCube")
    val cube = IntArray(54)
    for (i in cube.indices) {
        cube[i] = i / 9
    }
    return cube
}

fun prepareCubeToShowInGridView(cube: IntArray) : ArrayList<CubeAzbuka> {
    Log.v (DebugTag.TAG, "prepareCubeToShowInGridView")
    // очищаем grList = ListOf<(R.color.transparent, "")> - 108штук
    val grList = clearArray4GridList()
    // Задаем для элементов куба букву равную пробелу, и цвет соответствующий элемнтам куба (массива)
    // если остается = "" и цвет прозрачный то это элемент фона (и будет не виден)
    for (i in 0..8) {
        grList[(i / 3) * 12 + 3 + i % 3] = CubeAzbuka(cubeColor[cube[i]], " ")
        grList[(i / 3 + 3) * 12 + i % 3] = CubeAzbuka(cubeColor[cube[i + 9]], " ")
        grList[(i / 3 + 3) * 12 + 3 + i % 3] = CubeAzbuka(cubeColor[cube[i + 18]], " ")
        grList[(i / 3 + 3) * 12 + 6 + i % 3] = CubeAzbuka(cubeColor[cube[i + 27]], " ")
        grList[(i / 3 + 3) * 12 + 9 + i % 3] = CubeAzbuka(cubeColor[cube[i + 36]], " ")
        grList[(i / 3 + 6) * 12 + 3 + i % 3] = CubeAzbuka(cubeColor[cube[i + 45]], " ")
    }
    return grList
}

fun prepareAzbukaToShowInGridView(azbuka: Array<String>): ArrayList<CubeAzbuka> {
    Log.v (DebugTag.TAG, "prepareAzbukaToShowInGridView")
    val grList = clearArray4GridList()

    val cube = resetCube()

    for (i in 0..8) {
        grList[i / 3 * 12 + 3 + i % 3] = CubeAzbuka(cubeColor[cube[i]], azbuka[i])
        grList[(i / 3 + 3) * 12 + i % 3] = CubeAzbuka(cubeColor[cube[i + 9]], azbuka[i + 9])
        grList[(i / 3 + 3) * 12 + 3 + i % 3] = CubeAzbuka(cubeColor[cube[i + 18]], azbuka[i + 18])
        grList[(i / 3 + 3) * 12 + 6 + i % 3] = CubeAzbuka(cubeColor[cube[i + 27]], azbuka[i + 27])
        grList[(i / 3 + 3) * 12 + 9 + i % 3] = CubeAzbuka(cubeColor[cube[i + 36]], azbuka[i + 36])
        grList[(i / 3 + 6) * 12 + 3 + i % 3] = CubeAzbuka(cubeColor[cube[i + 45]], azbuka[i + 45])
    }
    return grList
}


fun clearArray4GridList(): ArrayList<CubeAzbuka> {
    Log.v (DebugTag.TAG, "FragmentScrambleGen clearArray4GridList")
    // 108 элементов GridList делаем пустыми и прозрачными
    val cubeAzbuka = CubeAzbuka(R.color.transparent, "")
    val grList = arrayListOf<CubeAzbuka>()
    for (i in 0..107) {
        grList.add(cubeAzbuka)
    }
    return grList
}

fun changeElements(cube: IntArray, firstElement: Int, secondElement: Int): IntArray {
    val buf = cube[firstElement]
    cube[firstElement] = cube[secondElement]
    cube[secondElement] = buf
    return cube
}

fun moveR(cube: IntArray): IntArray {
    var tmpCube = cube
    //Меняем фронт
    tmpCube = changeElements(tmpCube, 20, 2)
    tmpCube = changeElements(tmpCube, 23, 5)
    tmpCube = changeElements(tmpCube, 26, 8)
    tmpCube = changeElements(tmpCube, 20, 42)
    tmpCube = changeElements(tmpCube, 23, 39)
    tmpCube = changeElements(tmpCube, 26, 36)
    tmpCube = changeElements(tmpCube, 20, 47)
    tmpCube = changeElements(tmpCube, 23, 50)
    tmpCube = changeElements(tmpCube, 26, 53)
    //Меняем бок
    tmpCube = changeElements(tmpCube, 27, 29)
    tmpCube = changeElements(tmpCube, 27, 35)
    tmpCube = changeElements(tmpCube, 27, 33)
    tmpCube = changeElements(tmpCube, 30, 28)
    tmpCube = changeElements(tmpCube, 30, 32)
    tmpCube = changeElements(tmpCube, 30, 34)
    return tmpCube
}

fun moveRb(cube: IntArray): IntArray {
    var tmpCube = cube
    //Меняем фронт
    tmpCube = changeElements(tmpCube, 20, 47)
    tmpCube = changeElements(tmpCube, 23, 50)
    tmpCube = changeElements(tmpCube, 26, 53)
    tmpCube = changeElements(tmpCube, 20, 42)
    tmpCube = changeElements(tmpCube, 23, 39)
    tmpCube = changeElements(tmpCube, 26, 36)
    tmpCube = changeElements(tmpCube, 20, 2)
    tmpCube = changeElements(tmpCube, 23, 5)
    tmpCube = changeElements(tmpCube, 26, 8)
    //Меняем бок
    tmpCube = changeElements(tmpCube, 27, 33)
    tmpCube = changeElements(tmpCube, 27, 35)
    tmpCube = changeElements(tmpCube, 27, 29)
    tmpCube = changeElements(tmpCube, 30, 34)
    tmpCube = changeElements(tmpCube, 30, 32)
    tmpCube = changeElements(tmpCube, 30, 28)
    return tmpCube
}

fun moveR2(cube: IntArray): IntArray {
    var tmpCube = cube
    tmpCube = moveR(tmpCube)
    tmpCube = moveR(tmpCube)
    return tmpCube
}

fun moveF(cube: IntArray): IntArray {
    var tmpCube = cube
    //Меняем фронт
    tmpCube = changeElements(tmpCube, 26, 35)
    tmpCube = changeElements(tmpCube, 25, 34)
    tmpCube = changeElements(tmpCube, 24, 33)
    tmpCube = changeElements(tmpCube, 26, 44)
    tmpCube = changeElements(tmpCube, 25, 43)
    tmpCube = changeElements(tmpCube, 24, 42)
    tmpCube = changeElements(tmpCube, 26, 17)
    tmpCube = changeElements(tmpCube, 25, 16)
    tmpCube = changeElements(tmpCube, 24, 15)
    //Меняем бок
    tmpCube = changeElements(tmpCube, 46, 50)
    tmpCube = changeElements(tmpCube, 46, 52)
    tmpCube = changeElements(tmpCube, 46, 48)
    tmpCube = changeElements(tmpCube, 45, 47)
    tmpCube = changeElements(tmpCube, 45, 53)
    tmpCube = changeElements(tmpCube, 45, 51)
    return tmpCube
}

fun moveFb(cube: IntArray): IntArray {
    var tmpCube = cube
    //Меняем фронт
    tmpCube = changeElements(tmpCube, 26, 17)
    tmpCube = changeElements(tmpCube, 25, 16)
    tmpCube = changeElements(tmpCube, 24, 15)
    tmpCube = changeElements(tmpCube, 26, 44)
    tmpCube = changeElements(tmpCube, 25, 43)
    tmpCube = changeElements(tmpCube, 24, 42)
    tmpCube = changeElements(tmpCube, 26, 35)
    tmpCube = changeElements(tmpCube, 25, 34)
    tmpCube = changeElements(tmpCube, 24, 33)
    //Меняем бок
    tmpCube = changeElements(tmpCube, 46, 48)
    tmpCube = changeElements(tmpCube, 46, 52)
    tmpCube = changeElements(tmpCube, 46, 50)
    tmpCube = changeElements(tmpCube, 45, 51)
    tmpCube = changeElements(tmpCube, 45, 53)
    tmpCube = changeElements(tmpCube, 45, 47)
    return tmpCube
}

fun moveF2(cube: IntArray): IntArray {
    var tmpCube = cube
    tmpCube = moveF(tmpCube)
    tmpCube = moveF(tmpCube)
    return tmpCube
}

fun moveU(cube: IntArray): IntArray {
    var tmpCube = cube
    //Меняем фронт
    tmpCube = changeElements(tmpCube, 45, 11)
    tmpCube = changeElements(tmpCube, 46, 14)
    tmpCube = changeElements(tmpCube, 47, 17)
    tmpCube = changeElements(tmpCube, 45, 8)
    tmpCube = changeElements(tmpCube, 46, 7)
    tmpCube = changeElements(tmpCube, 47, 6)
    tmpCube = changeElements(tmpCube, 45, 33)
    tmpCube = changeElements(tmpCube, 46, 30)
    tmpCube = changeElements(tmpCube, 47, 27)
    //Меняем бок
    tmpCube = changeElements(tmpCube, 26, 24)
    tmpCube = changeElements(tmpCube, 26, 18)
    tmpCube = changeElements(tmpCube, 26, 20)
    tmpCube = changeElements(tmpCube, 25, 21)
    tmpCube = changeElements(tmpCube, 25, 19)
    tmpCube = changeElements(tmpCube, 25, 23)
    return tmpCube
}

fun moveUb(cube: IntArray): IntArray {
    var tmpCube = cube
    //Меняем фронт
    tmpCube = changeElements(tmpCube, 45, 33)
    tmpCube = changeElements(tmpCube, 46, 30)
    tmpCube = changeElements(tmpCube, 47, 27)
    tmpCube = changeElements(tmpCube, 45, 8)
    tmpCube = changeElements(tmpCube, 46, 7)
    tmpCube = changeElements(tmpCube, 47, 6)
    tmpCube = changeElements(tmpCube, 45, 11)
    tmpCube = changeElements(tmpCube, 46, 14)
    tmpCube = changeElements(tmpCube, 47, 17)
    //Меняем бок
    tmpCube = changeElements(tmpCube, 26, 20)
    tmpCube = changeElements(tmpCube, 26, 18)
    tmpCube = changeElements(tmpCube, 26, 24)
    tmpCube = changeElements(tmpCube, 25, 23)
    tmpCube = changeElements(tmpCube, 25, 19)
    tmpCube = changeElements(tmpCube, 25, 21)
    return tmpCube
}

fun moveU2(cube: IntArray): IntArray {
    var tmpCube = cube
    tmpCube = moveU(tmpCube)
    tmpCube = moveU(tmpCube)
    return tmpCube
}

fun moveL(cube: IntArray): IntArray {
    var tmpCube = cube
    //Меняем фронт
    tmpCube = changeElements(tmpCube, 18, 45)
    tmpCube = changeElements(tmpCube, 21, 48)
    tmpCube = changeElements(tmpCube, 24, 51)
    tmpCube = changeElements(tmpCube, 18, 44)
    tmpCube = changeElements(tmpCube, 21, 41)
    tmpCube = changeElements(tmpCube, 24, 38)
    tmpCube = changeElements(tmpCube, 18, 0)
    tmpCube = changeElements(tmpCube, 21, 3)
    tmpCube = changeElements(tmpCube, 24, 6)
    //Меняем бок
    tmpCube = changeElements(tmpCube, 11, 17)
    tmpCube = changeElements(tmpCube, 11, 15)
    tmpCube = changeElements(tmpCube, 11, 9)
    tmpCube = changeElements(tmpCube, 14, 16)
    tmpCube = changeElements(tmpCube, 14, 12)
    tmpCube = changeElements(tmpCube, 14, 10)
    return tmpCube
}

fun moveLb(cube: IntArray): IntArray {
    var tmpCube = cube
    //Меняем фронт
    tmpCube = changeElements(tmpCube, 18, 0)
    tmpCube = changeElements(tmpCube, 21, 3)
    tmpCube = changeElements(tmpCube, 24, 6)
    tmpCube = changeElements(tmpCube, 18, 44)
    tmpCube = changeElements(tmpCube, 21, 41)
    tmpCube = changeElements(tmpCube, 24, 38)
    tmpCube = changeElements(tmpCube, 18, 45)
    tmpCube = changeElements(tmpCube, 21, 48)
    tmpCube = changeElements(tmpCube, 24, 51)
    //Меняем бок
    tmpCube = changeElements(tmpCube, 11, 9)
    tmpCube = changeElements(tmpCube, 11, 15)
    tmpCube = changeElements(tmpCube, 11, 17)
    tmpCube = changeElements(tmpCube, 14, 10)
    tmpCube = changeElements(tmpCube, 14, 12)
    tmpCube = changeElements(tmpCube, 14, 16)
    return tmpCube
}

fun moveL2(cube: IntArray): IntArray {
    var tmpCube = cube
    tmpCube = moveL(tmpCube)
    tmpCube = moveL(tmpCube)
    return tmpCube
}

fun moveB(cube: IntArray): IntArray {
    var tmpCube = cube
    //Меняем фронт
    tmpCube = changeElements(tmpCube, 18, 9)
    tmpCube = changeElements(tmpCube, 19, 10)
    tmpCube = changeElements(tmpCube, 20, 11)
    tmpCube = changeElements(tmpCube, 18, 36)
    tmpCube = changeElements(tmpCube, 19, 37)
    tmpCube = changeElements(tmpCube, 20, 38)
    tmpCube = changeElements(tmpCube, 18, 27)
    tmpCube = changeElements(tmpCube, 19, 28)
    tmpCube = changeElements(tmpCube, 20, 29)
    //Меняем бок
    tmpCube = changeElements(tmpCube, 7, 3)
    tmpCube = changeElements(tmpCube, 7, 1)
    tmpCube = changeElements(tmpCube, 7, 5)
    tmpCube = changeElements(tmpCube, 8, 6)
    tmpCube = changeElements(tmpCube, 8, 0)
    tmpCube = changeElements(tmpCube, 8, 2)
    return tmpCube
}

fun moveBb(cube: IntArray): IntArray {
    var tmpCube = cube
    //Меняем фронт
    tmpCube = changeElements(tmpCube, 18, 27)
    tmpCube = changeElements(tmpCube, 19, 28)
    tmpCube = changeElements(tmpCube, 20, 29)
    tmpCube = changeElements(tmpCube, 18, 36)
    tmpCube = changeElements(tmpCube, 19, 37)
    tmpCube = changeElements(tmpCube, 20, 38)
    tmpCube = changeElements(tmpCube, 18, 9)
    tmpCube = changeElements(tmpCube, 19, 10)
    tmpCube = changeElements(tmpCube, 20, 11)
    //Меняем бок
    tmpCube = changeElements(tmpCube, 7, 5)
    tmpCube = changeElements(tmpCube, 7, 1)
    tmpCube = changeElements(tmpCube, 7, 3)
    tmpCube = changeElements(tmpCube, 8, 2)
    tmpCube = changeElements(tmpCube, 8, 0)
    tmpCube = changeElements(tmpCube, 8, 6)
    return tmpCube
}

fun moveB2(cube: IntArray): IntArray {
    var tmpCube = cube
    tmpCube = moveB(tmpCube)
    tmpCube = moveB(tmpCube)
    return tmpCube
}

fun moveD(cube: IntArray): IntArray {
    var tmpCube = cube
    //'Меняем фронт
    tmpCube = changeElements(tmpCube, 51, 35)
    tmpCube = changeElements(tmpCube, 52, 32)
    tmpCube = changeElements(tmpCube, 53, 29)
    tmpCube = changeElements(tmpCube, 51, 2)
    tmpCube = changeElements(tmpCube, 52, 1)
    tmpCube = changeElements(tmpCube, 53, 0)
    tmpCube = changeElements(tmpCube, 51, 9)
    tmpCube = changeElements(tmpCube, 52, 12)
    tmpCube = changeElements(tmpCube, 53, 15)
    //'Меняем бок
    tmpCube = changeElements(tmpCube, 43, 39)
    tmpCube = changeElements(tmpCube, 43, 37)
    tmpCube = changeElements(tmpCube, 43, 41)
    tmpCube = changeElements(tmpCube, 42, 36)
    tmpCube = changeElements(tmpCube, 42, 38)
    tmpCube = changeElements(tmpCube, 42, 44)
    return tmpCube
}

fun moveDb(cube: IntArray): IntArray {
    var tmpCube = cube
    //'Меняем фронт
    tmpCube = changeElements(tmpCube, 51, 9)
    tmpCube = changeElements(tmpCube, 52, 12)
    tmpCube = changeElements(tmpCube, 53, 15)
    tmpCube = changeElements(tmpCube, 51, 2)
    tmpCube = changeElements(tmpCube, 52, 1)
    tmpCube = changeElements(tmpCube, 53, 0)
    tmpCube = changeElements(tmpCube, 51, 35)
    tmpCube = changeElements(tmpCube, 52, 32)
    tmpCube = changeElements(tmpCube, 53, 29)
    //'Меняем бок
    tmpCube = changeElements(tmpCube, 43, 41)
    tmpCube = changeElements(tmpCube, 43, 37)
    tmpCube = changeElements(tmpCube, 43, 39)
    tmpCube = changeElements(tmpCube, 42, 44)
    tmpCube = changeElements(tmpCube, 42, 38)
    tmpCube = changeElements(tmpCube, 42, 36)
    return tmpCube
}

fun moveD2(cube: IntArray): IntArray {
    var tmpCube = cube
    tmpCube = moveD(tmpCube)
    tmpCube = moveD(tmpCube)
    return tmpCube
}

fun moveE(cube: IntArray): IntArray {
    var tmpCube = cube
    //'Меняем фронт
    tmpCube = changeElements(tmpCube, 48, 34)
    tmpCube = changeElements(tmpCube, 49, 31)
    tmpCube = changeElements(tmpCube, 50, 28)
    tmpCube = changeElements(tmpCube, 48, 5)
    tmpCube = changeElements(tmpCube, 49, 4)
    tmpCube = changeElements(tmpCube, 50, 3)
    tmpCube = changeElements(tmpCube, 48, 10)
    tmpCube = changeElements(tmpCube, 49, 13)
    tmpCube = changeElements(tmpCube, 50, 16)
    return tmpCube
}

fun moveEb(cube: IntArray): IntArray {
    var tmpCube = cube
    //'Меняем фронт
    tmpCube = changeElements(tmpCube, 48, 10)
    tmpCube = changeElements(tmpCube, 49, 13)
    tmpCube = changeElements(tmpCube, 50, 16)
    tmpCube = changeElements(tmpCube, 48, 5)
    tmpCube = changeElements(tmpCube, 49, 4)
    tmpCube = changeElements(tmpCube, 50, 3)
    tmpCube = changeElements(tmpCube, 48, 34)
    tmpCube = changeElements(tmpCube, 49, 31)
    tmpCube = changeElements(tmpCube, 50, 28)
    return tmpCube
}

fun moveE2(cube: IntArray): IntArray {
    var tmpCube = cube
    tmpCube = moveE(tmpCube)
    tmpCube = moveE(tmpCube)
    return tmpCube
}

fun moveM(cube: IntArray): IntArray {
    var tmpCube = cube
    //'Меняем фронт
    tmpCube = changeElements(tmpCube, 19, 46)
    tmpCube = changeElements(tmpCube, 22, 49)
    tmpCube = changeElements(tmpCube, 25, 52)
    tmpCube = changeElements(tmpCube, 19, 43)
    tmpCube = changeElements(tmpCube, 22, 40)
    tmpCube = changeElements(tmpCube, 25, 37)
    tmpCube = changeElements(tmpCube, 19, 1)
    tmpCube = changeElements(tmpCube, 22, 4)
    tmpCube = changeElements(tmpCube, 25, 7)
    return tmpCube
}

fun moveMb(cube: IntArray): IntArray {
    var tmpCube = cube
    //'Меняем фронт
    tmpCube = changeElements(tmpCube, 19, 1)
    tmpCube = changeElements(tmpCube, 22, 4)
    tmpCube = changeElements(tmpCube, 25, 7)
    tmpCube = changeElements(tmpCube, 19, 43)
    tmpCube = changeElements(tmpCube, 22, 40)
    tmpCube = changeElements(tmpCube, 25, 37)
    tmpCube = changeElements(tmpCube, 19, 46)
    tmpCube = changeElements(tmpCube, 22, 49)
    tmpCube = changeElements(tmpCube, 25, 52)
    return tmpCube
}

fun moveM2(cube: IntArray): IntArray {
    var tmpCube = cube
    tmpCube = moveM(tmpCube)
    tmpCube = moveM(tmpCube)
    return tmpCube
}

fun moveS(cube: IntArray): IntArray {
    var tmpCube = cube
    //'Меняем фронт
    tmpCube = changeElements(tmpCube, 21, 30)
    tmpCube = changeElements(tmpCube, 22, 31)
    tmpCube = changeElements(tmpCube, 23, 32)
    tmpCube = changeElements(tmpCube, 21, 39)
    tmpCube = changeElements(tmpCube, 22, 40)
    tmpCube = changeElements(tmpCube, 23, 41)
    tmpCube = changeElements(tmpCube, 21, 12)
    tmpCube = changeElements(tmpCube, 22, 13)
    tmpCube = changeElements(tmpCube, 23, 14)
    return tmpCube
}

fun moveSb(cube: IntArray): IntArray {
    var tmpCube = cube
    //'Меняем фронт
    tmpCube = changeElements(tmpCube, 21, 12)
    tmpCube = changeElements(tmpCube, 22, 13)
    tmpCube = changeElements(tmpCube, 23, 14)
    tmpCube = changeElements(tmpCube, 21, 39)
    tmpCube = changeElements(tmpCube, 22, 40)
    tmpCube = changeElements(tmpCube, 23, 41)
    tmpCube = changeElements(tmpCube, 21, 30)
    tmpCube = changeElements(tmpCube, 22, 31)
    tmpCube = changeElements(tmpCube, 23, 32)
    return tmpCube
}

fun moveS2(cube: IntArray): IntArray {
    var tmpCube = cube
    tmpCube = moveS(tmpCube)
    tmpCube = moveS(tmpCube)
    return tmpCube
}

fun moveRw(cube: IntArray): IntArray {   //Uw - верхние два слоя
    var tmpCube = cube
    tmpCube = moveR(tmpCube)
    tmpCube = moveMb(tmpCube)
    return tmpCube
}

fun moveRwb(cube: IntArray): IntArray {   //Uw' - верхние два слоя
    var tmpCube = cube
    tmpCube = moveRb(tmpCube)
    tmpCube = moveM(tmpCube)
    return tmpCube
}

fun moveRw2(cube: IntArray): IntArray {   //Uw2 - верхние два слоя
    var tmpCube = cube
    tmpCube = moveRw(tmpCube)
    tmpCube = moveRw(tmpCube)
    return tmpCube
}

fun moveUw(cube: IntArray): IntArray {   //Uw - верхние два слоя
    var tmpCube = cube
    tmpCube = moveU(tmpCube)
    tmpCube = moveEb(tmpCube)
    return tmpCube
}

fun moveUwb(cube: IntArray): IntArray {   //Uw' - верхние два слоя
    var tmpCube = cube
    tmpCube = moveUb(tmpCube)
    tmpCube = moveE(tmpCube)
    return tmpCube
}

fun moveUw2(cube: IntArray): IntArray {   //Uw2 - верхние два слоя
    var tmpCube = cube
    tmpCube = moveUw(tmpCube)
    tmpCube = moveUw(tmpCube)
    return tmpCube
}

fun moveDw(cube: IntArray): IntArray {   //Dw - нижние два слоя
    var tmpCube = cube
    tmpCube = moveD(tmpCube)
    tmpCube = moveE(tmpCube)
    return tmpCube
}

fun moveDwb(cube: IntArray): IntArray {
    var tmpCube = cube
    tmpCube = moveDb(tmpCube)
    tmpCube = moveEb(tmpCube)
    return tmpCube
}

fun moveDw2(cube: IntArray): IntArray {
    var tmpCube = cube
    tmpCube = moveDw(tmpCube)
    tmpCube = moveDw(tmpCube)
    return tmpCube
}

fun moveLw(cube: IntArray): IntArray {   //Uw - верхние два слоя
    var tmpCube = cube
    tmpCube = moveL(tmpCube)
    tmpCube = moveM(tmpCube)
    return tmpCube
}

fun moveLwb(cube: IntArray): IntArray {   //Uw' - верхние два слоя
    var tmpCube = cube
    tmpCube = moveLb(tmpCube)
    tmpCube = moveMb(tmpCube)
    return tmpCube
}

fun moveLw2(cube: IntArray): IntArray {   //Uw2 - верхние два слоя
    var tmpCube = cube
    tmpCube = moveLw(tmpCube)
    tmpCube = moveLw(tmpCube)
    return tmpCube
}

fun moveFw(cube: IntArray): IntArray {   //Uw - верхние два слоя
    var tmpCube = cube
    tmpCube = moveF(tmpCube)
    tmpCube = moveS(tmpCube)
    return tmpCube
}

fun moveFwb(cube: IntArray): IntArray {   //Uw' - верхние два слоя
    var tmpCube = cube
    tmpCube = moveFb(tmpCube)
    tmpCube = moveSb(tmpCube)
    return tmpCube
}

fun moveFw2(cube: IntArray): IntArray {   //Uw2 - верхние два слоя
    var tmpCube = cube
    tmpCube = moveFw(tmpCube)
    tmpCube = moveFw(tmpCube)
    return tmpCube
}

fun moveBw(cube: IntArray): IntArray {   //Uw - верхние два слоя
    var tmpCube = cube
    tmpCube = moveB(tmpCube)
    tmpCube = moveSb(tmpCube)
    return tmpCube
}

fun moveBwb(cube: IntArray): IntArray {   //Uw' - верхние два слоя
    var tmpCube = cube
    tmpCube = moveBb(tmpCube)
    tmpCube = moveS(tmpCube)
    return tmpCube
}

fun moveBw2(cube: IntArray): IntArray {   //Uw2 - верхние два слоя
    var tmpCube = cube
    tmpCube = moveBw(tmpCube)
    tmpCube = moveBw(tmpCube)
    return tmpCube
}

fun moveZ(cube: IntArray): IntArray {   //Uw2 - верхние два слоя
    var tmpCube = cube
    tmpCube = moveFw(tmpCube)
    tmpCube = moveBb(tmpCube)
    return tmpCube
}

fun moveZb(cube: IntArray): IntArray {   //Uw2 - верхние два слоя
    var tmpCube = cube
    tmpCube = moveBw(tmpCube)
    tmpCube = moveFb(tmpCube)
    return tmpCube
}

fun moveY(cube: IntArray): IntArray {   //Uw2 - верхние два слоя
    var tmpCube = cube
    tmpCube = moveUw(tmpCube)
    tmpCube = moveDb(tmpCube)
    return tmpCube
}

fun moveYb(cube: IntArray): IntArray {   //Uw2 - верхние два слоя
    var tmpCube = cube
    tmpCube = moveDw(tmpCube)
    tmpCube = moveUb(tmpCube)
    return tmpCube
}

fun moveX(cube: IntArray): IntArray {   //Uw2 - верхние два слоя
    var tmpCube = cube
    tmpCube = moveRw(tmpCube)
    tmpCube = moveLb(tmpCube)
    return tmpCube
}

fun moveXb(cube: IntArray): IntArray {   //Uw2 - верхние два слоя
    var tmpCube = cube
    tmpCube = moveLw(tmpCube)
    tmpCube = moveRb(tmpCube)
    return tmpCube
}
