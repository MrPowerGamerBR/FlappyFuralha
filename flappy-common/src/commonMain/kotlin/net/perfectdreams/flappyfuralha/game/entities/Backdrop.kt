package net.perfectdreams.flappyfuralha.game.entities

import net.perfectdreams.flappyfuralha.game.GameLogic

class Backdrop(
    override val entityId: Int,
    logic: GameLogic,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
) : Entity(logic, x, y, width, height) {
    override fun tick() {}
}