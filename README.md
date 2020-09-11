# <a target="_blank" rel="noopener noreferrer" href="https://www.software-challenge.de"><img width="64" src="https://raw.githubusercontent.com/CAU-Kiel-Tech-Inf/socha-gui/master/assets/build-resources/icon.png" alt="Software-Challenge Germany logo"></a> GUI für die Software-Challenge Germany [![Build Status](https://travis-ci.com/CAU-Kiel-Tech-Inf/gui.svg?branch=master)](https://travis-ci.com/CAU-Kiel-Tech-Inf/gui)

## Getting Started
- repository klonen
- das [Server-Projekt](https://github.com/CAU-Kiel-Tech-Inf/server) muss sich aktuell im selben Überverzeichnis wie dieses Projekt befinden (`../server`) und die Jar-Artefakte dort müssen gebaut sein (inklusive plugin)
- Java 11 wird benötigt (ggf. `org.gradle.java.home=/path/to/jdk` in `gradle.properties` setzen)
- `./gradlew run` ausführen

## Kollaboration

Unsere Commit-Messages folgen dem Muster `type(scope): summary` (siehe [Karma Runner Konvention](http://karma-runner.github.io/latest/dev/git-commit-msg.html)), wobei die verfügbaren Scopes in [.dev/scopes.txt](.dev/scopes.txt) definiert werden. Bitte führe nach dem Klonen des Repository's einmal Folgendes im Terminal aus, damit die entsprechenden Git-Hooks aktiv werden:  

    git config core.hooksPath .dev/githooks

Um bei den Branches die Übersicht zu behalten, sollten diese ebenfalls nach der Konvention benannt werden - z.B. könnte ein Branch mit einem Release-Fix für Gradle `fix/gradle-release` heißen und ein Branch, der ein neues Login-Feature zur GUI hinzufügt, `feat/gui-login`.  
Branches werden normalerweise beim Mergen zu einem einzelnen Commit zusammengefügt (Squash-Merge), es sei denn, die einzelnen Commits des Branches haben jeweils eine alleinstehende Aussagekraft.

Detaillierte Informationen zu unserem Kollaborations-Stil findet ihr in der [Kull Konvention](https://xerus2000.github.io/kull).
