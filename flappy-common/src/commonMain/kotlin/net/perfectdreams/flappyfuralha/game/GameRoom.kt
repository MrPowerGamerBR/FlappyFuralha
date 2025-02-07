package net.perfectdreams.flappyfuralha.game

import net.perfectdreams.flappyfuralha.game.GameLogic.Companion.GAME_HEIGHT
import net.perfectdreams.flappyfuralha.game.GameLogic.Companion.GAME_WIDTH
import net.perfectdreams.flappyfuralha.game.entities.*

/**
 * A game "room", similar to GameMaker's "room" system
 */
sealed class GameRoom(val logic: GameLogic) {
    val entities = mutableListOf<Entity>()

    abstract fun start()

    abstract fun tick()

    class MainMenu(logic: GameLogic) : GameRoom(logic) {
        override fun start() {
            val backdrop = Backdrop(this.logic.nextEntityId(), this.logic, 0f, 0f, GAME_WIDTH.toFloat(), GAME_HEIGHT.toFloat())
            val flappyBirdLogo = FlappyBirdLogo(this.logic.nextEntityId(), this.logic, 72.0f - (92f / 2), 40f)
            val playButton = PlayButton(this.logic.nextEntityId(), this.logic, 72.0f - (52f / 2), 150f)
            val ground1 = Ground(this.logic.nextEntityId(), this.logic, 0f, 200f, 168f, 56f)
            val ground2 = Ground(this.logic.nextEntityId(), this.logic, 168f, 200f, 168f, 56f)
            val fakeFuralha = FakeFuralha(this.logic.nextEntityId(), this.logic, 72.0f - 8f, 0f, 16f, 16f)

            entities.add(fakeFuralha)
            entities.add(backdrop)
            entities.add(flappyBirdLogo)
            entities.add(playButton)
            entities.add(ground1)
            entities.add(ground2)
        }

        override fun tick() {}
    }

    class Game(logic: GameLogic) : GameRoom(logic) {
        val furalha = Furalha(this.logic.nextEntityId(), this.logic, 72.0f - 8f, 0.0f, 16f, 16f)
        var lastProcessedScore = -1
        val scoreNumberEntities = mutableListOf<ScoreNumber>()

        override fun start() {
            val backdrop = Backdrop(this.logic.nextEntityId(), this.logic, 0f, 0f, GAME_WIDTH.toFloat(), GAME_HEIGHT.toFloat())
            val ground1 = Ground(this.logic.nextEntityId(), this.logic, 0f, 200f, 168f, 56f)
            val ground2 = Ground(this.logic.nextEntityId(), this.logic, 168f, 200f, 168f, 56f)
            val tap = Tap(this.logic.nextEntityId(), this.logic, 72.0f - (57f / 2), 130f, 57f, 30f)
            val getReady = GetReady(this.logic.nextEntityId(), this.logic, 72.0f - (92f / 2), 25f)

            entities.add(furalha)
            entities.add(backdrop)
            entities.add(tap)
            entities.add(getReady)
            entities.add(ground1)
            entities.add(ground2)
        }

        override fun tick() {
            when (furalha.phase) {
                GamePhase.PRE_START -> {}
                GamePhase.PLAYING -> {
                    val spawnNewPipe = this.furalha.elapsedPlayingTicks % 30 == 0

                    if (spawnNewPipe) {
                        val randomYOffset = this.logic.random.nextFloat() * 96f
                        this.entities.add(Pipe(this.logic.nextEntityId(), this.logic, Pipe.PipeType.TOP, 144f, -150f + randomYOffset, 26f, 160f))
                        this.entities.add(Pipe(this.logic.nextEntityId(), this.logic, Pipe.PipeType.BOTTOM, 144f, 70f + randomYOffset, 26f, 160f))
                    }
                }
                GamePhase.DIED -> {}
            }

            // Handles the score counter
            if (this.furalha.score != this.lastProcessedScore) {
                // This is like... bad lol
                for (entity in this.scoreNumberEntities) {
                    entity.isAlive = false
                }
                this.scoreNumberEntities.clear()

                val scoreAsString = this.furalha.score.toString()

                val newScoreEntities = mutableListOf<ScoreNumber>()
                for (char in scoreAsString) {
                    val newEntity = when (char) {
                        // It doesn't matter that we are spawning at 0f, 0f - We will change these coordinates later!
                        '0' -> ScoreNumber.ScoreNumberZero(this.logic.nextEntityId(), this.logic, 0f, 16f)
                        '1' -> ScoreNumber.ScoreNumberOne(this.logic.nextEntityId(), this.logic, 0f, 16f)
                        '2' -> ScoreNumber.ScoreNumberTwo(this.logic.nextEntityId(), this.logic, 0f, 16f)
                        '3' -> ScoreNumber.ScoreNumberThree(this.logic.nextEntityId(), this.logic, 0f, 16f)
                        '4' -> ScoreNumber.ScoreNumberFour(this.logic.nextEntityId(), this.logic, 0f, 16f)
                        '5' -> ScoreNumber.ScoreNumberFive(this.logic.nextEntityId(), this.logic, 0f, 16f)
                        '6' -> ScoreNumber.ScoreNumberSix(this.logic.nextEntityId(), this.logic, 0f, 16f)
                        '7' -> ScoreNumber.ScoreNumberSeven(this.logic.nextEntityId(), this.logic, 0f, 16f)
                        '8' -> ScoreNumber.ScoreNumberEight(this.logic.nextEntityId(), this.logic, 0f, 16f)
                        '9' -> ScoreNumber.ScoreNumberNine(this.logic.nextEntityId(), this.logic, 0f, 16f)
                        else -> error("Missing mapping for character $char")
                    }

                    newScoreEntities.add(newEntity)
                }

                var scoreX = 0.0f
                for (entity in newScoreEntities) {
                    entity.x = scoreX
                    scoreX += entity.width
                    scoreX += 1f
                }

                scoreX -= 1f

                // And now we offset the values to centralize them!
                val startX = (GameLogic.GAME_WIDTH / 2) - (scoreX / 2)
                for (entity in newScoreEntities) {
                    entity.x += startX
                }

                this.scoreNumberEntities.addAll(newScoreEntities)
                this.entities.addAll(newScoreEntities)
            }

            this.lastProcessedScore = furalha.score
        }
    }
}