package net.perfectdreams.flappyfuralha.game.entities

import net.perfectdreams.flappyfuralha.game.GameLogic
import net.perfectdreams.flappyfuralha.game.GamePhase
import net.perfectdreams.flappyfuralha.game.GameRoom

class Ground(
    override val entityId: Int,
    logic: GameLogic,
    x: Float,
    y: Float,
    width: Float,
    height: Float
) : Entity(logic, x, y, width, height) {
    override fun tick() {
        // Kinda hacky but it is what it is
        var scroll = true
        val room = logic.room

        if (room is GameRoom.Game) {
            val furalha = room.furalha
            scroll = furalha.phase == GamePhase.PRE_START || furalha.phase == GamePhase.PLAYING
        }

        if (scroll) {
            this.x -= GameLogic.PIPE_SPEED

            // Due to entity position interpolation, we need to create a new ground every single time
            // (Yes, there is the "interpolatePosition" variable, but the issue with it is that it causes the ground to be choppy when moving)
            if (0 >= this.x + width) {
                // Remove this ground from the world
                this.isAlive = false

                // Add new ground!
                this.logic.room.entities.add(Ground(logic.nextEntityId(), logic, 168f, 200f, 168f, 56f))
            }
        }
    }
}