package com.example.dreamteam

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * These classes represent the "JSON" structure that TheSportsDB sends back.
 */
data class PlayerResponse(val player: List<SportsDbPlayer>?)
data class SportsDbPlayer(
    val idPlayer: String,
    val strPlayer: String,
    val strTeam: String,
    val strPosition: String?,
    val strCutout: String?,
    val strThumb: String?,
    val strNationality: String
)

/**
 * Wikipedia API response structures
 */
data class WikiResponse(
    val thumbnail: WikiImage?,
    val originalimage: WikiImage?
)
data class WikiImage(val source: String)

/**
 * Interface for TheSportsDB API
 */
interface TheSportsDbApi {
    @GET("searchplayers.php")
    suspend fun searchPlayer(@Query("p") playerName: String): PlayerResponse
}

/**
 * Interface for Wikipedia API
 */
interface WikipediaApi {
    @GET("page/summary/{title}")
    suspend fun getPageSummary(@Path("title") title: String): WikiResponse
}

/**
 * Singleton object to manage network connections.
 */
object RetrofitClient {
    private const val SPORTSDB_BASE_URL = "https://www.thesportsdb.com/api/v1/json/3/"
    private const val WIKIPEDIA_BASE_URL = "https://en.wikipedia.org/api/rest_v1/"

    val api: TheSportsDbApi by lazy {
        Retrofit.Builder()
            .baseUrl(SPORTSDB_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TheSportsDbApi::class.java)
    }

    val wikiApi: WikipediaApi by lazy {
        Retrofit.Builder()
            .baseUrl(WIKIPEDIA_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WikipediaApi::class.java)
    }
}
