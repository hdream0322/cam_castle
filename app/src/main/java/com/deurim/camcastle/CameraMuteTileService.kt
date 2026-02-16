package com.deurim.camcastle

import android.content.Intent
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog

@RequiresApi(Build.VERSION_CODES.N)
class CameraMuteTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()

        if (!Settings.System.canWrite(applicationContext)) {
            showDialog(createPermissionDialog())
            return
        }

        val isMuted = CameraHelper.isCameraMuted(contentResolver)
        if (isMuted) {
            CameraHelper.setCameraUnmute(contentResolver)
        } else {
            CameraHelper.setCameraMute(contentResolver)
        }

        updateTileState()

        // Update widgets
        CameraMuteWidget.updateAllWidgets(this)
        CameraMuteWidget2x1.updateAllWidgets(this)
    }

    private fun updateTileState() {
        val tile = qsTile ?: return

        if (!Settings.System.canWrite(applicationContext)) {
            tile.state = Tile.STATE_INACTIVE
            tile.label = getString(R.string.tile_no_permission)
            tile.icon = Icon.createWithResource(this, R.drawable.baseline_videocam_24)
        } else {
            val isMuted = CameraHelper.isCameraMuted(contentResolver)

            tile.state = if (isMuted) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            tile.label = getString(if (isMuted) R.string.tile_muted else R.string.tile_unmuted)
            tile.icon = Icon.createWithResource(
                this,
                if (isMuted) R.drawable.baseline_videocam_off_24 else R.drawable.baseline_videocam_24
            )
        }

        tile.updateTile()
    }

    private fun createPermissionDialog(): AlertDialog {
        return AlertDialog.Builder(this)
            .setTitle(R.string.main_activity_write_settings_permission_title)
            .setMessage(R.string.main_activity_write_settings_permission_message)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                    data = Uri.parse("package:$packageName")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivityAndCollapse(intent)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
    }
}
