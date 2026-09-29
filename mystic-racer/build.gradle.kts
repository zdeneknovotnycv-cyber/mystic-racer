// Kořenový build soubor — pluginy se jen deklarují, aplikují se v modulu :app.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}
