package net.perfectdreams.flappyfuralha.game.entities

import net.perfectdreams.flappyfuralha.game.GameLogic
import net.perfectdreams.flappyfuralha.game.GamePhase
import net.perfectdreams.flappyfuralha.game.messages.GameMessage

class GetReady(
    override val entityId: Int,
    logic: GameLogic,
    x: Float,
    y: Float
) : Entity(logic, x, y, 92f, 25f) {
    override fun onMessageReceive(message: GameMessage) {
        if (message is GameMessage.GameStarted) {
            this.isAlive = false
        }
    }
}