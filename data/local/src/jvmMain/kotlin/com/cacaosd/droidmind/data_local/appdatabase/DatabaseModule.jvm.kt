package com.cacaosd.droidmind.data_local.appdatabase

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.cacaosd.droidmind.core.config.AppConfigManager
import com.cacaosd.platform.coroutines.dispatchers.PlatformDispatchers

actual fun provideAppDatabase(
    appConfigManager: AppConfigManager,
    platformDispatchers: PlatformDispatchers
): AppDatabase {
    val dbFile = appConfigManager.getStorageFile(AppDatabase.DB_NAME)
    return Room.databaseBuilder<AppDatabase>(
        name = dbFile.toAbsolutePath().toString(),
    )
        .fallbackToDestructiveMigration(false)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(platformDispatchers.io)
        .build()
}
