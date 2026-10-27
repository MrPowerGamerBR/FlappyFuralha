package net.perfectdreams.flappyfuralha.game.renderer

import net.perfectdreams.flappyfuralha.game.GameLogic
import net.perfectdreams.flappyfuralha.game.entities.Backdrop
import net.perfectdreams.flappyfuralha.game.entities.Entity
import net.perfectdreams.flappyfuralha.game.entities.FakeFuralha
import net.perfectdreams.flappyfuralha.game.entities.FlappyBirdLogo
import net.perfectdreams.flappyfuralha.game.entities.Furalha
import net.perfectdreams.flappyfuralha.game.entities.GameOver
import net.perfectdreams.flappyfuralha.game.entities.GetReady
import net.perfectdreams.flappyfuralha.game.entities.Ground
import net.perfectdreams.flappyfuralha.game.entities.OkButton
import net.perfectdreams.flappyfuralha.game.entities.Pipe
import net.perfectdreams.flappyfuralha.game.entities.PlayButton
import net.perfectdreams.flappyfuralha.game.entities.ScoreNumber
import net.perfectdreams.flappyfuralha.game.entities.Tap
import net.perfectdreams.harmony.gl.GLConstants
import net.perfectdreams.harmony.gl.GPUResources
import net.perfectdreams.harmony.gl.textures.Texture
import net.perfectdreams.harmony.graphics.batcher.SpriteBatcher
import kotlin.collections.iterator

class FlappyFuralhaRenderer(
    val logic: GameLogic,
    val gpu: GPUResources,
    val shader: BasicShader,
    val gameResources: GameResources
) {
    val batcher = SpriteBatcher(gpu.gl)

    // Entity -> RenderedSprite
    val renderedSprites = mutableMapOf<Entity, RenderedSprite>()

    // Implementing interpolation is a bit tricky, but not impossible!
    fun createOrUpdateEntitySprite(entity: Entity, zIndex: Int, texture: Texture) {
        val existingSprite = renderedSprites[entity]

        if (existingSprite != null) {
            // If a existing sprite ALREADY exists, then we'll update the current sprite to make it look *smooth*
            if (entity.interpolatePosition) {
                existingSprite.x = existingSprite.targetX
                existingSprite.y = existingSprite.targetY

                existingSprite.targetX = entity.x
                existingSprite.targetY = entity.y
            } else {
                // If the entity should NOT interpolate, then we set the values as is
                existingSprite.x = entity.x
                existingSprite.y = entity.y

                existingSprite.targetX = entity.x
                existingSprite.targetY = entity.y
            }
        } else {
            // Create a new sprite
            renderedSprites[entity] = RenderedSprite(entity.x, entity.y, entity.x, entity.y, zIndex, entity.width, entity.height, texture)
        }
    }

    fun tick() {
        for (entity in logic.room.entities) {
            val zIndex: Int
            val texture: Texture

            when (entity) {
                is Backdrop -> {
                    zIndex = 0
                    texture = gameResources.backdropTextureId
                }
                is Pipe -> {
                    zIndex = 2
                    texture = when (entity.type) {
                        Pipe.PipeType.TOP -> gameResources.pipeTopId
                        Pipe.PipeType.BOTTOM -> gameResources.pipeBottomId
                    }
                }
                is Ground -> {
                    zIndex = 3
                    texture = gameResources.groundId
                }
                is Furalha -> {
                    zIndex = 4
                    texture = gameResources.furalhaTextureId
                }
                is FakeFuralha -> {
                    zIndex = 4
                    texture = gameResources.furalhaTextureId
                }
                is Tap -> {
                    zIndex = 999
                    texture = gameResources.tapId
                }
                is GetReady -> {
                    zIndex = 999
                    texture = gameResources.getReadyId
                }
                is FlappyBirdLogo -> {
                    zIndex = 999
                    texture = gameResources.flappyBirdLogoId
                }
                is GameOver -> {
                    zIndex = 999
                    texture = gameResources.gameOverId
                }
                is OkButton -> {
                    zIndex = 999
                    texture = gameResources.okButtonId
                }

                is PlayButton -> {
                    zIndex = 999
                    texture = gameResources.playButtonId
                }

                is ScoreNumber.ScoreNumberZero -> {
                    zIndex = 998
                    texture = gameResources.scoreNumberZeroId
                }

                is ScoreNumber.ScoreNumberOne -> {
                    zIndex = 998
                    texture = gameResources.scoreNumberOneId
                }

                is ScoreNumber.ScoreNumberTwo -> {
                    zIndex = 998
                    texture = gameResources.scoreNumberTwoId
                }

                is ScoreNumber.ScoreNumberThree -> {
                    zIndex = 998
                    texture = gameResources.scoreNumberThreeId
                }

                is ScoreNumber.ScoreNumberFour -> {
                    zIndex = 998
                    texture = gameResources.scoreNumberFourId
                }

                is ScoreNumber.ScoreNumberFive -> {
                    zIndex = 998
                    texture = gameResources.scoreNumberFiveId
                }

                is ScoreNumber.ScoreNumberSix -> {
                    zIndex = 998
                    texture = gameResources.scoreNumberSixId
                }

                is ScoreNumber.ScoreNumberSeven -> {
                    zIndex = 998
                    texture = gameResources.scoreNumberSevenId
                }

                is ScoreNumber.ScoreNumberEight -> {
                    zIndex = 998
                    texture = gameResources.scoreNumberEightId
                }

                is ScoreNumber.ScoreNumberNine -> {
                    zIndex = 998
                    texture = gameResources.scoreNumberNineId
                }
            }

            createOrUpdateEntitySprite(
                entity,
                zIndex,
                texture,
            )
        }

        // Remove any rendered sprites that are NOT present on the game logic anymore!
        val toBeRemoved = mutableSetOf<Entity>()
        for ((entity, _) in renderedSprites) {
            val stillExists = logic.room.entities.contains(entity)
            if (!stillExists) {
                toBeRemoved.add(entity)
            }
        }

        for (remove in toBeRemoved) {
            renderedSprites.remove(remove)
        }
    }

    fun draw(interpolationPercent: Double) {
        gpu.gl.clear(GLConstants.GL_COLOR_BUFFER_BIT or GLConstants.GL_DEPTH_BUFFER_BIT)

        val sortedSprites = renderedSprites
            .values
            .sortedBy { it.zIndex }

        // TODO: Please replace this with JOML
        // The right and bottom are the window size!
        // The zNear must be smaller than 0.0, because 0.0 is where we are rendering the sprites
        // val projection = Matrix4f().ortho(0.0f, GameLogic.GAME_WIDTH.toFloat(), GameLogic.GAME_HEIGHT.toFloat(), 0.0f, -1.0f, 1.0f)
        val projection = floatArrayOf(0.013888889f, 0.0f, 0.0f, 0.0f, 0.0f, -0.0078125f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f, -1.0f, 1.0f, -0.0f, 1.0f)

        gpu.onRenderTarget(gpu.windowRenderTarget) {
            withPipeline(batcher.createSpritePipeline(shader)) {
                this.shader.uProjection.set(projection)

                batcher.drawBatch(this) {
                    for (sprite in sortedSprites) {
                        drawQuad(
                            Easings.easeLinear(
                                sprite.x.toDouble(),
                                sprite.targetX.toDouble(),
                                interpolationPercent
                            ).toFloat(),
                            Easings.easeLinear(
                                sprite.y.toDouble(),
                                sprite.targetY.toDouble(),
                                interpolationPercent
                            ).toFloat(),
                            sprite.width,
                            sprite.height,
                            0f,
                            0f,
                            1f,
                            1f,
                            sprite.texture,
                            1f,
                            1f,
                            1f,
                            1f
                        )
                    }
                }
            }
        }
    }
}