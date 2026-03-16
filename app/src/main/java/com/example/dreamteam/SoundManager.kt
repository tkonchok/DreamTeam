package com.example.dreamteam

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Handler
import android.os.Looper
import android.util.Log

object SoundManager {
    private var soundPool: SoundPool? = null
    private val soundMap = mutableMapOf<String, Int>()
    private val loadedSounds = mutableSetOf<Int>()
    private var bgPlayer: MediaPlayer? = null
    private var currentMode: String = "none" // "playlist", "draw", or "none"
    
    private var currentSongIndex = -1
    private val playlist = mutableListOf<Int>()
    private val handler = Handler(Looper.getMainLooper())
    private val snippetDuration = 60000L // 1 minute snippet
    private var appContext: Context? = null

    private val snippetRunnable = object : Runnable {
        override fun run() {
            if (currentMode == "playlist") {
                playNextInPlaylist()
            }
        }
    }

    fun init(context: Context) {
        if (soundPool != null) return
        
        appContext = context.applicationContext
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        
        soundPool = SoundPool.Builder()
            .setMaxStreams(10)
            .setAudioAttributes(attrs)
            .build().apply {
                setOnLoadCompleteListener { _, sampleId, status ->
                    if (status == 0) loadedSounds.add(sampleId)
                }
            }

        // Map internal names to actual res/raw filenames
        val soundResources = listOf(
            "click" to "button_click",
            "zap" to "black_ball_zap",
            "stop" to "spin_stop",
            "celebration" to "celebration",
            "anticipation" to "draw_ball_loop", // Fallback for missing spin_anticipation
            "goal" to "goal_sfx",
            "saved" to "save_sfx",
            "cheer" to "cheer" // Corrected from crowd_cheer
        )

        soundResources.forEach { (name, resName) ->
            val resId = context.resources.getIdentifier(resName, "raw", context.packageName)
            if (resId != 0) {
                try {
                    val id = soundPool?.load(context, resId, 1) ?: 0
                    if (id != 0) soundMap[name] = id
                } catch (e: Exception) {
                    Log.e("SoundManager", "Error loading $name: ${e.message}")
                }
            }
        }

        // Load Background Songs
        playlist.clear()
        var i = 1
        while (true) {
            val resId = context.resources.getIdentifier("song$i", "raw", context.packageName)
            if (resId != 0) {
                playlist.add(resId)
                i++
            } else break
        }
        playlist.shuffle()
    }

    fun playSound(name: String) {
        val id = soundMap[name]
        if (id != null && id != 0) {
            // Only play if SoundPool confirms it is loaded
            if (loadedSounds.contains(id)) {
                soundPool?.play(id, 1f, 1f, 1, 0, 1f)
            } else {
                // Fallback attempt if listener hasn't fired but ID is valid
                soundPool?.play(id, 1f, 1f, 1, 0, 1f)
            }
        }
    }

    fun startPlaylist(context: Context) {
        if (currentMode == "playlist") return
        currentMode = "playlist"
        appContext = context.applicationContext
        playNextInPlaylist()
    }

    private fun playNextInPlaylist() {
        if (playlist.isEmpty()) return
        handler.removeCallbacks(snippetRunnable)
        
        val ctx = appContext ?: return
        currentSongIndex = (currentSongIndex + 1) % playlist.size
        val resId = playlist[currentSongIndex]
        
        stopCurrentBg()
        try {
            bgPlayer = MediaPlayer.create(ctx, resId).apply {
                setOnCompletionListener { playNextInPlaylist() }
                start()
            }
            handler.postDelayed(snippetRunnable, snippetDuration)
        } catch (e: Exception) {
            Log.e("SoundManager", "Playlist error: ${e.message}")
        }
    }

    fun startDrawBallMusic(context: Context) {
        if (currentMode == "draw") return
        currentMode = "draw"
        handler.removeCallbacks(snippetRunnable)
        stopCurrentBg()
        
        val resId = context.resources.getIdentifier("draw_ball_loop", "raw", context.packageName)
        if (resId != 0) {
            try {
                bgPlayer = MediaPlayer.create(context.applicationContext, resId).apply {
                    isLooping = true
                    start()
                }
            } catch (e: Exception) {
                Log.e("SoundManager", "Draw music error: ${e.message}")
            }
        }
    }

    fun stopBgMusic() {
        currentMode = "none"
        handler.removeCallbacks(snippetRunnable)
        stopCurrentBg()
    }

    private fun stopCurrentBg() {
        try {
            bgPlayer?.stop()
            bgPlayer?.release()
        } catch (e: Exception) { }
        bgPlayer = null
    }
}
