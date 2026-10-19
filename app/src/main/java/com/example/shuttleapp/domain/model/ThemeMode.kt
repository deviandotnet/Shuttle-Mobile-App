package com.example.shuttleapp.domain.model

/** Which color theme the app uses. [System] follows the device's dark mode setting. */
enum class ThemeMode {
    System, Light, Dark;

    companion object {
        /** Parses a stored value; anything unknown or missing falls back to [System]. */
        fun fromStored(value: String?): ThemeMode = entries.firstOrNull { it.name == value } ?: System
    }
}
