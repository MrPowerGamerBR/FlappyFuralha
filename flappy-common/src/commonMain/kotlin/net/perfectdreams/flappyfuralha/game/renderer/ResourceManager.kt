package net.perfectdreams.flappyfuralha.game.renderer

import net.perfectdreams.harmony.render.textures.Texture

interface ResourceManager {
    fun loadTexture(path: String): Texture
}