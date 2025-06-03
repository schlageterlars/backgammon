package de.htwg.se.backgammon.ui.component

import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.geometry._
import scalafx.stage._
import scalafx.Includes._

import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.geometry._
import scalafx.stage._
import scalafx.Includes._
import javafx.util.Callback

object BackgammonNicknameDialog {

  def getNickname(): Option[String] = {
    val dialog = new Dialog[String]() {
      title = "🎲 Willkommen zum Backgammon-Duell!"
      headerText = "Ein neuer Herausforderer betritt das Spielfeld…"
      graphic = new Label("🎲🎲") {
        style = "-fx-font-size: 32px;"
      }
    }

    val loginButtonType = new ButtonType("Los geht’s!", ButtonBar.ButtonData.OKDone)
    dialog.dialogPane().buttonTypes = Seq(loginButtonType, ButtonType.Cancel)

    val nicknameField = new TextField() {
      promptText = "Dein ehrenvoller Nickname…"
    }

    val content = new GridPane {
      hgap = 10
      vgap = 10
      padding = Insets(20, 150, 10, 10)

      add(new Label("Wie sollen deine Gegner dich fürchten?"), 0, 0)
      add(nicknameField, 0, 1)
    }

    dialog.dialogPane().content = content

    val loginButton = dialog.dialogPane().lookupButton(loginButtonType)
    loginButton.disable <== nicknameField.text.isEmpty

    // Expliziter resultConverter
    dialog.resultConverter = new Callback[ButtonType, String] {
      override def call(button: ButtonType): String = {
        if (button == loginButtonType) nicknameField.text()
        else null
      }
    }

    dialog.showAndWait()
    return Some(nicknameField.text())
  }
}
