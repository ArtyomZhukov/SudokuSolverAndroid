package com.zhukovartemvl.sudokusolver

import android.content.Intent
import android.media.projection.MediaProjection
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zhukovartemvl.sudokusolver.service.SudokuSolverOverlayService
import com.zhukovartemvl.sudokusolver.ui.theme.SudokuSolverTheme
import com.zhukovartemvl.sudokusolver.util.StartMediaProjection

class AppActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val startMediaProjection = rememberLauncherForActivityResult(StartMediaProjection()) { mediaProjectionIntent ->
                if (mediaProjectionIntent != null) {
                    startSudokuSolverService(mediaProjectionIntent = mediaProjectionIntent)
                }
            }

            SudokuSolverTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Button(onClick = ::openOverlaySettings) {
                            Text(text = "Open overlay permissions")
                        }
                        Spacer(modifier = Modifier.height(100.dp))
                        Button(onClick = {
                            startMediaProjection.launch()
                        }) {
                            Text(text = "Start service")
                        }
                        Spacer(modifier = Modifier.height(100.dp))
                        Button(onClick = ::stopSudokuSolverService) {
                            Text(text = "Stop service")
                        }
                    }
                }
            }
        }
    }

    private fun startSudokuSolverService(mediaProjectionIntent: Intent) {
        AppActivity.mediaProjectionIntent = mediaProjectionIntent
        val intent = Intent(this, SudokuSolverOverlayService::class.java).apply {
            putExtra(INTENT_COMMAND, INTENT_COMMAND_START)
            // putExtra(Intent.EXTRA_INTENT, mediaProjectionIntent)
        }
        startForegroundService(intent)
    }

    private fun stopSudokuSolverService() {
        val intent = Intent(this, SudokuSolverOverlayService::class.java).apply {
            putExtra(INTENT_COMMAND, INTENT_COMMAND_EXIT)
        }
        startForegroundService(intent)
    }

    private fun checkOverlayPermissions(): Boolean {
        return Settings.canDrawOverlays(this)
    }

    private fun openOverlaySettings() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    // private fun initScreenshotResult() {
    //     registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
    //         screenshotManager.onActivityResult(REQUEST_CODE, it.resultCode, it.data)
    //     }
    // }

    companion object {
        var mediaProjectionIntent: Intent? = null
            private set
    }
}
