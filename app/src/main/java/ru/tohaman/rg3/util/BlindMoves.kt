package ru.tohaman.rg3.util

/**
 * Created by Toha on 22.12.2017. Внеклассовые методы, для ходов кубика Рубика
 * Алгоритмы постановки на место элементов для слепой сборки кубика
 *
 */

fun runScramble(cube: IntArray, scrm: String): IntArray {
    var scrm = scrm
    scrm = scrm.replace("'", "1")
    scrm = scrm.replace("r", "Rw")
    scrm = scrm.replace("l", "Lw")
    scrm = scrm.replace("u", "Uw")
    scrm = scrm.replace("d", "Dw")
    scrm = scrm.replace("f", "Fw")
    scrm = scrm.replace("b", "Bw")
    val ArScrm = scrm.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
    for (i in ArScrm.indices) {
        val hod = ArScrm[i]
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

fun Blinde20(cube: IntArray): IntArray {  //белосинекрасный угол
    runScramble(cube, "R D' F'")
    australia(cube)
    runScramble(cube, "F D R'")
    return cube
}

fun Blinde26(cube: IntArray): IntArray {  //белокраснозеленый угол
    australia(cube)
    return cube
}

fun Blinde24(cube: IntArray): IntArray {  //белозеленооранжевый угол
    runScramble(cube, "F' D R")
    australia(cube)
    runScramble(cube, "R' D' F")
    return cube
}

fun Blinde45(cube: IntArray): IntArray {  //зеленооранжевобелый
    runScramble(cube, "F' D F'")
    australia(cube)
    runScramble(cube, "F D' F")
    return cube
}

fun Blinde47(cube: IntArray): IntArray {  //зеленобелосиний
    runScramble(cube, "F R")
    australia(cube)
    runScramble(cube, "R' F'")
    return cube
}

fun Blinde53(cube: IntArray): IntArray {  //зеленокрасножелтый
    runScramble(cube, "R")
    australia(cube)
    runScramble(cube, "R'")
    return cube
}

fun Blinde51(cube: IntArray): IntArray {  //зеленожелтооранжевый
    runScramble(cube, "D F'")
    australia(cube)
    runScramble(cube, "F D'")
    return cube
}

fun Blinde8(cube: IntArray): IntArray {  //синекраснобелый
    runScramble(cube, "R'")
    australia(cube)
    runScramble(cube, "R")
    return cube
}

fun Blinde2(cube: IntArray): IntArray {  //синежелтокрасный
    runScramble(cube, "D' F'")
    australia(cube)
    runScramble(cube, "F D")
    return cube
}

fun Blinde0(cube: IntArray): IntArray {  //синеоранжевожелтый
    runScramble(cube, "D2 R")
    australia(cube)
    runScramble(cube, "R' D2")
    return cube
}

fun Blinde17(cube: IntArray): IntArray {  //оранжевобелозеленый
    runScramble(cube, "F")
    australia(cube)
    runScramble(cube, "F'")
    return cube
}

fun Blinde15(cube: IntArray): IntArray {  //оранжевозеленожелтый
    runScramble(cube, "D R")
    australia(cube)
    runScramble(cube, "R' D'")
    return cube
}

fun Blinde9(cube: IntArray): IntArray {  //оранжевожелтосиний
    runScramble(cube, "D2 F'")
    australia(cube)
    runScramble(cube, "F D2")
    return cube
}

fun Blinde27(cube: IntArray): IntArray {  //краснобелосиний
    runScramble(cube, "R2 F'")
    australia(cube)
    runScramble(cube, "F R2")
    return cube
}

fun Blinde33(cube: IntArray): IntArray {  //краснозеленобелый
    runScramble(cube, "R' F'")
    australia(cube)
    runScramble(cube, "F R")
    return cube
}

fun Blinde35(cube: IntArray): IntArray {  //красножелтозеленый
    runScramble(cube, "F'")
    australia(cube)
    runScramble(cube, "F")
    return cube
}

fun Blinde29(cube: IntArray): IntArray {  //красносинежелтый
    runScramble(cube, "R F'")
    australia(cube)
    runScramble(cube, "F R'")
    return cube
}

fun Blinde38(cube: IntArray): IntArray {  //желтосинеоранжевый
    runScramble(cube, "D' R2")
    australia(cube)
    runScramble(cube, "R2 D")
    return cube
}

fun Blinde36(cube: IntArray): IntArray {  //желтокрасносиний
    runScramble(cube, "R2")
    australia(cube)
    runScramble(cube, "R2")
    return cube
}

fun Blinde42(cube: IntArray): IntArray {  //желтозеленокрасный
    runScramble(cube, "D R2")
    australia(cube)
    runScramble(cube, "R2 D'")
    return cube
}

fun Blinde44(cube: IntArray): IntArray {  //желтооранжевозеленый
    runScramble(cube, "D2 R2")
    australia(cube)
    runScramble(cube, "R2 D2")
    return cube
}

