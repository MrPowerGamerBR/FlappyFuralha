package net.perfectdreams.flappyfuralha.lwjgl.renderer

import net.perfectdreams.flappyfuralha.game.renderer.ResourceManager
import net.perfectdreams.harmony.gl.ByteArrayTextureSource
import net.perfectdreams.harmony.gl.GLConstants
import net.perfectdreams.harmony.gl.GPUResources
import net.perfectdreams.harmony.gl.textures.Texture
import org.lwjgl.opengl.GL11.GL_NEAREST
import org.lwjgl.stb.STBImage
import org.lwjgl.system.MemoryStack.stackPush
import java.nio.ByteBuffer
import kotlin.use

class LWJGLResourceManager(val gpu: GPUResources) : ResourceManager {
    override fun loadTexture(path: String): Texture {
        stackPush().use { stack ->
            val width = stack.mallocInt(1)
            val height = stack.mallocInt(1)
            val channels = stack.mallocInt(1)

            val imageBytes = ResourceManager::class.java.getResource("/$path").readBytes()

            val data = stack.malloc(imageBytes.size)
            data.put(imageBytes)
            data.rewind()

            val imageBuffer = STBImage.stbi_load_from_memory(data, width, height, channels, 4)
            if (imageBuffer == null) {
                throw java.lang.RuntimeException("Failed to load texture: " + STBImage.stbi_failure_reason())
            }

            val realPixels = ByteBuffer
                .allocate(imageBuffer.capacity())
                .put(imageBuffer)
                .flip()
            imageBuffer.flip()
            val texture = gpu.createTexture(ByteArrayTextureSource(width[0], height[0], realPixels.array()), GLConstants.GL_CLAMP_TO_BORDER, GLConstants.GL_CLAMP_TO_BORDER, GLConstants.GL_NEAREST, GLConstants.GL_NEAREST)
            STBImage.stbi_image_free(imageBuffer)
            return texture
        }
    }
}