# <a target="_blank" rel="noopener noreferrer" href="https://www.software-challenge.de"><img width="64" src="https://raw.githubusercontent.com/CAU-Kiel-Tech-Inf/socha-gui/master/assets/build-resources/icon.png" alt="Software-Challenge Germany logo"></a> GUI für die Software-Challenge Germany ![.github/workflows/gradle.yml](https://github.com/CAU-Kiel-Tech-Inf/gui/workflows/.github/workflows/gradle.yml/badge.svg)

## Getting Started
- repository mit submodulen klonen:
  ```sh
  git clone https://github.com/CAU-Kiel-Tech-Inf/gui.git --recurse-submodules --shallow-submodules
  ```
- Java 11 wird benötigt (ggf. `org.gradle.java.home=/path/to/jdk` in `gradle.properties` setzen)
- `./gradlew run` ausführen

## Kollaboration

Unsere Commit-Messages folgen dem Muster `type(scope): summary` (siehe [Karma Runner Konvention](http://karma-runner.github.io/latest/dev/git-commit-msg.html)), wobei die verfügbaren Scopes in [.dev/scopes.txt](.dev/scopes.txt) definiert werden. Bitte führe nach dem Klonen des Repository’s Folgendes im Terminal aus, damit die entsprechenden Git-Hooks aktiv werden:

    git config core.hooksPath .dev/githooks

Um bei den Branches die Übersicht zu behalten, sollten diese ebenfalls nach der Konvention benannt werden – z. B. könnte ein Branch mit einem Release-Fix für Gradle `fix/gradle-release` heißen und ein Branch, der ein neues Login-Feature zur GUI hinzufügt, `feat/gui-login`.
Branches werden normalerweise beim Mergen zu einem einzelnen Commit zusammengefügt (Squash-Merge), es sei denn, die einzelnen Commits des Branches haben jeweils eine alleinstehende Aussagekraft.

Detaillierte Informationen zu unserem Kollaborations-Stil findet ihr in der [Kull Konvention](https://xerus2000.github.io/kull).
