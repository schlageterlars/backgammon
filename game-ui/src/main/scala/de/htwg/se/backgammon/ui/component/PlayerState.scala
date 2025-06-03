package de.htwg.se.backgammon.ui.component

import scalafx.scene.Group
import scalafx.scene.shape.Rectangle
import scalafx.scene.paint.Color
import scalafx.scene.text.Text
import scalafx.scene.text.Font
import de.htwg.se.backgammon.core.Player
import de.htwg.se.backgammon.ui.component.configuration.PlayerConfiguration

val MARGIN_TOP = 25
val PLAYER_BOX_WIDTH = 50
val CORNER_RADIUS = 5

class PlayerState(using cf: PlayerConfiguration) extends Group {
  case class Entry(player: Player, y: Double, color: Color)
  var emojis: Map[Player, Text] = Map()

  {
    val players = List(
      Entry(Player.Black, cf.marginTop, cf.colors.checkersBlack),
      Entry(Player.White, cf.yCoordWhite, cf.colors.checkersWhite)
    )

    players.foreach(entry => {
      create(entry.y, entry.color); createEmoji(entry.y, entry.player)
    })

    set(Player.White)
  }

  def set(player: Player): Unit =
    emojis.foreach(emoji => emoji._2.setVisible(emoji._1 == player))

  def set(player: Player, emoji: String): Unit = {
    emojis(player).setText(emoji); set(player)
  }

  def add(children: Seq[Rectangle]) =
    children.foreach(c => this.children.add(c))

  def createEmoji(_y: Double, player: Player) = {
    val emoji = new Text {
      font = Font.font("Arial", 30)
      text = "X" //"🎲"
      x = cf.xCoord + 10
      y = _y + +PLAYER_BOX_WIDTH / 2 + 10
      smooth = true
    }
    children.add(emoji)
    emojis = emojis ++ Map(player -> emoji)
  }

  def create(_y: Double, color: Color) = {
    val rectangle = new Rectangle {
      fill = color
      x = cf.xCoord
      y = _y
      width = PLAYER_BOX_WIDTH
      height = PLAYER_BOX_WIDTH
      arcWidth = CORNER_RADIUS
      arcHeight = CORNER_RADIUS
    }
    val border = new Rectangle {
      stroke = Color.rgb(1, 25, 54)
      fill = null
      x = cf.xCoord
      y = _y
      width = PLAYER_BOX_WIDTH
      height = PLAYER_BOX_WIDTH
      arcWidth = CORNER_RADIUS
      arcHeight = CORNER_RADIUS
    }
    add(Seq(rectangle, border))
  }
}
