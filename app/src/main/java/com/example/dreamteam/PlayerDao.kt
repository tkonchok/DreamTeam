package com.example.dreamteam

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PlayerDao {
    @Query("SELECT * FROM players WHERE rating BETWEEN :min AND :max ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomPlayer(min: Int, max: Int): PlayerEntity?

    @Query("SELECT * FROM players WHERE packType = :pack AND rating BETWEEN :min AND :max ORDER BY RANDOM() LIMIT 1")
    suspend fun getPackPlayer(pack: String, min: Int, max: Int): PlayerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(players: List<PlayerEntity>)
    
    @Query("SELECT COUNT(*) FROM players")
    suspend fun getCount(): Int

    @Query("SELECT * FROM players WHERE name LIKE '%' || :query || '%' OR club LIKE '%' || :query || '%' OR league LIKE '%' || :query || '%' LIMIT 50")
    suspend fun searchPlayers(query: String): List<PlayerEntity>

    @Query("SELECT * FROM players ORDER BY rating DESC LIMIT 50")
    suspend fun getTopPlayers(): List<PlayerEntity>

    @Query("UPDATE players SET imageUrl = :url WHERE id = :id")
    suspend fun updatePlayerImage(id: Int, url: String)
}
