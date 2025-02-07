package net.perfectdreams.flappyfuralha.lwjgl

import net.perfectdreams.flappyfuralha.game.*
import net.perfectdreams.flappyfuralha.game.entities.*
import org.joml.Matrix4f
import org.joml.Vector2f
import org.lwjgl.Version
import org.lwjgl.glfw.Callbacks.glfwFreeCallbacks
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT
import org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT
import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL30.glBindVertexArray
import org.lwjgl.opengl.GL30.glUseProgram
import org.lwjgl.opengl.GL32
import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.system.MemoryUtil

class FlappyFuralha {
    companion object {
        const val PHYSICS_TICK = 20
        const val PHYSICS_TIME = 1000 / PHYSICS_TICK
    }

    val shaderManager = ShaderManager()
    val resourceManager = ResourceManager()

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

        // Required for transparent textures!
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        val programId = shaderManager.loadShader("game.vsh", "game.fsh")

        val quadVAO = initRender()

        // Because we are only using one shader, we can keep it like this and pass the projection once instead of passing every render
        glUseProgram(programId)

        // The right and bottom are the window size!
        // The zNear must be smaller than 0.0, because 0.0 is where we are rendering the sprites
        val projection = Matrix4f().ortho(0.0f, GameLogic.GAME_WIDTH.toFloat(), GameLogic.GAME_HEIGHT.toFloat(), 0.0f, -1.0f, 1.0f)
        // println("Projection is ${projection.get(FloatArray(16)).joinToString()}")

        val location = glGetUniformLocation(programId, "projection")
        glUniformMatrix4fv(location, false, projection.get(FloatArray(16)))

        val gameResources = GameResources(resourceManager)

        // Set the clear color
        GL11.glClearColor(1.0f, 0.0f, 0.0f, 1.0f) // WATCH OUT! LWJGL example uses 0.0f for the alpha and, while that does work for LWJGL, it DOES NOT work in WebGL!

        var totalElapsedMS = PHYSICS_TIME.toLong() // We always want first render tick to ALSO do a physics tick to process the game

        var lastGamePhysicsUpdate = System.currentTimeMillis()

        // Entity -> RenderedSprite
        val renderedSprites = mutableMapOf<Entity, RenderedSprite>()

        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while (!glfwWindowShouldClose(window)) {
            println("Processing render...")

            val startedProcessingAt = System.currentTimeMillis()
            // Implementing interpolation is a bit tricky, but not impossible!
            fun createOrUpdateEntitySprite(entity: Entity, zIndex: Int, textureId: Int) {
                val existingSprite = renderedSprites[entity]

                if (existingSprite != null) {
                    // If a existing sprite ALREADY exists, then we'll update the current sprite to make it look *smooth*
                    if (entity.interpolatePosition) {
                        existingSprite.x = existingSprite.targetX
                        existingSprite.y = existingSprite.targetY

                        existingSprite.targetX = entity.x
                        existingSprite.targetY = entity.y
                    } else {
                        // If the entity should NOT interpolate, then we set the values as is
                        existingSprite.x = entity.x
                        existingSprite.y = entity.y

                        existingSprite.targetX = entity.x
                        existingSprite.targetY = entity.y
                    }
                } else {
                    // Create a new sprite
                    renderedSprites[entity] = RenderedSprite(entity.x, entity.y, entity.x, entity.y, zIndex, entity.width, entity.height, textureId)
                }
            }

            // The game runs at 20 ticks per second (50ms)
            while (totalElapsedMS >= PHYSICS_TIME) {
                println("Processing logic! $totalElapsedMS")
                logic.tick()

                // When updating the game logic, we need to update all sprites to point to the new target coordinate
                for (entity in logic.room.entities) {
                    val zIndex: Int
                    val textureId: Int

                    when (entity) {
                        is Backdrop -> {
                            zIndex = 0
                            textureId = gameResources.backdropTextureId
                        }
                        is Pipe -> {
                            zIndex = 2
                            textureId = when (entity.type) {
                                Pipe.PipeType.TOP -> gameResources.pipeTopId
                                Pipe.PipeType.BOTTOM -> gameResources.pipeBottomId
                            }
                        }
                        is Ground -> {
                            zIndex = 3
                            textureId = gameResources.groundId
                        }
                        is Furalha -> {
                            zIndex = 4
                            textureId = gameResources.furalhaTextureId
                        }
                        is FakeFuralha -> {
                            zIndex = 4
                            textureId = gameResources.furalhaTextureId
                        }
                        is Tap -> {
                            zIndex = 999
                            textureId = gameResources.tapId
                        }
                        is GetReady -> {
                            zIndex = 999
                            textureId = gameResources.getReadyId
                        }
                        is FlappyBirdLogo -> {
                            zIndex = 999
                            textureId = gameResources.flappyBirdLogoId
                        }
                        is GameOver -> {
                            zIndex = 999
                            textureId = gameResources.gameOverId
                        }
                        is OkButton -> {
                            zIndex = 999
                            textureId = gameResources.okButtonId
                        }

                        is PlayButton -> {
                            zIndex = 999
                            textureId = gameResources.playButtonId
                        }

                        is ScoreNumber.ScoreNumberZero -> {
                            zIndex = 998
                            textureId = gameResources.scoreNumberZeroId
                        }

                        is ScoreNumber.ScoreNumberOne -> {
                            zIndex = 998
                            textureId = gameResources.scoreNumberOneId
                        }

                        is ScoreNumber.ScoreNumberTwo -> {
                            zIndex = 998
                            textureId = gameResources.scoreNumberTwoId
                        }

                        is ScoreNumber.ScoreNumberThree -> {
                            zIndex = 998
                            textureId = gameResources.scoreNumberThreeId
                        }

                        is ScoreNumber.ScoreNumberFour -> {
                            zIndex = 998
                            textureId = gameResources.scoreNumberFourId
                        }

                        is ScoreNumber.ScoreNumberFive -> {
                            zIndex = 998
                            textureId = gameResources.scoreNumberFiveId
                        }

                        is ScoreNumber.ScoreNumberSix -> {
                            zIndex = 998
                            textureId = gameResources.scoreNumberSixId
                        }

                        is ScoreNumber.ScoreNumberSeven -> {
                            zIndex = 998
                            textureId = gameResources.scoreNumberSevenId
                        }

                        is ScoreNumber.ScoreNumberEight -> {
                            zIndex = 998
                            textureId = gameResources.scoreNumberEightId
                        }

                        is ScoreNumber.ScoreNumberNine -> {
                            zIndex = 998
                            textureId = gameResources.scoreNumberNineId
                        }
                    }

                    createOrUpdateEntitySprite(
                        entity,
                        zIndex,
                        textureId,
                    )
                }

                // Remove any rendered sprites that are NOT present on the game logic anymore!
                val toBeRemoved = mutableSetOf<Entity>()
                for ((entity, _) in renderedSprites) {
                    val stillExists = logic.room.entities.contains(entity)
                    if (!stillExists) {
                        toBeRemoved.add(entity)
                    }
                }

                for (remove in toBeRemoved) {
                    renderedSprites.remove(remove)
                }

                lastGamePhysicsUpdate = startedProcessingAt
                totalElapsedMS -= PHYSICS_TIME
            }

            val diffBetweenGamePhysicsAndNow = startedProcessingAt - lastGamePhysicsUpdate
            println("Diff: $diffBetweenGamePhysicsAndNow")
            val interpolationPercent = (diffBetweenGamePhysicsAndNow / PHYSICS_TIME.toDouble())

            println("Interpolation Percent: $interpolationPercent")

            GL11.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT) // clear the framebuffer

            val sortedSprites = renderedSprites
                .values
                .sortedBy { it.zIndex }

            for (sprite in sortedSprites) {
                drawSprite(
                    programId,
                    quadVAO,
                    sprite.textureId,
                    Vector2f(
                        Easings.easeLinear(
                            sprite.x.toDouble(),
                            sprite.targetX.toDouble(),
                            interpolationPercent
                        ).toFloat(),
                        Easings.easeLinear(
                            sprite.y.toDouble(),
                            sprite.targetY.toDouble(),
                            interpolationPercent
                        ).toFloat()
                    ),
                    Vector2f(sprite.width, sprite.height)
                )
            }

            glfwSwapBuffers(window) // swap the color buffers

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents()

            val delta = System.currentTimeMillis() - startedProcessingAt
            totalElapsedMS += delta
        }
    }

    // VAO = Vertex Array Object
    // VBO = Vertex Buffer Object
    private fun initRender(): Int {
        val vbo = glGenBuffers()
        val vertices = floatArrayOf(
            // pos      // tex
            0.0f, 1.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 0.0f,

            0.0f, 1.0f, 0.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 0.0f, 1.0f, 0.0f
        )

        val quadVAO = GL32.glGenVertexArrays()

        glBindBuffer(GL_ARRAY_BUFFER, vbo)
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW)

        glBindVertexArray(quadVAO)
        glEnableVertexAttribArray(0)
        glVertexAttribPointer(0, 4, GL_FLOAT, false, 4 * Float.SIZE_BYTES, 0)
        glBindBuffer(GL_ARRAY_BUFFER, 0)
        glBindVertexArray(0)

        return quadVAO
    }

    fun drawSprite(programId: Int, quadVAO: Int, textureId: Int, position: Vector2f, size: Vector2f) {
        glUseProgram(programId)

        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, textureId)

        val model = Matrix4f()
        model.translate(position.x, position.y, 0.0f)
        model.translate(0.5f * size.x, 0.5f * size.y, 0.0f)
        // glm::rotate(model, glm::radians(rotate), glm::vec3(0.0f, 0.0f, 1.0f));
        model.translate(-0.5f * size.x, -0.5f * size.y, 0.0f)

        model.scale(size.x, size.y, 1.0f)

        val location = glGetUniformLocation(programId, "model")
        glUniformMatrix4fv(location, false, model.get(FloatArray(16)))

        glBindVertexArray(quadVAO)
        glDrawArrays(GL_TRIANGLES, 0, 6)
        glBindVertexArray(0)
    }
}