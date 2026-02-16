package com.bignerdranch.android.a7individdibi.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "students")
data class Student(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fullName: String,
    val groupName: String,
    val course: Int,
    val specialtyId: Int,
    val photoUri: String,
    val birthDate: String,
    val isBudget: Boolean
)