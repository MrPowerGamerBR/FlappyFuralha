package net.perfectdreams.flappyfuralha.webgl2

import web.gl.WebGLTexture

/**
 * References a game sprite
 */
class RenderedSprite(
    var x: Float,
    var y: Float,
    var targetX: Float,
    var targetY: Float,
    var zIndex: Int,
    var width: Float,
    var height: Float,
    var textureId: WebGLTexture
)