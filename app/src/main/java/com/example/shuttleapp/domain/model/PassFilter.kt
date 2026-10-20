package com.example.shuttleapp.domain.model

enum class PassFilter {
    All, Late, NotSynced;

    companion object {
        /** Reads a navigation argument; anything unknown or missing shows all passes. */
        fun fromRoute(value: String?): PassFilter = entries.firstOrNull { it.name == value } ?: All
    }
}
