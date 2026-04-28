package com.cacaosd.droidmind.data_local.appdatabase.converter

import androidx.room.TypeConverter
import java.time.Instant

class InstantConverter {
    @TypeConverter
    fun toInstant(value: Long): Instant {
        return Instant.ofEpochMilli(value)
    }

    @TypeConverter
    fun toLong(instant: Instant): Long {
        return instant.toEpochMilli()
    }
}
