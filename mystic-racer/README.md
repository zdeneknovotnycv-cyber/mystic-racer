# Sci-Fi Mystic Racer

Top-down sci-fi závodění v mystickém prostředí (Jetpack Compose, Kotlin). Verze **0.1.0**, 10 levelů. Změny viz [CHANGELOG.md](CHANGELOG.md).

## Jak získat APK

### A) GitHub Actions (bez instalace čehokoli)
1. Vytvoř nový repozitář na GitHubu a nahraj do něj obsah této složky (včetně skryté složky `.github`).
2. Otevři záložku **Actions**, workflow **Build APK** se spustí sám po nahrání (jinak *Run workflow*).
3. Po dokončení stáhni artefakt **mystic-racer-debug-apk** (uvnitř je `app-debug.apk`).
4. Zkopíruj APK do telefonu a nainstaluj (povol instalaci z neznámých zdrojů).

### B) Android Studio
1. *File → Open* a vyber tuto složku. Studio si samo stáhne Gradle a SDK 35.
2. Spusť na telefonu nebo emulátoru (zelená šipka), nebo *Build → Build APK(s)*.
3. Soubor je v `app/build/outputs/apk/debug/app-debug.apk`.

### C) Příkazový řádek
```
./gradlew assembleDebug
```
Vyžaduje JDK 17 a nainstalované Android SDK (`ANDROID_HOME`).

## Ovládání
- **Tažení prstem** kamkoli na displeji: loď se hýbe doleva a doprava (relativně k prstu).
- **Nastavení → Ovládání tlačítky**: dvě velká tlačítka vlevo/vpravo.
- Sbírej krystaly (zlaté), vyhýbej se asteroidům a zdím. Fialový portál obrátí ovládání, modrá brána zrychlí a na chvíli tě učiní nezranitelným, gravitační anomálie tě stahuje do strany.

## Struktura
Jeden modul `:app`, balíčky odpovídají vrstvám Clean Architecture, takže se dají později rozdělit do modulů:

| Balíček | Obsah |
|---|---|
| `domain` | modely, pravidla hvězd, rozhraní `ProgressRepository` (čistý Kotlin) |
| `data` | katalog 10 levelů, ukládání postupu |
| `engine` | generátor levelů a herní logika bez závislosti na Androidu |
| `ui` | Compose obrazovky, renderer hry na Canvasu, navigace |

Testy: `./gradlew testDebugUnitTest` (bot projede všech 10 levelů bez zásahu).

## Pro Google Play
Před publikací je potřeba zvýšit `targetSdk` (Play vyžaduje aktuální API úroveň), podepsat release build a doplnit ikonu, popisy a zásady ochrany soukromí.
