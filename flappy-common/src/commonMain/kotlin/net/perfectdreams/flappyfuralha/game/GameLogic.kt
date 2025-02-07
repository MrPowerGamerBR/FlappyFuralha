package net.perfectdreams.flappyfuralha.game

import net.perfectdreams.flappyfuralha.game.entities.*
import net.perfectdreams.flappyfuralha.game.messages.GameMessage
import kotlin.random.Random

class GameLogic {
    companion object {
        const val PIPE_SPEED = 3f

        /**
         * The width of the game area
         */
        const val GAME_WIDTH = 144

        /**
         * The height of the game area
         */
        const val GAME_HEIGHT = 256
    }

    private var currentEntityId = 0

    var room: GameRoom = GameRoom.MainMenu(this)
    val pendingClicks = mutableListOf<PendingClick>()

    var elapsedTicks = 0
    val random = Random.Default

    // The game runs at 20 ticks per second
    fun tick() {
        val currentRoom = this.room

        // To avoid any clicks outside of the physics ticks, we add them to a list and then process them during a physics tick
        while (this.pendingClicks.isNotEmpty()) {
            val click = pendingClicks.removeFirst()

            val clickAABB = AABB(click.x, click.y, 0f, 0f)

            for (entity in this.room.entities) {
                val entityAABB = AABB(entity.x, entity.y, entity.width, entity.height)

                // Pass our global click through ANY object
                entity.onGlobalClick()

                if (clickAABB.intersects(entityAABB)) {
                    // If our click intersects the AABB, then we process the onClick!
                    entity.onClick()
                }

                // This is a bit hacky... but if we changed rooms, we should NOT process ANY other clicks!!
                if (currentRoom != this.room)
                    break
            }
        }

        // Tick current room
        this.room.tick()

        // We create a copy of the entities list to avoid any ConcurrentModificationExceptions when ticking entities
        // (Example: If an entity attempts to spawn another entity)
        for (entity in this.room.entities.toList()) {
            entity.tick()
        }

        // Remove all dead entities
        this.room.entities.removeAll { !it.isAlive }

        println("Alive Entities: ${this.room.entities.size}")
        this.elapsedTicks++
    }

    fun onClick(x: Float, y: Float) {
        this.pendingClicks.add(PendingClick(x, y))
    }

    fun nextEntityId(): Int {
        return this.currentEntityId++
    }

    /**
     * Dispatches the [message] to all [entities]
     */
    fun dispatchMessage(message: GameMessage) {
        for (entity in this.room.entities) {
            entity.onMessageReceive(message)
        }
    }

    /**
     * Switches to a new [GameRoom]
     */
    fun switchRoom(newRoom: GameRoom) {
        this.room = newRoom
        newRoom.start()
    }

    /**
     * Gets the first entity in the current active room by the specified type
     *
     * Throws [NoSuchElementException] if not found
     */
    inline fun <reified T> getEntityByType(): T {
        return this.room.entities.asSequence().filterIsInstance<T>().first()
    }

    data class AABB(val x: Float, val y: Float, val width: Float, val height: Float) {
        fun intersects(other: AABB): Boolean {
            return x < other.x + other.width &&
                    x + width > other.x &&
                    y < other.y + other.height &&
                    y + height > other.y
        }
    }

    data class PendingClick(val x: Float, val y: Float)
}