package net.perfectdreams.flappyfuralha.game.entities

import net.perfectdreams.flappyfuralha.game.GameLogic
import net.perfectdreams.flappyfuralha.game.GamePhase
import net.perfectdreams.flappyfuralha.game.GameRoom
import net.perfectdreams.flappyfuralha.game.messages.GameMessage

class PlayButton(
    override val entityId: Int,
    logic: GameLogic,
    x: Float,
    y: Float
) : Entity(logic, x, y, 52f, 29f) {
    override fun onClick() {
        logic.switchRoom(GameRoom.Game(logic))
    }
}