package net.perfectdreams.flappyfuralha.webgl2

object Easings {
    fun easeLinear(start: Double, end: Double, percent: Double): Double {
        return start+(end-start)*percent
    }
}