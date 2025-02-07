package net.perfectdreams.flappyfuralha.lwjgl

import org.lwjgl.opengl.GL30.*
import org.lwjgl.stb.STBImage
import org.lwjgl.system.MemoryStack.stackPush
import java.nio.ByteBuffer

class ResourceManager {
    fun loadTexture(path: String): Int {
        val textureID: Int
        val imageBuffer: ByteBuffer?

        stackPush().use { stack ->
            val width = stack.mallocInt(1)
            val height = stack.mallocInt(1)
            val channels = stack.mallocInt(1)

            val imageBytes = ResourceManager::class.java.getResource("/$path").readBytes()

            val data = stack.malloc(imageBytes.size)
            data.put(imageBytes)
            data.rewind()

            imageBuffer = STBImage.stbi_load_from_memory(data, width, height, channels, 4)
            if (imageBuffer == null) {
                throw java.lang.RuntimeException("Failed to load texture: " + STBImage.stbi_failure_reason())
            }

            textureID = glGenTextures()
            glBindTexture(GL_TEXTURE_2D, textureID)

            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width[0], height[0], 0, GL_RGBA, GL_UNSIGNED_BYTE, imageBuffer)

            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
            STBImage.stbi_image_free(imageBuffer)
        }
        return textureID
    }
}