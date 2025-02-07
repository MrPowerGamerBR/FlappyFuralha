package net.perfectdreams.flappyfuralha.webgl2

import web.gl.GLenum

fun GLenum.fixToInt(): Int {
    return this.toString().toInt()
}