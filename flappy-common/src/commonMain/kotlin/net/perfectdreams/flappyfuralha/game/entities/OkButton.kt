package net.perfectdreams.flappyfuralha.game.entities

import net.perfectdreams.flappyfuralha.game.GameLogic
import net.perfectdreams.flappyfuralha.game.GamePhase
import net.perfectdreams.flappyfuralha.game.GameRoom
import net.perfectdreams.flappyfuralha.game.messages.GameMessage

class OkButton(
    override val entityId: Int,
    logic: GameLogic,
    x: Float,
    y: Float
) : Entity(logic, x, y, 40f, 14f) {
    override fun onClick() {
        this.logic.switchRoom(GameRoom.MainMenu(this.logic))
    }
}