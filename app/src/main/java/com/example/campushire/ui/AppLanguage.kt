package com.example.campushire.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

/** The selected language is available to all Compose screens. */
val LocalCampusHireLanguage = staticCompositionLocalOf { "English" }

@Composable
fun localized(english: String): String {
    val language = LocalCampusHireLanguage.current
    return when (language) {
        "Afrikaans" -> afrikaans[english] ?: english
        "isiZulu" -> isiZulu[english] ?: english
        else -> english
    }
}

private val afrikaans = mapOf(
    "Home" to "Tuis", "Jobs" to "Werk", "My Applications" to "My Aansoeke", "Saved Jobs" to "Gestoorde Werk",
    "My Profile" to "My Profiel", "Settings" to "Instellings", "Resources" to "Hulpbronne", "Notifications" to "Kennisgewings",
    "Filters" to "Filters", "Job Details" to "Werkbesonderhede", "Apply" to "Doen aansoek", "Edit Profile" to "Wysig profiel",
    "Account" to "Rekening", "Preferences" to "Voorkeure", "Accessibility" to "Toeganklikheid", "Information" to "Inligting",
    "Language" to "Taal", "Dark Mode" to "Donker modus", "Notifications" to "Kennisgewings", "Text Size" to "Teksgrootte",
    "High Contrast" to "Hoë kontras", "Logout" to "Meld af", "Career Resources" to "Loopbaanhulpbronne",
    "CV Resources" to "CV-hulpbronne", "Interview Resources" to "Onderhoudhulpbronne", "Work-Study Resources" to "Werk-en-studie-hulpbronne"
)

private val isiZulu = mapOf(
    "Home" to "Ekhaya", "Jobs" to "Imisebenzi", "My Applications" to "Izicelo zami", "Saved Jobs" to "Imisebenzi elondoloziwe",
    "My Profile" to "Iphrofayela yami", "Settings" to "Izilungiselelo", "Resources" to "Izinsiza", "Notifications" to "Izaziso",
    "Filters" to "Izihlungi", "Job Details" to "Imininingwane yomsebenzi", "Apply" to "Faka isicelo", "Edit Profile" to "Hlela iphrofayela",
    "Account" to "I-akhawunti", "Preferences" to "Okuthandwayo", "Accessibility" to "Ukufinyeleleka", "Information" to "Ulwazi",
    "Language" to "Ulimi", "Dark Mode" to "Imodi emnyama", "Text Size" to "Usayizi wombhalo",
    "High Contrast" to "Umehluko ophezulu", "Logout" to "Phuma", "Career Resources" to "Izinsiza zomsebenzi",
    "CV Resources" to "Izinsiza ze-CV", "Interview Resources" to "Izinsiza zenhlolokhono", "Work-Study Resources" to "Izinsiza zomsebenzi nesikole"
)

