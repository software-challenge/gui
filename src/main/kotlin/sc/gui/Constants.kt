package sc.gui

import sc.server.network.NewClientListener

const val serverAddress = "localhost"

val serverPort: Int
    get() = NewClientListener.lastUsedPort

val guide = """
        - Fahre über eine Figur, um ihre möglichen Züge zu sehen
        - Klicke eine Figur und dann das Zielfeld an, um sie zu bewegen
        - Durch ein erneutes Klicken auf die Figur kannst du sie wieder abwählen
    """.trimIndent()

val guideMq = """
        Zur Bedienung von Mississippi Queen:
        - Das Label "S" an den Schiffen ist die aktuelle Geschwindigkeit
        - Das Label "M" am aktuellen Schiff sind die offenen Bewegungspunkte
          (Bei Bestätigung des Zuges wird aus diesen automatisch die nötige Beschleunigungsaktion berechnet)
        - Optional kann man am Beginn des Zuges manuell über die Knöpfe + und - beschleunigen.
        - Bewegungen menschlicher Spieler können über die Knöpfe oder die korrespondierenden Buchstabentasten erfolgen
        - Mit der Taste "S" wird der aktuelle Zug abgeschickt, mit "C" zurückgesetzt
        - Kohle wird automatisch abgezogen anhand der Regeln
        - Die Seite mit den Regeln lässt sich oben im Menü aufrufen
        Viel Spaß!
    """.trimIndent()