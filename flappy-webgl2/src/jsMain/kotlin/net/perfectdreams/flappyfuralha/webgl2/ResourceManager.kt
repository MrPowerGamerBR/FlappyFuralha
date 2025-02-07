package net.perfectdreams.flappyfuralha.webgl2

import web.gl.WebGL2RenderingContext
import web.gl.WebGLTexture

class ResourceManager(val virtualFileSystem: VirtualFileSystem, val gl: WebGL2RenderingContext) {
    fun loadTexture(path: String): WebGLTexture {
        val textureID = gl.createTexture()
        gl.bindTexture(WebGL2RenderingContext.TEXTURE_2D, textureID)

        println("Loaded file: ${virtualFileSystem.images[path]!!}")
        gl.texImage2D(
            WebGL2RenderingContext.TEXTURE_2D,
            0,
            WebGL2RenderingContext.RGBA.fixToInt(),
            WebGL2RenderingContext.RGBA,
            WebGL2RenderingContext.UNSIGNED_BYTE,
            virtualFileSystem.images[path]!!
        )

        gl.texParameteri(
            WebGL2RenderingContext.TEXTURE_2D,
            WebGL2RenderingContext.TEXTURE_MIN_FILTER,
            WebGL2RenderingContext.NEAREST.fixToInt()
        )

        gl.texParameteri(
            WebGL2RenderingContext.TEXTURE_2D,
            WebGL2RenderingContext.TEXTURE_MAG_FILTER,
            WebGL2RenderingContext.NEAREST.fixToInt()
        )

        return textureID
    }
}