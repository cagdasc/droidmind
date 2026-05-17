package com.cacaosd.droidmind.data_local.appdatabase.converter

import androidx.room.TypeConverter
import java.util.*

class UuidConverter {
    @TypeConverter
    fun fromUUID(uuid: UUID): String {
        return uuid.toString()
    }

    @TypeConverter
    fun toUUID(uuid: String): UUID {
        return UUID.fromString(uuid)
    }
}
