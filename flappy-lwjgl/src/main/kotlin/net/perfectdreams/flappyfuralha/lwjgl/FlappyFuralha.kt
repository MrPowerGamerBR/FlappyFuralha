package net.perfectdreams.flappyfuralha.lwjgl

import net.perfectdreams.flappyfuralha.game.*
import net.perfectdreams.flappyfuralha.game.entities.*
import net.perfectdreams.flappyfuralha.game.renderer.BasicShader
import net.perfectdreams.flappyfuralha.game.renderer.Easings
import net.perfectdreams.flappyfuralha.game.renderer.FlappyFuralhaRenderer
import net.perfectdreams.flappyfuralha.game.renderer.GameResources
import net.perfectdreams.flappyfuralha.lwjgl.renderer.LWJGLResourceManager
import net.perfectdreams.harmony.gl.GLConstants.GL_BLEND
import net.perfectdreams.harmony.gl.GLConstants.GL_COLOR_BUFFER_BIT
import net.perfectdreams.harmony.gl.GLConstants.GL_DEPTH_BUFFER_BIT
import net.perfectdreams.harmony.gl.GLConstants.GL_ONE_MINUS_SRC_ALPHA
import net.perfectdreams.harmony.gl.GLConstants.GL_SRC_ALPHA
import net.perfectdreams.harmony.gl.GPUResources
import net.perfectdreams.harmony.gl.opengl.OpenGLContext
import net.perfectdreams.harmony.gl.opengl.OpenGLDebugging
import net.perfectdreams.harmony.gl.shaders.withProgram
import net.perfectdreams.harmony.graphics.batcher.SpriteBatcher
import org.joml.Matrix4f
import org.lwjgl.Version
import org.lwjgl.glfw.Callbacks.glfwFreeCallbacks
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL
import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.system.MemoryUtil

class FlappyFuralha {
    companion object {
        const val PHYSICS_TICK = 20
        const val PHYSICS_TIME = 1000 / PHYSICS_TICK
    }

    // The window handle
    private var window: Long = 0

    // We can't use 144 because that's too small for Windows, which causes the window to be resized...
    private val windowWidth = (144 * 2)
    private val windowHeight = (256 * 2)

    val scaleWidthDiff = GameLogic.GAME_WIDTH / windowWidth.toDouble()
    val scaleHeightDiff = GameLogic.GAME_HEIGHT / windowHeight.toDouble()

    val logic = GameLogic()

    fun run() {
        println("Howdy from Kotlin ${KotlinVersion.CURRENT}! :3 - LWJGL " + Version.getVersion())

        logic.switchRoom(GameRoom.MainMenu(logic))

        init()
        loop()

        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(window)
        glfwDestroyWindow(window)

        // Terminate GLFW and free the error callback
        glfwTerminate()
        glfwSetErrorCallback(null)!!.free()
    }

    private fun init() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set()

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        check(glfwInit()) { "Unable to initialize GLFW" }

        // Configure GLFW
        glfwDefaultWindowHints() // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE) // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE) // the window will be resizable

        // Set GLFW to use OpenGL Core Profile
        // OpenGL 3.3
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3)
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3)
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE)
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE)

        // Create the window
        window = GLFW.glfwCreateWindow(windowWidth, windowHeight, "Power's Flappy Bird", MemoryUtil.NULL, MemoryUtil.NULL)
        if (window == MemoryUtil.NULL) throw RuntimeException("Failed to create the GLFW window")

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        // We will detect this in the rendering loop
        glfwSetKeyCallback(
            window
        ) { window: Long, key: Int, scancode: Int, action: Int, mods: Int ->
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                // Close the window when pressing escape
                glfwSetWindowShouldClose(
                    window,
                    true
                )
            }
        }

        glfwSetMouseButtonCallback(window) { window: Long, button: Int, action: Int, mods: Int ->
            // On click...

            // We only care about left click and pressing rn
            if (button == GLFW_MOUSE_BUTTON_LEFT && action == GLFW_PRESS) {
                // We figure out where we are clicking on the window
                val xpos = DoubleArray(1)
                val ypos = DoubleArray(1)

                glfwGetCursorPos(window, xpos, ypos)

                val cursorX = xpos[0]
                val cursorY = ypos[0]

                // Yes, we don't need to do all of this, we can do
                // cursorX * scaleWidthDiff
                // cursorY * scaleHeightDiff
                // And we would get the same result
                // But I wanted to do it like this
                val gameXPercentage = (cursorX / GameLogic.GAME_WIDTH) * scaleWidthDiff
                val gameYPercentage = (cursorY / GameLogic.GAME_HEIGHT) * scaleHeightDiff

                // And then we pass to the game!
                logic.onClick(
                    (gameXPercentage * GameLogic.GAME_WIDTH).toFloat(),
                    (gameYPercentage * GameLogic.GAME_HEIGHT).toFloat()
                )
            }
        }

        stackPush().use { stack ->
            val pWidth = stack.mallocInt(1) // int*
            val pHeight = stack.mallocInt(1) // int*

            // Get the window size passed to glfwCreateWindow
            GLFW.glfwGetWindowSize(window, pWidth, pHeight)

            // Get the resolution of the primary monitor
            val vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor())

            // Center the window
            glfwSetWindowPos(
                window,
                (vidmode!!.width() - pWidth[0]) / 2,
                (vidmode!!.height() - pHeight[0]) / 2
            )
        }
        // Make the OpenGL context current
        glfwMakeContextCurrent(window)

        // Enable V-sync
        glfwSwapInterval(1)

        // Make the window visible
        glfwShowWindow(window)
    }

    private fun loop() {
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities()
        OpenGLDebugging.enableReportGLErrors()

        val gl = OpenGLContext()
        val gpu = GPUResources(gl, windowWidth, windowHeight)
        val resourceManager = LWJGLResourceManager(gpu)

        val shader = gpu.createShader(
            FlappyFuralha::class.java.getResourceAsStream("/game.vsh")!!.readAllBytes().toString(Charsets.UTF_8),
            FlappyFuralha::class.java.getResourceAsStream("/game.fsh")!!.readAllBytes().toString(Charsets.UTF_8),
            ::BasicShader
        )

        // Required for transparent textures!
        gl.enable(GL_BLEND)
        gl.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        val gameResources = GameResources(resourceManager)

        // Set the clear color
        gl.clearColor(1.0f, 0.0f, 0.0f, 1.0f) // WATCH OUT! LWJGL example uses 0.0f for the alpha and, while that does work for LWJGL, it DOES NOT work in WebGL!

        var totalElapsedMS = PHYSICS_TIME.toLong() // We always want first render tick to ALSO do a physics tick to process the game

        var lastGamePhysicsUpdate = System.currentTimeMillis()

        val renderer = FlappyFuralhaRenderer(
            logic,
            gpu,
            shader,
            gameResources
        )

        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while (!glfwWindowShouldClose(window)) {
            println("Processing render...")

            val startedProcessingAt = System.currentTimeMillis()

            // The game runs at 20 ticks per second (50ms)
            while (totalElapsedMS >= PHYSICS_TIME) {
                println("Processing logic! $totalElapsedMS")

                logic.tick()
                renderer.tick()

                lastGamePhysicsUpdate = startedProcessingAt
                totalElapsedMS -= PHYSICS_TIME
            }

            val diffBetweenGamePhysicsAndNow = startedProcessingAt - lastGamePhysicsUpdate
            println("Diff: $diffBetweenGamePhysicsAndNow")
            val interpolationPercent = (diffBetweenGamePhysicsAndNow / PHYSICS_TIME.toDouble())

            println("Interpolation Percent: $interpolationPercent")

            renderer.draw(interpolationPercent)

            glfwSwapBuffers(window) // swap the color buffers

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents()

            val delta = System.currentTimeMillis() - startedProcessingAt
            totalElapsedMS += delta
        }
    }
}