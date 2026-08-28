package com.example.spofity_ad_muter.ui.viewmodel

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.spofity_ad_muter.AdMuterForegroundService
import com.example.spofity_ad_muter.AppPreferences
import com.example.spofity_ad_muter.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = AppPreferences.getInstance(application)

    private val _playbackStatus = MutableStateFlow(PlaybackStatus())
    val playbackStatus: StateFlow<PlaybackStatus> = _playbackStatus.asStateFlow()

    private val _stats = MutableStateFlow(prefs.getStatistics())
    val stats: StateFlow<MuterStatistics> = _stats.asStateFlow()

    private val _config = MutableStateFlow(prefs.getConfig())
    val config: StateFlow<AppConfig> = _config.asStateFlow()

    private val _isNotificationAccessGranted = MutableStateFlow(false)
    val isNotificationAccessGranted: StateFlow<Boolean> = _isNotificationAccessGranted.asStateFlow()

    private val _isBatteryOptimizationIgnored = MutableStateFlow(false)
    val isBatteryOptimizationIgnored: StateFlow<Boolean> = _isBatteryOptimizationIgnored.asStateFlow()

    private val _activityLog = MutableStateFlow<List<AdLogEntry>>(emptyList())
    val activityLog: StateFlow<List<AdLogEntry>> = _activityLog.asStateFlow()

    init {
        checkPermissions()
        observeEvents()
    }

    private fun observeEvents() {
        viewModelScope.launch {
            EventBus.events.collect { event ->
                when (event) {
                    is AppEvent.PlaybackUpdated -> {
                        _playbackStatus.value = event.status
                        if (event.status.isAd && event.status.isMuted) {
                            val newEntry = AdLogEntry(
                                id = UUID.randomUUID().toString(),
                                title = event.status.title,
                                artist = event.status.artist,
                                timestamp = System.currentTimeMillis()
                            )
                            _activityLog.update { (listOf(newEntry) + it).take(50) }
                        }
                    }
                    is AppEvent.StatsUpdated -> {
                        _stats.value = event.stats
                    }
                    is AppEvent.ServiceConnectionChanged -> {
                        checkPermissions()
                    }
                }
            }
        }
    }

    fun checkPermissions() {
        val context = getApplication<Application>()
        _isNotificationAccessGranted.value = isNotificationListenerEnabled(context)
        _isBatteryOptimizationIgnored.value = isBatteryOptimizationDisabled(context)
        _stats.value = prefs.getStatistics()
        _config.value = prefs.getConfig()
    }

    fun toggleServiceActive(active: Boolean) {
        prefs.isServiceActive = active
        _config.value = prefs.getConfig()

        val context = getApplication<Application>()
        val intent = Intent(context, AdMuterForegroundService::class.java)

        if (active) {
            intent.action = AdMuterForegroundService.ACTION_START
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } else {
            intent.action = AdMuterForegroundService.ACTION_STOP
            context.startService(intent)
            _playbackStatus.value = PlaybackStatus()
        }
    }

    fun setUnmuteDelay(delayMs: Long) {
        prefs.unmuteDelayMs = delayMs
        _config.value = prefs.getConfig()
    }

    fun setLowVolumeMode(enabled: Boolean) {
        prefs.isLowVolumeMode = enabled
        _config.value = prefs.getConfig()
    }

    fun setShowNotifications(show: Boolean) {
        prefs.showNotifications = show
        _config.value = prefs.getConfig()
    }

    fun setAutoStartBoot(autoStart: Boolean) {
        prefs.autoStartOnBoot = autoStart
        _config.value = prefs.getConfig()
    }

    fun resetStats() {
        prefs.resetStatistics()
        _stats.value = prefs.getStatistics()
        _activityLog.value = emptyList()
    }

    fun openNotificationAccessSettings(context: Context) {
        try {
            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            } else {
                Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
            }
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        } catch (_: Exception) {
            val intent = Intent(Settings.ACTION_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }

    fun requestIgnoreBatteryOptimization(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (_: Exception) {
                val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            }
        }
    }

    fun openSpotifySettings(context: Context) {
        try {
            val intent = Intent(Intent.ACTION_APPLICATION_PREFERENCES).apply {
                setPackage("com.spotify.music")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            try {
                val intent = context.packageManager.getLaunchIntentForPackage("com.spotify.music")
                if (intent != null) {
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(intent)
                }
            } catch (_: Exception) {}
        }
    }

    private fun isNotificationListenerEnabled(context: Context): Boolean {
        val packageName = context.packageName
        val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        if (flat != null && flat.isNotEmpty()) {
            val names = flat.split(":")
            for (name in names) {
                val cn = ComponentName.unflattenFromString(name)
                if (cn != null && cn.packageName == packageName) {
                    return true
                }
            }
        }
        return false
    }

    private fun isBatteryOptimizationDisabled(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            powerManager.isIgnoringBatteryOptimizations(context.packageName)
        } else {
            true
        }
    }
}
