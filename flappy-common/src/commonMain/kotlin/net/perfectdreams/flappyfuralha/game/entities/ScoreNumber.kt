package net.perfectdreams.flappyfuralha.game.entities

import net.perfectdreams.flappyfuralha.game.GameLogic
import net.perfectdreams.flappyfuralha.game.GamePhase
import net.perfectdreams.flappyfuralha.game.GameRoom
import net.perfectdreams.flappyfuralha.game.messages.GameMessage

sealed class ScoreNumber(
    override val entityId: Int,
    logic: GameLogic,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
) : Entity(logic, x, y, width, height) {
    class ScoreNumberZero(
        override val entityId: Int,
        logic: GameLogic,
        x: Float,
        y: Float
    ) : ScoreNumber(entityId, logic, x, y, 12f, 18f)

    class ScoreNumberOne(
        override val entityId: Int,
        logic: GameLogic,
        x: Float,
        y: Float
    ) : ScoreNumber(entityId, logic, x, y, 8f, 18f)

    class ScoreNumberTwo(
        override val entityId: Int,
        logic: GameLogic,
        x: Float,
        y: Float
    ) : ScoreNumber(entityId, logic, x, y, 12f, 18f)

    class ScoreNumberThree(
        override val entityId: Int,
        logic: GameLogic,
        x: Float,
        y: Float
    ) : ScoreNumber(entityId, logic, x, y, 12f, 18f)

    class ScoreNumberFour(
        override val entityId: Int,
        logic: GameLogic,
        x: Float,
        y: Float
    ) : ScoreNumber(entityId, logic, x, y, 12f, 18f)

    class ScoreNumberFive(
        override val entityId: Int,
        logic: GameLogic,
        x: Float,
        y: Float
    ) : ScoreNumber(entityId, logic, x, y, 12f, 18f)

    class ScoreNumberSix(
        override val entityId: Int,
        logic: GameLogic,
        x: Float,
        y: Float
    ) : ScoreNumber(entityId, logic, x, y, 12f, 18f)

    class ScoreNumberSeven(
        override val entityId: Int,
        logic: GameLogic,
        x: Float,
        y: Float
    ) : ScoreNumber(entityId, logic, x, y, 12f, 18f)

    class ScoreNumberEight(
        override val entityId: Int,
        logic: GameLogic,
        x: Float,
        y: Float
    ) : ScoreNumber(entityId, logic, x, y, 12f, 18f)

    class ScoreNumberNine(
        override val entityId: Int,
        logic: GameLogic,
        x: Float,
        y: Float
    ) : ScoreNumber(entityId, logic, x, y, 12f, 18f)
}