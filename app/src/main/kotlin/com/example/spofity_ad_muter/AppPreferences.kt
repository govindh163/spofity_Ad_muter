package com.example.spofity_ad_muter

import android.content.Context
import android.content.SharedPreferences
import com.example.spofity_ad_muter.data.AppConfig
import com.example.spofity_ad_muter.data.MuterStatistics
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Ultra battery-efficient persistent preferences and statistical storage.
 */
class AppPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "spofity_ad_muter_prefs"
        private const val KEY_SERVICE_ACTIVE = "service_active"
        private const val KEY_UNMUTE_DELAY_MS = "unmute_delay_ms"
        private const val KEY_LOW_VOLUME_MODE = "low_volume_mode"
        private const val KEY_SHOW_NOTIFICATIONS = "show_notifications"
        private const val KEY_AUTO_START_BOOT = "auto_start_boot"
        private const val KEY_TOTAL_ADS_MUTED = "total_ads_muted"
        private const val KEY_TOTAL_SILENCE_SECONDS = "total_silence_seconds"
        private const val KEY_TODAY_ADS_MUTED = "today_ads_muted"
        private const val KEY_LAST_DATE = "last_date_recorded"
        private const val KEY_SAVED_VOLUME = "saved_media_volume"

        @Volatile
        private var instance: AppPreferences? = null

        fun getInstance(context: Context): AppPreferences {
            return instance ?: synchronized(this) {
                instance ?: AppPreferences(context).also { instance = it }
            }
        }
    }

    var isServiceActive: Boolean
        get() = prefs.getBoolean(KEY_SERVICE_ACTIVE, true)
        set(value) = prefs.edit().putBoolean(KEY_SERVICE_ACTIVE, value).apply()

    var unmuteDelayMs: Long
        get() = prefs.getLong(KEY_UNMUTE_DELAY_MS, 400L)
        set(value) = prefs.edit().putLong(KEY_UNMUTE_DELAY_MS, value).apply()

    var isLowVolumeMode: Boolean
        get() = prefs.getBoolean(KEY_LOW_VOLUME_MODE, false)
        set(value) = prefs.edit().putBoolean(KEY_LOW_VOLUME_MODE, value).apply()

    var showNotifications: Boolean
        get() = prefs.getBoolean(KEY_SHOW_NOTIFICATIONS, true)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_NOTIFICATIONS, value).apply()

    var autoStartOnBoot: Boolean
        get() = prefs.getBoolean(KEY_AUTO_START_BOOT, true)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_START_BOOT, value).apply()

    var totalAdsMuted: Int
        get() = prefs.getInt(KEY_TOTAL_ADS_MUTED, 0)
        set(value) = prefs.edit().putInt(KEY_TOTAL_ADS_MUTED, value).apply()

    var totalSilenceSeconds: Long
        get() = prefs.getLong(KEY_TOTAL_SILENCE_SECONDS, 0L)
        set(value) = prefs.edit().putLong(KEY_TOTAL_SILENCE_SECONDS, value).apply()

    var savedVolume: Int
        get() = prefs.getInt(KEY_SAVED_VOLUME, -1)
        set(value) = prefs.edit().putInt(KEY_SAVED_VOLUME, value).apply()

    val todayAdsMuted: Int
        get() {
            checkAndResetDailyCount()
            return prefs.getInt(KEY_TODAY_ADS_MUTED, 0)
        }

    fun recordAdMuted(durationSeconds: Long = 15L) {
        checkAndResetDailyCount()
        val currentTotal = totalAdsMuted + 1
        val currentSeconds = totalSilenceSeconds + durationSeconds
        val currentToday = prefs.getInt(KEY_TODAY_ADS_MUTED, 0) + 1

        prefs.edit()
            .putInt(KEY_TOTAL_ADS_MUTED, currentTotal)
            .putLong(KEY_TOTAL_SILENCE_SECONDS, currentSeconds)
            .putInt(KEY_TODAY_ADS_MUTED, currentToday)
            .apply()
    }

    private fun checkAndResetDailyCount() {
        val todayStr = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
        val lastDate = prefs.getString(KEY_LAST_DATE, "")
        if (lastDate != todayStr) {
            prefs.edit()
                .putString(KEY_LAST_DATE, todayStr)
                .putInt(KEY_TODAY_ADS_MUTED, 0)
                .apply()
        }
    }

    fun getStatistics(): MuterStatistics {
        return MuterStatistics(
            totalAdsMuted = totalAdsMuted,
            todayAdsMuted = todayAdsMuted,
            totalSilenceSeconds = totalSilenceSeconds
        )
    }

    fun getConfig(): AppConfig {
        return AppConfig(
            isServiceActive = isServiceActive,
            unmuteDelayMs = unmuteDelayMs,
            isLowVolumeMode = isLowVolumeMode,
            showNotifications = showNotifications,
            autoStartOnBoot = autoStartOnBoot
        )
    }

    fun resetStatistics() {
        prefs.edit()
            .putInt(KEY_TOTAL_ADS_MUTED, 0)
            .putLong(KEY_TOTAL_SILENCE_SECONDS, 0L)
            .putInt(KEY_TODAY_ADS_MUTED, 0)
            .apply()
    }
}
