package net.perfectdreams.flappyfuralha.game.messages

sealed class GameMessage {
    data object GameStarted : GameMessage()
}