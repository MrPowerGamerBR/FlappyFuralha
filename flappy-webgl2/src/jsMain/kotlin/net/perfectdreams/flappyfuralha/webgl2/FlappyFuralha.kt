package net.perfectdreams.flappyfuralha.webgl2

import js.buffer.ArrayBuffer
import js.typedarrays.Float32Array
import net.perfectdreams.flappyfuralha.game.GameLogic
import net.perfectdreams.flappyfuralha.game.GameRoom
import net.perfectdreams.flappyfuralha.game.renderer.BasicShader
import net.perfectdreams.flappyfuralha.game.renderer.FlappyFuralhaRenderer
import net.perfectdreams.flappyfuralha.game.renderer.GameResources
import net.perfectdreams.flappyfuralha.webgl2.renderer.WebResourceManager
import net.perfectdreams.harmony.gl.GLConstants
import net.perfectdreams.harmony.gl.GPUResources
import net.perfectdreams.harmony.gl.shaders.withProgram
import net.perfectdreams.harmony.gl.webgl2.WebGL2Context
import web.animations.requestAnimationFrame
import web.dom.clickEvent
import web.dom.document
import web.dom.touchStartEvent
import web.events.addHandler
import web.gl.ID
import web.gl.WebGL2RenderingContext
import web.html.HTMLCanvasElement
import web.html.HTMLElement
import web.performance.performance

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
        println("Current Millis: ${currentTimeMillis()}")

        logic.switchRoom(GameRoom.MainMenu(logic))

        val gl = init()
        loop(gl)
    }

    private fun init(): WebGL2RenderingContext {
        // Get the Canvas from the DOM
        val canvas = document.querySelector("#glCanvas") as HTMLCanvasElement
        val gl = canvas.getContext(WebGL2RenderingContext.ID) ?: error("WebGL2 is not supported!")

        canvas.clickEvent.addHandler {
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

        // We use touchstart because on mobile devices it feels sluggish to use click
        // In theory setting the viewports parameters on the page SHOULD fix this issue, but it doesn't 100% work on iOS devices (THANKS APPLE)
        // That's why we use touch start too
        // https://stackoverflow.com/a/27612273
        canvas.touchStartEvent.addHandler {
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

        return gl
    }

    private fun loop(webgl2: WebGL2RenderingContext) {
        val gl = WebGL2Context(webgl2)
        val gpu = GPUResources(gl, webgl2.drawingBufferWidth, webgl2.drawingBufferHeight)

        // Required for transparent textures!
        gl.enable(GLConstants.GL_BLEND)
        gl.blendFunc(GLConstants.GL_SRC_ALPHA, GLConstants.GL_ONE_MINUS_SRC_ALPHA)

        val shader = gpu.createShader(virtualFileSystem.files["game.vsh"]!!.decodeToString(), virtualFileSystem.files["game.fsh"]!!.decodeToString(), ::BasicShader)

        val resourceManager = WebResourceManager(virtualFileSystem, gpu)
        val gameResources = GameResources(resourceManager)
        val renderer = FlappyFuralhaRenderer(
            logic,
            gpu,
            shader,
            gameResources,
        )

        // Set the clear color
        gl.clearColor(1.0f, 0.0f, 0.0f, 1.0f)

        var totalElapsedMS = PHYSICS_TIME.toDouble() // We always want first render tick to ALSO do a physics tick to process the game

        var lastGamePhysicsUpdate = 0.0
        var lastRenderTime = 0.0

        fun render(timestamp: Double) {
            println("Processing render... $timestamp")

            val delta = timestamp - lastRenderTime
            val startedProcessingAt = timestamp

            // The game runs at 20 ticks per second (50ms)
            while (totalElapsedMS >= PHYSICS_TIME) {
                println("Processing logic! $totalElapsedMS $PHYSICS_TIME")
                logic.tick()
                renderer.tick()

                lastGamePhysicsUpdate = startedProcessingAt
                totalElapsedMS -= PHYSICS_TIME
            }

            val diffBetweenGamePhysicsAndNow = startedProcessingAt - lastGamePhysicsUpdate
            println("Diff: $diffBetweenGamePhysicsAndNow - ElapsedMS: $totalElapsedMS")
            val interpolationPercent = (diffBetweenGamePhysicsAndNow / PHYSICS_TIME.toDouble())

            println("Interpolation Percent: $interpolationPercent")

            renderer.draw(interpolationPercent)

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

    // SHOULD WORK LIKE THE JVM currentTimeMillis!
    fun currentTimeMillis() = performance.now().toLong()

    fun toFloat32Array(array: FloatArray): Float32Array<ArrayBuffer> {
        return Float32Array<ArrayBuffer>(array.toTypedArray())
    }
}