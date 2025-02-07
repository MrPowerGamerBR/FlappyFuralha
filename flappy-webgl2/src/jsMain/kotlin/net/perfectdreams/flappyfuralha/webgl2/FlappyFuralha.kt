package net.perfectdreams.flappyfuralha.webgl2

import js.buffer.ArrayBuffer
import js.typedarrays.Float32Array
import net.perfectdreams.flappyfuralha.game.GameLogic
import net.perfectdreams.flappyfuralha.game.GameRoom
import net.perfectdreams.flappyfuralha.game.entities.*
import web.animations.requestAnimationFrame
import web.dom.document
import web.events.addEventListener
import web.gl.WebGL2RenderingContext
import web.gl.WebGLProgram
import web.gl.WebGLTexture
import web.gl.WebGLVertexArrayObject
import web.html.HTMLCanvasElement
import web.html.HTMLElement
import web.performance.performance
import web.uievents.MouseEvent
import web.uievents.TouchEvent

class FlappyFuralha(val virtualFileSystem: VirtualFileSystem) {
    companion object {
        const val PHYSICS_TICK = 20
        const val PHYSICS_TIME = 1000 / PHYSICS_TICK
    }

    // We can't use 144 because that's too small for Windows, which causes the window to be resized...
    private val windowWidth = (144 * 2)
    private val windowHeight = (256 * 2)

    val logic = GameLogic()

    fun run() {
        println("Howdy from Kotlin ${KotlinVersion.CURRENT}! :3")
        println("Enum fix? ${WebGL2RenderingContext.COLOR_BUFFER_BIT.fixToInt()}")
        println("Current Millis: ${currentTimeMillis()}")

        logic.switchRoom(GameRoom.MainMenu(logic))

        val gl = init()
        loop(gl)
    }

    private fun init(): WebGL2RenderingContext {
        // Get the Canvas from the DOM
        val canvas = document.querySelector("#glCanvas") as HTMLCanvasElement
        val gl = canvas.getContext(WebGL2RenderingContext.ID) ?: error("WebGL2 is not supported!")

        canvas.addEventListener(
            MouseEvent.CLICK,
            {
                // https://stackoverflow.com/a/42111623
                val currentTarget = it.currentTarget as HTMLElement
                val rect =  currentTarget.getBoundingClientRect()
                val cursorX = it.clientX - rect.left // x position within the element.
                val cursorY = it.clientY - rect.top  // y position within the element.

                // This gets the pixel coordinate in relation of the canvas
                println("cursorXY $cursorX, $cursorY")

                // Yes, we don't need to do all of this, we can do
                // cursorX * scaleWidthDiff
                // cursorY * scaleHeightDiff
                // And we would get the same result
                // But I wanted to do it like this
                val gameXPercentage = cursorX / canvas.width
                val gameYPercentage = cursorY / canvas.height

                println("clicked at $gameXPercentage, $gameYPercentage")

                // And then we pass to the game!
                logic.onClick(
                    (gameXPercentage * GameLogic.GAME_WIDTH).toFloat(),
                    (gameYPercentage * GameLogic.GAME_HEIGHT).toFloat()
                )
            }
        )

        // We use touchstart because on mobile devices it feels sluggish to use click
        // In theory setting the viewports parameters on the page SHOULD fix this issue, but it doesn't 100% work on iOS devices (THANKS APPLE)
        // That's why we use touch start too
        // https://stackoverflow.com/a/27612273
        canvas.addEventListener(
            TouchEvent.TOUCH_START,
            {
                // https://stackoverflow.com/a/42111623
                val currentTarget = it.currentTarget as HTMLElement
                val rect =  currentTarget.getBoundingClientRect()
                val touch = it.touches.item(0)!!
                val cursorX = touch.clientX - rect.left // x position within the element.
                val cursorY = touch.clientY - rect.top  // y position within the element.

                // This gets the pixel coordinate in relation of the canvas
                println("cursorXY $cursorX, $cursorY")

                // Yes, we don't need to do all of this, we can do
                // cursorX * scaleWidthDiff
                // cursorY * scaleHeightDiff
                // And we would get the same result
                // But I wanted to do it like this
                val gameXPercentage = cursorX / canvas.width
                val gameYPercentage = cursorY / canvas.height

                println("clicked at $gameXPercentage, $gameYPercentage")

                // And then we pass to the game!
                logic.onClick(
                    (gameXPercentage * GameLogic.GAME_WIDTH).toFloat(),
                    (gameYPercentage * GameLogic.GAME_HEIGHT).toFloat()
                )

                it.preventDefault()
            }
        )

        return gl
    }

    private fun loop(gl: WebGL2RenderingContext) {
        val shaderManager = ShaderManager(virtualFileSystem, gl)

        // Required for transparent textures!
        gl.enable(WebGL2RenderingContext.BLEND)
        gl.blendFunc(WebGL2RenderingContext.SRC_ALPHA, WebGL2RenderingContext.ONE_MINUS_SRC_ALPHA)

        val programId = shaderManager.loadShader("game.vsh", "game.fsh")

        val quadVAO = initRender(gl)

        // Because we are only using one shader, we can keep it like this and pass the projection once instead of passing every render
        gl.useProgram(programId)

        // The right and bottom are the window size!
        // The zNear must be smaller than 0.0, because 0.0 is where we are rendering the sprites
        // val projection = Matrix4f().ortho(0.0f, GameLogic.GAME_WIDTH.toFloat(), GameLogic.GAME_HEIGHT.toFloat(), 0.0f, -1.0f, 1.0f)
        val projection = floatArrayOf(0.013888889f, 0.0f, 0.0f, 0.0f, 0.0f, -0.0078125f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f, -1.0f, 1.0f, -0.0f, 1.0f)
        // println("Projection is ${projection.get(FloatArray(16)).joinToString()}")

        val location = gl.getUniformLocation(programId, "projection") ?: error("Could not find uniform location")
        gl.uniformMatrix4fv(location, false, toFloat32Array(projection), null, null)

        val resourceManager = ResourceManager(virtualFileSystem, gl)
        val gameResources = GameResources(resourceManager)

        // Set the clear color
        gl.clearColor(1.0f, 0.0f, 0.0f, 1.0f)

        var totalElapsedMS = PHYSICS_TIME.toDouble() // We always want first render tick to ALSO do a physics tick to process the game

        var lastGamePhysicsUpdate = 0.0
        var lastRenderTime = 0.0

        // Entity -> RenderedSprite
        val renderedSprites = mutableMapOf<Entity, RenderedSprite>()

        fun render(timestamp: Double) {
            println("Processing render... $timestamp")

            val delta = timestamp - lastRenderTime
            val startedProcessingAt = timestamp

            // Implementing interpolation is a bit tricky, but not impossible!
            fun createOrUpdateEntitySprite(entity: Entity, zIndex: Int, textureId: WebGLTexture) {
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
                println("Processing logic! $totalElapsedMS $PHYSICS_TIME")
                logic.tick()

                // When updating the game logic, we need to update all sprites to point to the new target coordinate
                for (entity in logic.room.entities) {
                    val zIndex: Int
                    val textureId: WebGLTexture

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
            println("Diff: $diffBetweenGamePhysicsAndNow - ElapsedMS: $totalElapsedMS")
            val interpolationPercent = (diffBetweenGamePhysicsAndNow / PHYSICS_TIME.toDouble())

            println("Interpolation Percent: $interpolationPercent")

            gl.clear(WebGL2RenderingContext.COLOR_BUFFER_BIT.fixToInt() or WebGL2RenderingContext.DEPTH_BUFFER_BIT.fixToInt())

            val sortedSprites = renderedSprites
                .values
                .sortedBy { it.zIndex }

            for (sprite in sortedSprites) {
                drawSprite(
                    gl,
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

            // glfwSwapBuffers(window) // swap the color buffers

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            // glfwPollEvents()

            totalElapsedMS += delta
            lastRenderTime = timestamp

            requestAnimationFrame { render(it) }
        }

        requestAnimationFrame { render(it) }
    }

    // VAO = Vertex Array Object
    // VBO = Vertex Buffer Object
    private fun initRender(gl: WebGL2RenderingContext): WebGLVertexArrayObject {
        val vbo = gl.createBuffer()
        val vertices = floatArrayOf(
            // pos      // tex
            0.0f, 1.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 0.0f,

            0.0f, 1.0f, 0.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 0.0f, 1.0f, 0.0f
        )

        val quadVAO = gl.createVertexArray()

        gl.bindBuffer(WebGL2RenderingContext.ARRAY_BUFFER, vbo)
        // The Float32Array<ArrayBuffer> call is weird as hell, but let's carry on
        gl.bufferData(WebGL2RenderingContext.ARRAY_BUFFER, toFloat32Array(vertices), WebGL2RenderingContext.STATIC_DRAW)

        gl.bindVertexArray(quadVAO)
        gl.enableVertexAttribArray(0)
        gl.vertexAttribPointer(0, 4, WebGL2RenderingContext.FLOAT, false, 4 * Float.SIZE_BYTES, 0)
        gl.bindBuffer(WebGL2RenderingContext.ARRAY_BUFFER, null)
        gl.bindVertexArray(null)

        return quadVAO
    }

    fun drawSprite(gl: WebGL2RenderingContext, programId: WebGLProgram, quadVAO: WebGLVertexArrayObject, textureId: WebGLTexture, position: Vector2f, size: Vector2f) {
        gl.useProgram(programId)

        gl.activeTexture(WebGL2RenderingContext.TEXTURE0)
        gl.bindTexture(WebGL2RenderingContext.TEXTURE_2D, textureId)

        // It would be better to actually use a Matrix4f like how we do in the LWJGL implementation, however for our use case making the Matrix manually is not *that* hard
        val matrix4f = floatArrayOf(size.x, 0.0f, 0.0f, 0.0f, 0.0f, size.y, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, position.x, position.y, 0.0f, 1.0f)

        val location = gl.getUniformLocation(programId, "model") ?: error("Could not find uniform location")
        gl.uniformMatrix4fv(location, false, toFloat32Array(matrix4f), null, null)

        gl.bindVertexArray(quadVAO)
        gl.drawArrays(WebGL2RenderingContext.TRIANGLES, 0, 6)
        gl.bindVertexArray(null)
    }

    // SHOULD WORK LIKE THE JVM currentTimeMillis!
    fun currentTimeMillis() = performance.now().toLong()

    fun toFloat32Array(array: FloatArray): Float32Array<ArrayBuffer> {
        return Float32Array<ArrayBuffer>(array.toTypedArray())
    }
}