package com.everlauncher.data.repository

import com.everlauncher.data.db.AppDao
import com.everlauncher.data.db.entities.AppEntity
import com.everlauncher.domain.model.AppItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AppRepository(private val appDao: AppDao) {

    fun getAllAppsFlow(): Flow<List<AppItem>> =
        appDao.getAllFlow().map { list -> list.map { it.toDomain() } }

    suspend fun getAllApps(): List<AppItem> =
        appDao.getAll().map { it.toDomain() }

    suspend fun getAppById(id: String): AppItem? =
        appDao.getById(id)?.toDomain()

    suspend fun getAppsByIds(ids: List<String>): List<AppItem> =
        appDao.getByIds(ids).map { it.toDomain() }

    suspend fun getAppByPackageName(packageName: String): AppItem? =
        appDao.getByPackageName(packageName)?.toDomain()

    suspend fun insertApp(app: AppItem) =
        appDao.insert(AppEntity.fromDomain(app))

    suspend fun insertApps(apps: List<AppItem>) =
        appDao.insertAll(apps.map { AppEntity.fromDomain(it) })

    suspend fun updateApp(app: AppItem) =
        appDao.update(AppEntity.fromDomain(app))

    suspend fun deleteApp(app: AppItem) =
        appDao.delete(AppEntity.fromDomain(app))

    suspend fun deleteAppById(id: String) =
        appDao.deleteById(id)

    suspend fun replaceAll(apps: List<AppItem>) {
        appDao.deleteAll()
        appDao.insertAll(apps.map { AppEntity.fromDomain(it) })
    }
}
