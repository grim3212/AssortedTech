# Assorted Tech

Contains an assorted group of additions based around tech, machines, and logic.

Minecraft 26.2, on both NeoForge and Fabric from a single source tree. Requires
[Assorted Lib](https://github.com/AssortedMods/AssortedLib). Branches are per Minecraft version; `26.2`
is the current one.

## Issue Reporting

Please include the following

* Minecraft version
* Loader and its version — NeoForge, or Fabric Loader together with Fabric API
* Assorted Tech version
* Assorted Lib version
* The full `latest.log`, plus the crash report if the game crashed

## Building

JDK 25 and the bundled Gradle wrapper. `common/` holds the loader-agnostic code; both loader
modules compile those sources inline rather than depending on a common jar, so there is nothing to
install between them.

How the build works - the Minecraft and loader versions, the runs, the tests, publishing - lives in
[AssortedBuild](https://github.com/AssortedMods/AssortedBuild), pinned by `assortedbuild_version` in
`gradle.properties`. This repository only says what the mod is.

Assorted Lib is consumed as a Maven artifact, so publish it first:

```bash
cd ../AssortedLib && ./gradlew publishToMavenLocal
```

Then from this repository:

```bash
./gradlew build                        # every module; jars land in <module>/build/libs
./gradlew :neoforge:runClient
./gradlew :fabric:runClient
./gradlew :neoforge:runGameTestServer  # headless gametests, non-zero exit on failure
./gradlew :fabric:runGameTest
./gradlew :fabric:runClientGameTest    # client gametests; opens a real game window
./gradlew :fabric:runProductionClientGameTest   # the same tests against the built jars; what CI runs
./gradlew :neoforge:runClientData      # datagen
./gradlew :neoforge:runServerData
```

Generated resources are committed. The NeoForge datagen writes them for both loaders; they are
regenerated, never hand-edited.
`TESTING-CHECKLIST.md` lists the in-game checks the gametests cannot cover.

## License

[LGPL-3.0-only](LICENSE).
