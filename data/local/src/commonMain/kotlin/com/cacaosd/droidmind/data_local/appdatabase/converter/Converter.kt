package com.cacaosd.droidmind.data_local.appdatabase.converter

import androidx.room.TypeConverter


class Converter {
    @TypeConverter
    fun fromSet(value: Set<String>): String {
        return value.joinToString(separator = "-") { it }
    }

    @TypeConverter
    fun setFromString(value: String): Set<String> {
        return value.split("-").toSet()
    }
}
