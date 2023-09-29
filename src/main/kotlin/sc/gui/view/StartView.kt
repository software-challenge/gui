package sc.gui.view

import javafx.geometry.Pos
import javafx.scene.text.TextAlignment
import sc.gui.AppStyle
import sc.gui.controller.CreateGame
import tornadofx.*

class StartView: View() {
    override val root = vbox(AppStyle.spacing, Pos.CENTER) {
        paddingAll = AppStyle.spacing
        label("Willkommen bei der Software-Challenge!") {
            addClass(AppStyle.heading)
            isWrapText = true
            textAlignment = TextAlignment.CENTER
        }
        label("""
            Zur Bedienung von Mississippi Queen:
            - Das Label "S" an den Schiffen ist die aktuelle Geschwindigkeit
            - Das Label "M" am aktuellen Schiff sind die offenen Bewegungspunkte
              (Bei Bestätigung des Zuges wird aus diesen automatisch die nötige Beschleunigungsaktion berechnet)
            - Bewegungen menschlicher Spieler können über die Knöpfe oder die korrespondierenden Buchstabentasten erfolgen
            - Mit der Taste "S" wird der aktuelle Zug abgeschickt, mit "C" zurückgesetzt
            - Kohle wird automatisch abgezogen anhand der Regeln
            - Die Seite mit den Regeln lässt sich oben im Menü aufrufen
            Viel Spaß!
            """.trimIndent())
        button("Neues Spiel starten") {
            action {
                fire(CreateGame)
            }
        }
    }
}

