# <a target="_blank" rel="noopener noreferrer" href="https://www.software-challenge.de"><img width="128" src="https://software-challenge.de/site/themes/freebird/img/logo.png" alt="Software-Challenge Logo"></a> Grafischer Spieleserver der Software-Challenge Germany ![.github/workflows/gradle.yml](https://github.com/software-challenge/gui/workflows/.github/workflows/gradle.yml/badge.svg)

Dies ist die Grafische Oberfläche für die Software-Challenge Germany.
Nutzerdokumentation findet sich in https://docs.software-challenge.de/server.html

> Hinweis: Wenn als erster Parameter des Programms eine Zahl mitgegeben wird,
wird der Server auf diesem Port Verbindungen von Spielern erwarten.

# Für Entwickler

## Erste Schritte

- zuerst das Projekt lokal mit Submodulen klonen:
  ```sh
  git clone https://github.com/software-challenge/gui.git --recurse-submodules --shallow-submodules
  ```
- mindestens Java 11 wird benötigt (ggf. `org.gradle.java.home=/path/to/jdk` in `gradle.properties` setzen)
- `./gradlew run` ausführen

## Kollaboration

Unsere Commit-Messages folgen dem Muster `type(scope): summary`
(siehe [Karma Runner Konvention](http://karma-runner.github.io/6.2/dev/git-commit-msg.html)),
wobei die gängigen Scopes in [.dev/scopes.txt](.dev/scopes.txt) definiert werden.
Nach dem Klonen mit git sollte dazu der hook aktiviert werden:

    git config core.hooksPath .dev/githooks

Um bei den Branches die Übersicht zu behalten,
sollten diese ebenfalls nach der Konvention benannt werden,
z. B. könnte ein Branch mit einem Release-Fix für Gradle `chore/gradle/release-fix` heißen
und ein Branch, der ein neues Login-Feature zur GUI hinzufügt, `feat/gui-login`.

Wenn die einzelnen Commits eines Pull Requests eigenständig funktionieren,
sollte ein rebase merge durchgeführt werden,
ansonsten (gerade bei experimentier-Branches) ein squash merge,
wobei der Titel des Pull Requests der Commit-Message entsprechen sollte.

Detaillierte Informationen zu unserem Kollaborations-Stil
findet ihr in der [Kull Konvention](https://kull.jfischer.org).
