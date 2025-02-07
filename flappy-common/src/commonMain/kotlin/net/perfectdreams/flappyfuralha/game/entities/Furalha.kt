package net.perfectdreams.flappyfuralha.game.entities

import net.perfectdreams.flappyfuralha.game.GameLogic
import net.perfectdreams.flappyfuralha.game.GameLogic.AABB
import net.perfectdreams.flappyfuralha.game.GamePhase
import net.perfectdreams.flappyfuralha.game.messages.GameMessage
import kotlin.math.sin

class Furalha(
    override val entityId: Int,
    logic: GameLogic,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
) : Entity(logic, x, y, width, height) {
    var gravity = 0.0f
    var hasShownGameOver = false
    var phase = GamePhase.PRE_START
    var elapsedPlayingTicks = 0
    var score = 0

    override fun tick() {
        when (this.phase) {
            GamePhase.PRE_START -> {
                this.y = 100f + (sin(logic.elapsedTicks.toDouble() / 4.0).toFloat() * 4f)
            }
            GamePhase.PLAYING -> {
                this.gravity += 0.5f
                this.y += this.gravity

                // Reset y coordinate if we are trying to go above the screen
                if (0 > this.y)
                    this.y = 0.0f

                // Are we intersecting with the ground?
                if (this.y + this.height >= 200f) {
                    // Then we lost!
                    this.phase = GamePhase.DIED
                    return
                }

                // Are we interscting with any pipes?
                val playerAABB = AABB(this.x, this.y, this.width, this.height)

                val pipes = this.logic.room.entities.filterIsInstance<Pipe>()

                for (pipe in pipes) {
                    val pipeAABB = AABB(pipe.x, pipe.y, pipe.width, pipe.height)

                    if (playerAABB.intersects(pipeAABB)) {
                        this.phase = GamePhase.DIED
                        return
                    }

                    // Check and give points
                    if (pipe.collectedPoints)
                        continue

                    // We only care about TOP pipes for scoring
                    // (Mostly to make our lives easier)
                    if (pipe.type == Pipe.PipeType.TOP)
                        continue

                    val middleX = (pipe.x) + pipe.width / 2

                    if (this.x >= middleX) {
                        pipe.collectedPoints = true
                        this.score += 1
                        println("Beep!")
                    }
                }

                this.elapsedPlayingTicks++
            }
            GamePhase.DIED -> {
                this.gravity += 0.7f
                this.y += this.gravity

                // Reset y coordinate if we are trying to go above the screen
                if (0 > this.y)
                    this.y = 0.0f

                // Are we intersecting with the ground?
                if (this.y + this.height >= 200f) {
                    // If yes, then we STAY HERE
                    this.y = 200f - this.height

                    this.hasShownGameOver = true
                    this.logic.room.entities.add(GameOver(this.logic.nextEntityId(), this.logic, 72.0f - (92f / 2), 25f))
                    this.logic.room.entities.add(OkButton(this.logic.nextEntityId(), this.logic, 72.0f - (40f / 2), 200f))
                }
            }
        }
    }

    override fun onGlobalClick() {
        if (this.phase != GamePhase.DIED) {
            // In Flappy Bird, the gravity resets when clicking
            this.gravity = 0f
            this.gravity -= 5f
        }
    }

    override fun onMessageReceive(message: GameMessage) {
        if (message is GameMessage.GameStarted) {
            this.phase = GamePhase.PLAYING
        }
    }
}