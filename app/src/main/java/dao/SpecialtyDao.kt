package com.bignerdranch.android.a7individdibi.dao

import androidx.room.*
import com.bignerdranch.android.a7individdibi.data.Specialty
import kotlinx.coroutines.flow.Flow

@Dao
interface SpecialtyDao {
    @Query("SELECT * FROM specialties")
    fun getAllSpecialties(): Flow<List<Specialty>>


    @Query("SELECT * FROM specialties")
    suspend fun getAllSpecialtiesDirect(): List<Specialty>


    @Query("SELECT * FROM specialties WHERE id = :id")
    fun getSpecialtyById(id: Int): Flow<Specialty>

    @Insert
    suspend fun insertSpecialty(specialty: Specialty)

    @Update
    suspend fun updateSpecialty(specialty: Specialty)
}