package com.cacaosd.droidmind.data_local.appdatabase

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.cacaosd.droidmind.core.config.AppConfigManager
import kotlinx.coroutines.Dispatchers

actual fun provideAppDatabase(appConfigManager: AppConfigManager): AppDatabase {
    val dbFile = appConfigManager.getStorageFile(AppDatabase.DB_NAME)
    return Room.databaseBuilder<AppDatabase>(
        name = dbFile.toAbsolutePath().toString(),
    )
        .fallbackToDestructiveMigration(false)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}
