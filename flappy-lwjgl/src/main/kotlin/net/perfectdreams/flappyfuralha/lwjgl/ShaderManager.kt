package net.perfectdreams.flappyfuralha.lwjgl

import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30.*

class ShaderManager {
    /**
     * Loads the vertex shader and fragment shader by their file name from the application's resources
     */
    fun loadShader(vertexShaderFileName: String, fragmentShaderFileName: String): Int {
        val vertexShaderId = glCreateShader(GL_VERTEX_SHADER)
        val fragmentShaderId = glCreateShader(GL_FRAGMENT_SHADER)

        val vertexShaderCode = ShaderManager::class.java.getResource("/$vertexShaderFileName").readText(Charsets.UTF_8)
        val fragmentShaderCode = ShaderManager::class.java.getResource("/$fragmentShaderFileName").readText(Charsets.UTF_8)

        // Compile Vertex Shader
        checkAndCompile(vertexShaderId, vertexShaderCode)

        // Compile Fragment Shader
        checkAndCompile(fragmentShaderId, fragmentShaderCode)

        val programId = glCreateProgram()
        glAttachShader(programId, vertexShaderId)
        glAttachShader(programId, fragmentShaderId)
        glLinkProgram(programId)

        // Check the program
        val result = glGetProgrami(programId, GL_LINK_STATUS)
        val infoLog = glGetProgramInfoLog(programId, GL_INFO_LOG_LENGTH)

        // YES DON'T FORGET THAT WE NEED TO USE GL_TRUE!!
        // I was checking using == 0 and of course that doesn't work because that means FALSE (i think)
        if (result != GL_TRUE) {
            error("Something went wrong while linking shader! Status: $result; Info: $infoLog")
        }

        glDetachShader(programId, vertexShaderId)
        glDetachShader(programId, fragmentShaderId)

        glDeleteShader(vertexShaderId)
        glDeleteShader(fragmentShaderId)

        return programId
    }

    private fun checkAndCompile(shaderId: Int, code: String) {
        // Compile Shader
        glShaderSource(shaderId, code)
        glCompileShader(shaderId)

        // Check Shader
        val result = GL20.glGetShaderi(shaderId, GL_COMPILE_STATUS)
        val infoLog = GL20.glGetShaderInfoLog(shaderId, GL_INFO_LOG_LENGTH)

        // YES DON'T FORGET THAT WE NEED TO USE GL_TRUE!!
        // I was checking using == 0 and of course that doesn't work because that means FALSE (i think)
        if (result != GL_TRUE) {
            error("Something went wrong while compiling shader $shaderId! Status: $result; Info: $infoLog")
        }
    }
}