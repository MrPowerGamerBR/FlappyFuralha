package net.perfectdreams.flappyfuralha.game.entities

import net.perfectdreams.flappyfuralha.game.GameLogic
import net.perfectdreams.flappyfuralha.game.GameLogic.AABB
import net.perfectdreams.flappyfuralha.game.GamePhase
import kotlin.math.sin

class FakeFuralha(
    override val entityId: Int,
    logic: GameLogic,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
) : Entity(logic, x, y, width, height) {
    override fun tick() {
        this.y = 100f + (sin(logic.elapsedTicks.toDouble() / 4.0).toFloat() * 4f)
    }
}