package com.bignerdranch.android.a7individdibi.database

import androidx.room.TypeConverter

class Converters {

    @TypeConverter
    fun fromString(value: String?): List<Int> {
        if (value.isNullOrEmpty()) {
            return emptyList()
        }
        return value.split(",").map { it.trim().toInt() }
    }

    @TypeConverter
    fun fromList(list: List<Int>?): String {
        if (list.isNullOrEmpty()) {
            return ""
        }
        return list.joinToString(",")
    }
}