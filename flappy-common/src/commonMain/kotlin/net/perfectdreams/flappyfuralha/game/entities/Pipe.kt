package net.perfectdreams.flappyfuralha.game.entities

import net.perfectdreams.flappyfuralha.game.GameLogic
import net.perfectdreams.flappyfuralha.game.GamePhase
import net.perfectdreams.flappyfuralha.game.GameRoom

class Pipe(
    override val entityId: Int,
    logic: GameLogic,
    val type: PipeType,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
) : Entity(logic, x, y, width, height) {
    var collectedPoints = false

    override fun tick() {
        // We except that the pipe will ONLY be used in a game room
        val room = logic.room as GameRoom.Game

        when (room.furalha.phase) {
            GamePhase.PLAYING -> {
                this.x -= GameLogic.PIPE_SPEED

                // Remove ourselves if we are outside of the screen
                val areWeOutsideOfTheScreen = 0f > this.x + this.width
                if (areWeOutsideOfTheScreen) {
                    this.isAlive = false
                }
            }
            GamePhase.PRE_START, GamePhase.DIED -> {}
        }
    }

    enum class PipeType {
        TOP,
        BOTTOM
    }
}