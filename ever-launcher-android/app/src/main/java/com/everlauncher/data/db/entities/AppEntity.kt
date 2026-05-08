package com.everlauncher.data.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.everlauncher.domain.model.AppItem
import com.everlauncher.domain.model.GateType

@Entity(tableName = "apps")
data class AppEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "display_name") val displayName: String,
    @ColumnInfo(name = "package_name") val packageName: String,
    @ColumnInfo(name = "is_system_app") val isSystemApp: Boolean = false,
    @ColumnInfo(name = "is_gated") val isGated: Boolean = false,
    @ColumnInfo(name = "gate_type") val gateType: GateType? = null,
    @ColumnInfo(name = "sort_order") val sortOrder: Int = 0
) {
    fun toDomain(): AppItem = AppItem(
        id = id,
        displayName = displayName,
        packageName = packageName,
        isSystemApp = isSystemApp,
        isGated = isGated,
        gateType = gateType,
        sortOrder = sortOrder
    )

    companion object {
        fun fromDomain(app: AppItem) = AppEntity(
            id = app.id,
            displayName = app.displayName,
            packageName = app.packageName,
            isSystemApp = app.isSystemApp,
            isGated = app.isGated,
            gateType = app.gateType,
            sortOrder = app.sortOrder
        )
    }
}
