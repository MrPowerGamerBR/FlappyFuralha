package net.perfectdreams.flappyfuralha.game.renderer

import net.perfectdreams.harmony.gl.GLContext
import net.perfectdreams.harmony.graphics.batcher.SpriteShader

class BasicShader(gl: GLContext, programId: Int) : SpriteShader(gl, programId) {
    override val uTexture = uniform1Sampler2D("uTexture")
    val uProjection = uniformMatrix4fv("uProjection")
}