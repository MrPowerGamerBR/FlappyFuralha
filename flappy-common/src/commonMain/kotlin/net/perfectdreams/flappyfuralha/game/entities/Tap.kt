package net.perfectdreams.flappyfuralha.game.entities

import net.perfectdreams.flappyfuralha.game.GameLogic
import net.perfectdreams.flappyfuralha.game.messages.GameMessage

class Tap(
    override val entityId: Int,
    logic: GameLogic,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
) : Entity(logic, x, y, width, height) {
    override fun tick() {}

    override fun onGlobalClick() {
        this.logic.dispatchMessage(GameMessage.GameStarted)
    }

    override fun onMessageReceive(message: GameMessage) {
        if (message is GameMessage.GameStarted) {
            this.isAlive = false
        }
    }
}