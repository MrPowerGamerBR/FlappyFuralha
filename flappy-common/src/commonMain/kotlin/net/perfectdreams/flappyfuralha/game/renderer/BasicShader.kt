package net.perfectdreams.flappyfuralha.game.renderer

import net.perfectdreams.harmony.gl.GLContext
import net.perfectdreams.harmony.gl.GLProgram
import net.perfectdreams.harmony.render.batcher.SpriteShader

class BasicShader(gl: GLContext, programId: GLProgram) : SpriteShader(gl, programId) {
    override val uTexture = uniformSampler2D("uTexture")
    val uProjection = uniformMatrix4fv("uProjection")
}