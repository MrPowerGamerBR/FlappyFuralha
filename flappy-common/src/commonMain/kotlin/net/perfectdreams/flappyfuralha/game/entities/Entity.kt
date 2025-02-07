package net.perfectdreams.flappyfuralha.game.entities

import net.perfectdreams.flappyfuralha.game.GameLogic
import net.perfectdreams.flappyfuralha.game.messages.GameMessage

sealed class Entity(
    val logic: GameLogic,
    var x: Float,
    var y: Float,
    var width: Float,
    var height: Float,
) {
    abstract val entityId: Int

    /**
     * If true, the entity's position will be interpolated
     */
    open val interpolatePosition = true

    /**
     * If the entity is not alive, the entity will be removed from the game when ticking finishes
     */
    var isAlive = true

    /**
     * Triggered when the game ticks this entity
     */
    open fun tick() {}

    /**
     * Triggered when clicking on the entity with the mouse
     */
    open fun onClick() {}

    /**
     * Triggered when clicking anywhere on the game screen
     */
    open fun onGlobalClick() {}

    /**
     * Triggered when this entity receives a [message]
     */
    open fun onMessageReceive(message: GameMessage) {}
}