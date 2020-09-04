<p align="center">
  <a target="_blank" rel="noopener noreferrer" href="https://www.software-challenge.de"><img width="128" src="https://raw.githubusercontent.com/CAU-Kiel-Tech-Inf/socha-gui/master/assets/build-resources/icon.png" alt="Software-Challenge Germany logo"></a>
</p>

<p align="center">
  <a href="https://travis-ci.com/CAU-Kiel-Tech-Inf/gui" rel="nofollow"><img src="https://travis-ci.com/CAU-Kiel-Tech-Inf/gui.svg?branch=master" alt="Build Status"></a>
</p>

<h1 align="center">GUI für die Software-Challenge Germany</h1>

## Getting Started
Klone das Repository und führe den Befehl `gradlew run` aus.

## Vorraussetzungen
Für die kompilierte Version wird eine JRE mindestens Java 11 vorrausgesetzt.
Zur Entwicklung wird ein JDK von Version 11 oder höher benötigt.

Zur Zeit muss sich das Server-Projekt "neben" diesem Projekt (also im selben Elternverzeichnis) in einem Verzeichnis mit dem Namen "server" befinden.

Ausserdem muss das Server-Projekt einmal erfolgreich gebaut werden, damit die Jars vorhanden sind (auch Abhaengigkeiten wie XStream).

Das aktuelle Spiel-Plugin als JAR muss im Unterverzeichnis `plugins` liegen.

## Collaboration

Unsere Commit-Messages folgen dem Muster `type(scope): summary` (siehe [Karma Runner Konvention](http://karma-runner.github.io/latest/dev/git-commit-msg.html)), wobei die verfügbaren Scopes in [.dev/scopes.txt](.dev/scopes.txt) definiert werden. Bitte führe nach dem Klonen des Repository's einmal Folgendes im Terminal aus, damit die entsprechenden Git-Hooks aktiv werden:  

    git config core.hooksPath .dev/githooks

Um bei den Branches die Übersicht zu behalten, sollten diese ebenfalls nach der Konvention benannt werden - z.B. könnte ein Branch mit einem Release-Fix für Gradle `fix/gradle-release` heißen und ein Branch, der ein neues Login-Feature zur GUI hinzufügt, `feat/gui-login`.  
Branches werden normalerweise beim Mergen zu einem einzelnen Commit zusammengefügt (Squash-Merge), es sei denn, die einzelnen Commits des Branches haben jeweils eine alleinstehende Aussagekraft.

Detaillierte Informationen zu unserem Kollaborations-Stil findet ihr in der [Kull Konvention](https://xerus2000.github.io/kull).
