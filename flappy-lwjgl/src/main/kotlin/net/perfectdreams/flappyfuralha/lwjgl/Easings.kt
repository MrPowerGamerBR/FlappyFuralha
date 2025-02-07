package net.perfectdreams.flappyfuralha.lwjgl

object Easings {
    fun easeLinear(start: Double, end: Double, percent: Double): Double {
        return start+(end-start)*percent
    }
}