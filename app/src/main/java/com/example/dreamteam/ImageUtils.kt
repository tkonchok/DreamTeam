package com.example.dreamteam

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ImageFetcher {
    /**
     * Tries to find a high-quality player image from multiple sources.
     * 1. TheSportsDB (Cutout/Thumb)
     * 2. Wikipedia (Thumbnail/Original)
     */
    suspend fun fetchPlayerImage(name: String): String = withContext(Dispatchers.IO) {
        // Source 1: TheSportsDB
        try {
            val sportsResponse = RetrofitClient.api.searchPlayer(name)
            val sportsPlayer = sportsResponse.player?.firstOrNull()
            if (sportsPlayer != null) {
                val url = sportsPlayer.strCutout ?: sportsPlayer.strThumb
                if (!url.isNullOrEmpty()) return@withContext url
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Source 2: Wikipedia (Fallback)
        try {
            val wikiResponse = RetrofitClient.wikiApi.getPageSummary(name.replace(" ", "_"))
            val url = wikiResponse.originalimage?.source ?: wikiResponse.thumbnail?.source
            if (!url.isNullOrEmpty()) return@withContext url
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return@withContext ""
    }
}
