rootProject.name = "software-challenge-gui"

includeBuild("backend/gradle/custom-tasks")
includeBuild("backend") {
    // https://publicobject.com/2021/03/11/includebuild
    dependencySubstitution {
        substitute(module("software-challenge:plugin"))
                .with(project(":plugin"))
        substitute(module("software-challenge:server"))
                .with(project(":server"))
    }
}