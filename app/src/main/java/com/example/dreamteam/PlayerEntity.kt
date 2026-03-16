package com.example.dreamteam

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "players")
data class PlayerEntity(
    @PrimaryKey 
    @SerializedName("ID")
    val id: Int,
    
    @SerializedName("Name")
    val name: String,
    
    @SerializedName("OVR")
    val rating: Int,
    
    @SerializedName("Position")
    val position: String,
    
    @SerializedName("Team")
    val club: String,
    
    @SerializedName("League")
    val league: String,
    
    @SerializedName("Nation")
    val country: String,
    
    @SerializedName("card")
    val imageUrl: String,
    
    @SerializedName("PAC")
    val pace: Int,
    
    @SerializedName("SHO")
    val shooting: Int,
    
    @SerializedName("PAS")
    val passing: Int,
    
    @SerializedName("DRI")
    val dribbling: Int,
    
    @SerializedName("DEF")
    val defending: Int,
    
    @SerializedName("PHY")
    val physical: Int,
    
    val packType: String? = null
) {
    fun toSoccerPlayer() = SoccerPlayer(
        id = id,
        name = name,
        rating = rating,
        position = position,
        club = club,
        league = league,
        country = country,
        imageUrl = imageUrl,
        pace = pace,
        shooting = shooting,
        passing = passing,
        dribbling = dribbling,
        defending = defending,
        physical = physical
    )
}
