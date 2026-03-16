package com.example.dreamteam

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Handles persistent storage for player profile, collection, and squads using SharedPreferences and Gson.
 */
class UserStorage(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("dream_team_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveProfile(profile: PlayerProfile) {
        val json = gson.toJson(profile)
        prefs.edit().putString("player_profile", json).apply()
    }

    fun getProfile(): PlayerProfile? {
        return try {
            val json = prefs.getString("player_profile", null) ?: return null
            val profile = gson.fromJson(json, PlayerProfile::class.java)
            
            val (newEnergy, newTime) = profile.getUpdatedEnergy()
            if (newEnergy != profile.energy) {
                val updatedProfile = profile.copy(energy = newEnergy, lastEnergyRefillTime = newTime)
                saveProfile(updatedProfile)
                return updatedProfile
            }
            profile
        } catch (e: Exception) {
            null
        }
    }

    fun saveCollection(collection: List<SoccerPlayer>) {
        val json = gson.toJson(collection)
        prefs.edit().putString("player_collection", json).apply()
    }

    fun getCollection(): List<SoccerPlayer> {
        return try {
            val json = prefs.getString("player_collection", null) ?: return emptyList()
            val type = object : TypeToken<List<SoccerPlayer>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveSquads(squads: List<Squad>) {
        val json = gson.toJson(squads)
        prefs.edit().putString("saved_squads", json).apply()
    }

    fun getSquads(): List<Squad> {
        return try {
            val json = prefs.getString("saved_squads", null) ?: return emptyList()
            val type = object : TypeToken<List<Squad>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }
}
