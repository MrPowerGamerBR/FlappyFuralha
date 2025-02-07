package net.perfectdreams.flappyfuralha.lwjgl

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
    var textureId: Int
)