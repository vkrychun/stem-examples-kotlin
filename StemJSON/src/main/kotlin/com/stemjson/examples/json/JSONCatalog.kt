package com.stemjson.examples.json

import android.content.Context
import java.io.IOException

/**
 * Catalog of every Stem module bundled with the examples. Each entry pairs
 * a stable identifier with the metadata the home-grid tile needs:
 * `displayName`, `subtitle`, Material Symbol `iconName`, and a string
 * `iconColor` that resolves against StemJSON's color system.
 *
 * The underlying JSON file (or zip package) for each module lives at
 * `assets/json/<rawValue>.<fileExtension>`.
 */
public object JSONCatalog {

    public enum class Module(public val rawValue: String) {
        /** Three-in-one math tool: calculator, tip split, unit converter */
        CALCULATOR("calculator"),
        /** Chat app with real-time messaging */
        MESSENGER("messenger"),
        /** Live weather search and forecasts via Open-Meteo */
        WEATHER("weather"),
        /** Product catalog with remote API, local persistence & search */
        SHOP("shop"),
        /** Photo gallery with device library access, grid layout. */
        GALLERY("gallery"),
        /** Multi-file zip recipe book: pdf, media, file:// nav, EN/UK l10n. */
        RECIPES("recipes"),
        /** Restaurant reservation form (form, section, datePicker, alert). */
        BOOKING("booking"),
        /** Developer reference: every component type + its loading state. */
        SKELETONS("skeletons"),
        /** Instagram-like feed — zip + bundled asset + l10n + Pexels API. */
        INSTAGRAM("instagram"),
        /** Live reference for every built-in expression function (§8). */
        FUNCTIONS("functions"),
        /** Live readout of every #{…} system literal (§6.3). */
        SYSTEM("system"),
        /** Heavy-media: video, pdf, map, universal `media`. */
        MEDIA("media"),
        /** Two-column splitview (§4.5). */
        SPLITVIEW("splitview"),
        /** Absolute smallest "Hello, name" module (under 30 lines). */
        HELLO("hello");

        /** `.zip` for packaged modules with their own dependencies / l10n. */
        public val fileExtension: String
            get() = when (this) {
                INSTAGRAM, RECIPES -> "zip"
                else -> "json"
            }

        public val displayName: String
            get() = when (this) {
                CALCULATOR -> "Smart Calculator"
                MESSENGER -> "Messenger"
                WEATHER -> "Weather"
                SHOP -> "Shop"
                GALLERY -> "Gallery"
                RECIPES -> "Recipes"
                BOOKING -> "Booking"
                SKELETONS -> "Component Reference"
                INSTAGRAM -> "Instagram"
                FUNCTIONS -> "Functions"
                SYSTEM -> "System Literals"
                MEDIA -> "Media Showcase"
                SPLITVIEW -> "Split View"
                HELLO -> "Hello"
            }

        /** Material Symbols name rendered on the home-grid tile. */
        public val iconName: String
            get() = when (this) {
                CALCULATOR -> "calculate"
                MESSENGER -> "forum"
                WEATHER -> "wb_cloudy"
                SHOP -> "shopping_bag"
                GALLERY -> "photo_library"
                RECIPES -> "menu_book"
                BOOKING -> "event"
                SKELETONS -> "view_quilt"
                INSTAGRAM -> "camera"
                FUNCTIONS -> "functions"
                SYSTEM -> "smartphone"
                MEDIA -> "play_circle"
                SPLITVIEW -> "splitscreen"
                HELLO -> "waving_hand"
            }

        public val iconColor: String
            get() = when (this) {
                CALCULATOR -> "orange"
                MESSENGER -> "teal"
                WEATHER -> "blue"
                SHOP -> "green"
                GALLERY -> "mint"
                RECIPES -> "brown"
                BOOKING -> "indigo"
                SKELETONS -> "purple"
                INSTAGRAM -> "pink"
                FUNCTIONS -> "cyan"
                SYSTEM -> "gray"
                MEDIA -> "red"
                SPLITVIEW -> "blue"
                HELLO -> "yellow"
            }

        public val subtitle: String
            get() = when (this) {
                CALCULATOR -> "Calculator, tip split & unit converter"
                MESSENGER -> "Real-time chat with media"
                WEATHER -> "Live forecasts with Open-Meteo API"
                SHOP -> "Product catalog with search & persistence"
                GALLERY -> "Grid layout, photos repo, sort & format"
                RECIPES -> "Multi-file zip with pdf, media & file:// nav"
                BOOKING -> "Form, datePicker, toggle & alert modal"
                SKELETONS -> "Developer reference for every component and its loading state"
                INSTAGRAM -> "Zip + bundled asset + localization + API"
                FUNCTIONS -> "Every expression function, live"
                SYSTEM -> "Every #{} system literal, live"
                MEDIA -> "video, pdf, map & universal media"
                SPLITVIEW -> "Two-column tablet layout"
                HELLO -> "The smallest possible module"
            }

        public companion object {
            /** Modules displayed on the home grid, in display order. */
            public val displayOrder: List<Module> = listOf(
                HELLO, INSTAGRAM, CALCULATOR, MESSENGER, WEATHER, SHOP, GALLERY,
                RECIPES, BOOKING, SKELETONS,
                FUNCTIONS, SYSTEM, MEDIA, SPLITVIEW,
            )
        }
    }

    /**
     * Reads the JSON / zip bytes for [module] from the StemJSON assets.
     * Throws [CatalogError.ModuleNotFound] if the asset is missing — which
     * should only happen if [Module.rawValue] is out of sync with the
     * bundled file names.
     */
    @Throws(CatalogError.ModuleNotFound::class, IOException::class)
    public fun data(context: Context, module: Module): ByteArray {
        val path = "json/${module.rawValue}.${module.fileExtension}"
        return try {
            context.assets.open(path).use { it.readBytes() }
        } catch (e: IOException) {
            throw CatalogError.ModuleNotFound(module.rawValue)
        }
    }

    public sealed class CatalogError(message: String) : Exception(message) {
        public class ModuleNotFound(public val name: String) :
            CatalogError("Module not found in StemJSON assets: $name")
    }
}
