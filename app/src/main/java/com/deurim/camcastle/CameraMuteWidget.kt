package com.deurim.camcastle

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.service.quicksettings.TileService
import android.widget.RemoteViews
import androidx.annotation.RequiresApi

class CameraMuteWidget : AppWidgetProvider() {

    companion object {
        private const val ACTION_WIDGET_TOGGLE = "com.deurim.camcastle.WIDGET_TOGGLE"

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, CameraMuteWidget::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

            val intent = Intent(context, CameraMuteWidget::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
            }
            context.sendBroadcast(intent)
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == ACTION_WIDGET_TOGGLE) {
            handleWidgetToggle(context)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun handleWidgetToggle(context: Context) {
        if (!Settings.System.canWrite(context)) {
            val intent = Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            return
        }

        val isMuted = CameraHelper.isCameraMuted(context.contentResolver)
        if (isMuted) {
            CameraHelper.setCameraUnmute(context.contentResolver)
        } else {
            CameraHelper.setCameraMute(context.contentResolver)
        }

        updateAllWidgets(context)

        // Update Quick Settings Tile (Android 7.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            TileService.requestListeningState(
                context,
                ComponentName(context, CameraMuteTileService::class.java)
            )
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_camera_mute)

        val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.System.canWrite(context)
        } else {
            true
        }

        if (!hasPermission) {
            views.setImageViewResource(R.id.widget_icon, R.drawable.baseline_videocam_24)
            views.setTextViewText(R.id.widget_label, context.getString(R.string.tile_no_permission))
        } else {
            val isMuted = CameraHelper.isCameraMuted(context.contentResolver)

            views.setImageViewResource(
                R.id.widget_icon,
                if (isMuted) R.drawable.baseline_videocam_off_24 else R.drawable.baseline_videocam_24
            )

            views.setTextViewText(
                R.id.widget_label,
                context.getString(if (isMuted) R.string.tile_muted else R.string.tile_unmuted)
            )
        }

        val toggleIntent = Intent(context, CameraMuteWidget::class.java).apply {
            action = ACTION_WIDGET_TOGGLE
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            toggleIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_icon, pendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
