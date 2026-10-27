package net.perfectdreams.flappyfuralha.webgl2.renderer

import net.perfectdreams.flappyfuralha.game.renderer.ResourceManager
import net.perfectdreams.flappyfuralha.webgl2.VirtualFileSystem
import net.perfectdreams.harmony.gl.GLConstants.GL_CLAMP_TO_EDGE
import net.perfectdreams.harmony.gl.GLConstants.GL_NEAREST
import net.perfectdreams.harmony.gl.GPUResources
import net.perfectdreams.harmony.gl.textures.Texture
import net.perfectdreams.harmony.gl.webgl2.BrowserImageTextureSource

class WebResourceManager(val virtualFileSystem: VirtualFileSystem, val gpu: GPUResources) : ResourceManager {
    override fun loadTexture(path: String): Texture {
        val image = virtualFileSystem.images[path]!!
        return gpu.createTexture(
            BrowserImageTextureSource(image),
            GL_CLAMP_TO_EDGE,
            GL_CLAMP_TO_EDGE,
            GL_NEAREST,
            GL_NEAREST
        )
    }
}