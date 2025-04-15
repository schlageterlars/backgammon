package de.htwg.se.backgammon.model.strategy

import de.htwg.se.backgammon.model.IGame
import de.htwg.se.backgammon.model.IMove
import de.htwg.se.backgammon.model.IField
import de.htwg.se.backgammon.model.Player
import de.htwg.se.backgammon.model.base.Game
import de.htwg.se.backgammon.model.base.Move
import de.htwg.se.backgammon.model.base.BearInMove
import de.htwg.se.backgammon.model.base.BearOffMove

object MoveStrategy {
  def apply(game: IGame, move: IMove, to: Int): IMoveStrategy = move match {
    case move: BearInMove => {
      if game(to) isNotOccupiedBy move.player then
        new BearInMoveStrategy(game, move.player, to)
          with AttackMoveStrategy(game, move.player, to)
      else
        new BearInMoveStrategy(game, move.player, to)
          with PeacefulMoveStrategy(game, move.player, to)
    }
    case move: BearOffMove => {
      if (move.movesToField(game)) then
        apply(game, Move(move.from, move.steps), to)
      else new BearOffMoveStrategy(game, move.from, to)
    }
    case Move(from, steps) => {
      if game(from) hasDifferentOccupierThen game(to) then
        new DefaultMoveStrategy(game, from, to)
          with AttackMoveStrategy(game, game(from).occupier, to)
      else
        new DefaultMoveStrategy(game, from, to)
          with PeacefulMoveStrategy(game, game(from).occupier, to)
    }
  }
}

trait IMoveStrategy(var game: IGame) {
  def execute(): IGame

  def set(change: (Int, IField)): IGame = set(Map(change))

  def set(changes: Map[Int, IField]): IGame = {
    game = new Game(
      List.tabulate(game.length)(i => changes.getOrElse(i, game(i))),
      barWhite = game.barWhite,
      barBlack = game.barBlack
    ); game
  }

  def set(
      barWhite: Int = game.barWhite,
      barBlack: Int = game.barBlack
  ): IGame = {
    game = new Game(game.fields, barWhite, barBlack); game
  }
}

abstract class MoveCheckersStrategy(game: IGame) extends IMoveStrategy(game) {

  def execute(): IGame = {
    placeCheckers; pickUpCheckers
  }

  def placeCheckers: IGame

  def pickUpCheckers: IGame

  trait Bar { def ++ : IGame; def -- : IGame }
  def bar(player: Player): Bar = new Bar {
    private def add(num: Int) = player match {
      case Player.White => set(barWhite = game.barWhite + num)
      case _            => set(barBlack = game.barBlack + num)
    }
    def ++ : IGame = add(1); def -- : IGame = add(-1)
  }
}
