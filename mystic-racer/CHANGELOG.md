# Changelog

Formát: [verze] – datum. Při každé větší změně zvyš `versionName` a `versionCode` v `app/build.gradle.kts` a doplň záznam sem.

## [0.1.0] – 2026-09-29

První hratelná verze.

### Přidáno
- 10 levelů v 5 biomech (Nebula Drift, Ancient Ruins, Void Rift, Crystal Caverns, Dark Star), po dvou levelech na biom, s rostoucí obtížností.
- Top-down sci-fi jízda: loď na neonové trati, ovládání relativním tažením prstem, nebo dvěma tlačítky (nastavení).
- Prvky trati: asteroidy (i houpavé), zdi s mezerou, krystaly, urychlovací brány, temné portály (obrátí ovládání), gravitační anomálie a mlha.
- Tři štíty, hvězdy za level (dokončení, ≥ 60 % krystalů, max. jeden zásah), odemykání levelů podle postupu.
- Menu, výběr levelu, pauza, obrazovka výsledků, nastavení (ovládání tlačítky, vibrace).
- Ukládání postupu a nastavení (SharedPreferences).
- Deterministický generátor levelů; každý level je vždy průjezdný (bezpečný koridor).
- Unit test: bot simuluje všech 10 levelů bez zásahu.
- GitHub Actions workflow, který sestaví `app-debug.apk`.

### Zatím není
- Zvuk a hudba, reklamy a nákupy, zbylých 30 levelů z návrhu (celkem 40), rozdělení do modulů `:core`, `:domain`, `:data`, `:feature:*`.
