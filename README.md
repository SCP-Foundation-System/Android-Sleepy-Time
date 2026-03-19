# Android-Sleepy-Time

Android-Sleepy-Time ist als moderne, lokal nutzbare Traumtagebuch- und Traumanalyse-App geplant.
Dieses Repository enthält in der ersten Phase ein solides Android-Grundgerüst mit Jetpack Compose,
einem mehrstufigen Setup-Fluss und einer vorbereiteten Hauptnavigation.

## Enthalten in dieser ersten Phase
- Kotlin + Jetpack Compose Grundprojekt
- Setup / Ersteinrichtung mit den Schritten Name, Design und Import
- persistente Grundeinstellungen über DataStore
- Haupt-App mit Bottom Navigation für Erfassen, Analyse und Träume
- Platzhalter-Struktur für spätere Features wie Import/Export, Analyse und Traumverwaltung

## Langfristige Ausrichtung
Die App soll schrittweise zu einer eigenständigen, sauberen und testbaren Basis für Traumtagebuch,
Traumanalyse, Backups und spätere Statistik-/Auswertungsfunktionen ausgebaut werden.


## GitHub Actions
Nach einem Push oder Pull Request baut GitHub Actions automatisch eine Debug-APK und stellt sie als Artifact `sleepytime-debug-apk` bereit. Die CI richtet die benötigte Gradle-Version selbst ein, sodass kein binärer `gradle-wrapper.jar` im Repository nötig ist.

## Lizenzhinweis
Copyright (c) 2026 Mika Harberts

All rights reserved.

This project is provided for viewing purposes only.
No permission is granted to use, copy, modify, distribute, or reproduce any part of this code without explicit written permission.
