package com.everlauncher.domain.model

import java.util.UUID

/**
 * Represents a launchable app assigned to one or more focus modes.
 * On Android, apps are identified by packageName. iconData is not stored —
 * icons are loaded at runtime via PackageManager.
 */
data class AppItem(
    val id: String = UUID.randomUUID().toString(),
    val displayName: String,
    val packageName: String,
    val isSystemApp: Boolean = false,
    val isGated: Boolean = false,
    val gateType: GateType? = null,
    /** Position within its mode's app list (0-indexed) */
    val sortOrder: Int = 0
) {
    init {
        require(displayName.isNotBlank()) { "displayName must not be blank" }
        require(packageName.isNotBlank()) { "packageName must not be blank" }
    }
}
