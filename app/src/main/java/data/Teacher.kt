package com.bignerdranch.android.a7individdibi.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "teachers")
data class Teacher(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fullName: String,
    val totalHoursPerYear: Int,
    val specialtyIds: List<Int>
)