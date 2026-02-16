package com.bignerdranch.android.a7individdibi.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "specialties")
data class Specialty(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val maxHours: Int? = null
)