package net.perfectdreams.flappyfuralha.webgl2

import web.gl.WebGLProgram
import web.gl.WebGLRenderingContext
import web.gl.WebGLShader
import web.gl.WebGL2RenderingContext

class ShaderManager(val virtualFileSystem: VirtualFileSystem, val gl: WebGL2RenderingContext) {
    /**
     * Loads the vertex shader and fragment shader by their file name from the application's resources
     */
    fun loadShader(vertexShaderFileName: String, fragmentShaderFileName: String): WebGLProgram {
        val vertexShaderId = gl.createShader(WebGL2RenderingContext.VERTEX_SHADER) ?: error("Failed to create vertex shader")
        val fragmentShaderId = gl.createShader(WebGL2RenderingContext.FRAGMENT_SHADER) ?: error("Failed to create fragment shader")

        // Compile Vertex Shader
        checkAndCompile(vertexShaderId, virtualFileSystem.files[vertexShaderFileName]!!.decodeToString())

        // Compile Fragment Shader
        checkAndCompile(fragmentShaderId, virtualFileSystem.files[fragmentShaderFileName]!!.decodeToString())

        val programId = gl.createProgram() ?: error("Failed to create WebGL shader program")
        gl.attachShader(programId, vertexShaderId)
        gl.attachShader(programId, fragmentShaderId)
        gl.linkProgram(programId)

        // Check the program
        val result = gl.getProgramParameter(programId, WebGLRenderingContext.LINK_STATUS)
        val infoLog = gl.getProgramInfoLog(programId)

        // YES DON'T FORGET THAT WE NEED TO USE GL_TRUE!!
        // I was checking using == 0 and of course that doesn't work because that means FALSE (i think)
        if (result != true) {
            error("Something went wrong while linking shader! Status: $result; Info: $infoLog")
        }

        gl.detachShader(programId, vertexShaderId)
        gl.detachShader(programId, fragmentShaderId)

        gl.deleteShader(vertexShaderId)
        gl.deleteShader(fragmentShaderId)

        return programId
    }

    private fun checkAndCompile(shaderId: WebGLShader, code: String) {
        // Compile Shader
        gl.shaderSource(shaderId, code)
        gl.compileShader(shaderId)

        // Check Shader
        val result = gl.getShaderParameter(shaderId, WebGLRenderingContext.COMPILE_STATUS)
        val infoLog = gl.getShaderInfoLog(shaderId)

        // YES DON'T FORGET THAT WE NEED TO USE GL_TRUE!!
        // I was checking using == 0 and of course that doesn't work because that means FALSE (i think)
        if (result != true) {
            error("Something went wrong while compiling shader $shaderId! Status: $result; Info: $infoLog")
        }
    }
}