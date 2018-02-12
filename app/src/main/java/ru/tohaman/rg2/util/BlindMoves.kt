package ru.tohaman.rg2.util

import android.util.Log
import ru.tohaman.rg2.DebugTag
import java.util.*

/**
 * Created by Toha on 22.12.2017. Внеклассовые методы, для ходов кубика Рубика
 * Алгоритмы постановки на место элементов для слепой сборки кубика
 *
 */

//Генерация скрамбла определенной длинны (без учета переплавки буфера)
fun generateScramble(length: Int): String {
    Log.v(DebugTag.TAG, "FragmentScrambleGen generateScramble $length")
    val random = Random()
    var scramble = ""
    var i = 0
    var prevRandom = random.nextInt(0..6)                     //генерируем число от 0 до 5
    var prevPrevRandom = random.nextInt(0..6)                 //генерируем число от 0 до 5
    val map = hashMapOf(0 to "R", 1 to "L", 2 to "F", 3 to "B", 4 to "U", 5 to "D")

    do {
        val curRandom = random.nextInt(0..6)                     //генерируем число от 0 до 5
        if (curRandom != prevRandom) {
            if ((curRandom / 2 != prevRandom / 2) or (curRandom != prevPrevRandom)) {
                i++                                                 //увеличиваем счетчик на 1
                // ход будет по часовой, против или двойной
                when (random.nextInt(3)) {
                //по часовой
                    0 -> { scramble = "$scramble${map[curRandom]} "  }      //просто добавляем букву
                //против часовой
                    1 -> { scramble = "$scramble${map[curRandom]}' " }      //добавляем букву c '
                //двойной
                    2 -> { scramble = "$scramble${map[curRandom]}2 " }      //добавляем двойку
                }
                prevPrevRandom = prevRandom                         // запоминаем -2 позиции от текущего числа
                prevRandom = curRandom                              //запоминаем это число в prevRandom
            }
        }
    } while (i < length)

    scramble = scramble.trim (' ')                 //убираем лишние пробелы
    return scramble
}

fun Random.nextInt(range: IntRange): Int {
    return range.start + nextInt(range.last - range.start)
}

fun runScramble(cube: IntArray, scrm: String): IntArray {
    var scrambleString = scrm
    scrambleString = scrambleString.replace("'", "1")
    scrambleString = scrambleString.replace("r", "Rw")
    scrambleString = scrambleString.replace("l", "Lw")
    scrambleString = scrambleString.replace("u", "Uw")
    scrambleString = scrambleString.replace("d", "Dw")
    scrambleString = scrambleString.replace("f", "Fw")
    scrambleString = scrambleString.replace("b", "Bw")
    val arrayOfScramble = scrambleString.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
    for (i in arrayOfScramble.indices) {
        val hod = arrayOfScramble[i]
        when (hod) {
            "R" -> moveR(cube)
            "R1" -> moveRb(cube)
            "R2" -> moveR2(cube)
            "F" -> moveF(cube)
            "F1" -> moveFb(cube)
            "F2" -> moveF2(cube)
            "U" -> moveU(cube)
            "U1" -> moveUb(cube)
            "U2" -> moveU2(cube)
            "L" -> moveL(cube)
            "L1" -> moveLb(cube)
            "L2" -> moveL2(cube)
            "B" -> moveB(cube)
            "B1" -> moveBb(cube)
            "B2" -> moveB2(cube)
            "D" -> moveD(cube)
            "D1" -> moveDb(cube)
            "D2" -> moveD2(cube)
            "E" -> moveE(cube)
            "E1" -> moveEb(cube)
            "E2" -> moveE2(cube)
            "M" -> moveM(cube)
            "M1" -> moveMb(cube)
            "M2" -> moveM2(cube)
            "S" -> moveS(cube)
            "S1" -> moveSb(cube)
            "S2" -> moveS2(cube)
            "Rw" -> moveRw(cube)
            "Rw1" -> moveRwb(cube)
            "Rw2" -> moveRw2(cube)
            "Uw" -> moveUw(cube)
            "Uw1" -> moveUwb(cube)
            "Uw2" -> moveUw2(cube)
            "Dw" -> moveDw(cube)
            "Dw1" -> moveDwb(cube)
            "Dw2" -> moveDw2(cube)
            "Lw" -> moveLw(cube)
            "Lw1" -> moveLwb(cube)
            "Lw2" -> moveLw2(cube)
            "Fw" -> moveFw(cube)
            "Fw1" -> moveFwb(cube)
            "Fw2" -> moveFw2(cube)
            "Bw" -> moveBw(cube)
            "Bw1" -> moveBwb(cube)
            "Bw2" -> moveBw2(cube)
        }
    }
    return cube
}

fun zapad(cube: IntArray): IntArray {  //Алгоритм Запад
    runScramble(cube, "R U R' U' R' F R2 U' R' U' R U R' F'")
    return cube
}

fun yug(cube: IntArray): IntArray {  //Алгоритм Юг
    runScramble(cube, "R U R' F' R U R' U' R' F R2 U' R' U'")
    return cube
}

fun pifPaf(cube: IntArray): IntArray {  //Алгоритм Пиф-паф
    runScramble(cube, "R U R' U'")
    return cube
}

fun ekvator(cube: IntArray): IntArray {  //Алгоритм Экватор
    runScramble(cube, "R U R' F' R U2 R' U2 R' F R U R U2 R' U'")
    return cube
}

fun australia(cube: IntArray): IntArray {  //Алгоритм Австралия
    runScramble(cube, "F R U' R' U' R U R' F' R U R' U' R' F R F'")
    return cube
}

fun blinde19(cube: IntArray): IntArray {  //белосинее ребро
    runScramble(cube, "M2 D' L2")
    zapad(cube)
    runScramble(cube, "L2 D M2")
    return cube
}

fun blinde25(cube: IntArray): IntArray {  //белозеленое
    yug(cube)
    return cube
}

fun blinde21(cube: IntArray): IntArray {  //белооранжевое
    zapad(cube)
    return cube
}

fun blinde46(cube: IntArray): IntArray {  //зеленобелое
    runScramble(cube, "M D' L2")
    zapad(cube)
    runScramble(cube, "L2 D M'")
    return cube
}

fun blinde50(cube: IntArray): IntArray {  //зеленокрасное
    runScramble(cube, "Dw2 L")
    zapad(cube)
    runScramble(cube, "L' Dw2")
    return cube
}

fun blinde52(cube: IntArray): IntArray {  //зеленожелтое
    runScramble(cube, "M'")
    yug(cube)
    runScramble(cube, "M")
    return cube
}

fun blinde48(cube: IntArray): IntArray {  //зеленооранжевое
    runScramble(cube, "L'")
    zapad(cube)
    runScramble(cube, "L")
    return cube
}

fun blinde7(cube: IntArray): IntArray {  //синебелое
    runScramble(cube, "M")
    yug(cube)
    runScramble(cube, "M'")
    return cube
}

fun blinde5(cube: IntArray): IntArray {  //синекрасное
    runScramble(cube, "Dw2 L'")
    zapad(cube)
    runScramble(cube, "L Dw2")
    return cube
}

fun blinde1(cube: IntArray): IntArray {  //синежелтое
    runScramble(cube, "D2 M'")
    yug(cube)
    runScramble(cube, "M D2")
    return cube
}

fun blinde3(cube: IntArray): IntArray {  //синеоранжевое
    runScramble(cube, "L")
    zapad(cube)
    runScramble(cube, "L'")
    return cube
}

fun blinde14(cube: IntArray): IntArray {  //оранжевобелое
    runScramble(cube, "L2 D M'")
    yug(cube)
    runScramble(cube, "M D' L2")
    return cube
}

fun blinde16(cube: IntArray): IntArray {  //оранжевозеленое
    runScramble(cube, "Dw' L")
    zapad(cube)
    runScramble(cube, "L' Dw")
    return cube
}

fun blinde12(cube: IntArray): IntArray {  //оранжевожелтое
    runScramble(cube, "D M'")
    yug(cube)
    runScramble(cube, "M D'")
    return cube
}

fun blinde10(cube: IntArray): IntArray {  //оранжевосинее
    runScramble(cube, "Dw L'")
    zapad(cube)
    runScramble(cube, "L Dw'")
    return cube
}

fun blinde34(cube: IntArray): IntArray {  //краснозеленое
    runScramble(cube, "Dw' L'")
    zapad(cube)
    runScramble(cube, "L Dw")
    return cube
}

fun blinde32(cube: IntArray): IntArray {  //красножелтое
    runScramble(cube, "D' M'")
    yug(cube)
    runScramble(cube, "M D")
    return cube
}

fun blinde28(cube: IntArray): IntArray {  //красносинее
    runScramble(cube, "Dw L")
    zapad(cube)
    runScramble(cube, "L' Dw'")
    return cube
}

fun blinde37(cube: IntArray): IntArray {  //желтосинее
    runScramble(cube, "D L2")
    zapad(cube)
    runScramble(cube, "L2 D'")
    return cube
}

fun blinde39(cube: IntArray): IntArray {  //желтокрасное
    runScramble(cube, "D2 L2")
    zapad(cube)
    runScramble(cube, "L2 D2")
    return cube
}

fun blinde43(cube: IntArray): IntArray {  //желтозеленое
    runScramble(cube, "D' L2")
    zapad(cube)
    runScramble(cube, "L2 D")
    return cube
}

fun blinde41(cube: IntArray): IntArray {  //желтооранжевое
    runScramble(cube, "L2")
    zapad(cube)
    runScramble(cube, "L2")
    return cube
}

//--------------------------------------------------------------------------------------------------

fun blinde20(cube: IntArray): IntArray {  //белосинекрасный угол
    runScramble(cube, "R D' F'")
    australia(cube)
    runScramble(cube, "F D R'")
    return cube
}

fun blinde26(cube: IntArray): IntArray {  //белокраснозеленый угол
    australia(cube)
    return cube
}

fun blinde24(cube: IntArray): IntArray {  //белозеленооранжевый угол
    runScramble(cube, "F' D R")
    australia(cube)
    runScramble(cube, "R' D' F")
    return cube
}

fun blinde45(cube: IntArray): IntArray {  //зеленооранжевобелый
    runScramble(cube, "F' D F'")
    australia(cube)
    runScramble(cube, "F D' F")
    return cube
}

fun blinde47(cube: IntArray): IntArray {  //зеленобелосиний
    runScramble(cube, "F R")
    australia(cube)
    runScramble(cube, "R' F'")
    return cube
}

fun blinde53(cube: IntArray): IntArray {  //зеленокрасножелтый
    runScramble(cube, "R")
    australia(cube)
    runScramble(cube, "R'")
    return cube
}

fun blinde51(cube: IntArray): IntArray {  //зеленожелтооранжевый
    runScramble(cube, "D F'")
    australia(cube)
    runScramble(cube, "F D'")
    return cube
}

fun blinde8(cube: IntArray): IntArray {  //синекраснобелый
    runScramble(cube, "R'")
    australia(cube)
    runScramble(cube, "R")
    return cube
}

fun blinde2(cube: IntArray): IntArray {  //синежелтокрасный
    runScramble(cube, "D' F'")
    australia(cube)
    runScramble(cube, "F D")
    return cube
}

fun blinde0(cube: IntArray): IntArray {  //синеоранжевожелтый
    runScramble(cube, "D2 R")
    australia(cube)
    runScramble(cube, "R' D2")
    return cube
}

fun blinde17(cube: IntArray): IntArray {  //оранжевобелозеленый
    runScramble(cube, "F")
    australia(cube)
    runScramble(cube, "F'")
    return cube
}

fun blinde15(cube: IntArray): IntArray {  //оранжевозеленожелтый
    runScramble(cube, "D R")
    australia(cube)
    runScramble(cube, "R' D'")
    return cube
}

fun blinde9(cube: IntArray): IntArray {  //оранжевожелтосиний
    runScramble(cube, "D2 F'")
    australia(cube)
    runScramble(cube, "F D2")
    return cube
}

fun blinde27(cube: IntArray): IntArray {  //краснобелосиний
    runScramble(cube, "R2 F'")
    australia(cube)
    runScramble(cube, "F R2")
    return cube
}

fun blinde33(cube: IntArray): IntArray {  //краснозеленобелый
    runScramble(cube, "R' F'")
    australia(cube)
    runScramble(cube, "F R")
    return cube
}

fun blinde35(cube: IntArray): IntArray {  //красножелтозеленый
    runScramble(cube, "F'")
    australia(cube)
    runScramble(cube, "F")
    return cube
}

fun blinde29(cube: IntArray): IntArray {  //красносинежелтый
    runScramble(cube, "R F'")
    australia(cube)
    runScramble(cube, "F R'")
    return cube
}

fun blinde38(cube: IntArray): IntArray {  //желтосинеоранжевый
    runScramble(cube, "D' R2")
    australia(cube)
    runScramble(cube, "R2 D")
    return cube
}

fun blinde36(cube: IntArray): IntArray {  //желтокрасносиний
    runScramble(cube, "R2")
    australia(cube)
    runScramble(cube, "R2")
    return cube
}

fun blinde42(cube: IntArray): IntArray {  //желтозеленокрасный
    runScramble(cube, "D R2")
    australia(cube)
    runScramble(cube, "R2 D'")
    return cube
}

fun blinde44(cube: IntArray): IntArray {  //желтооранжевозеленый
    runScramble(cube, "D2 R2")
    australia(cube)
    runScramble(cube, "R2 D2")
    return cube
}

