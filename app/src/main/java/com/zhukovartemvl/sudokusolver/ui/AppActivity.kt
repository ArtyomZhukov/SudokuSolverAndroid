package com.zhukovartemvl.sudokusolver.ui

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import com.zhukovartemvl.sudokusolver.INTENT_COMMAND
import com.zhukovartemvl.sudokusolver.INTENT_COMMAND_EXIT
import com.zhukovartemvl.sudokusolver.INTENT_COMMAND_START
import com.zhukovartemvl.sudokusolver.service.SudokuSolverOverlayService
import com.zhukovartemvl.sudokusolver.tools.MediaProjectionKeeper
import com.zhukovartemvl.sudokusolver.tools.PermissionsChecker
import com.zhukovartemvl.sudokusolver.ui.main_screen.MainScreenState
import com.zhukovartemvl.sudokusolver.ui.main_screen.MainScreenView
import com.zhukovartemvl.sudokusolver.util.LifecycleListener
import com.zhukovartemvl.sudokusolver.util.StartMediaProjection
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class AppActivity : ComponentActivity() {

    private val permissionsChecker: PermissionsChecker by inject()
    private val mediaProjectionKeeper: MediaProjectionKeeper by inject()

    private var screenState by mutableStateOf(MainScreenState())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val startMediaProjection = rememberLauncherForActivityResult(StartMediaProjection()) { mediaProjectionIntent ->
                if (mediaProjectionIntent != null) {
                    startSudokuSolverService(mediaProjectionIntent = mediaProjectionIntent)
                }
            }
            MainScreenView(
                state = screenState,
                onGrantOverlayPermissionsClick = ::openOverlaySettings,
                onGrantAccessibilityPermissionsClick = ::openAccessibilitySettings,
                onStartService = startMediaProjection::launch,
                onStopService = ::stopSudokuSolverService,
            )
            LifecycleListener(onResume = ::refreshScreenState)
        }
    }

    private fun refreshScreenState() {
        screenState = screenState.copy(
            isOverlayPermissionGranted = permissionsChecker.checkOverlayPermissions(context = this),
            isAccessibilityPermissionGranted = permissionsChecker.checkAccessibilityPermissions(context = this),
        )
    }

    // private val connection = object : ServiceConnection {
    //     override fun onServiceConnected(className: ComponentName, service: IBinder) {
    //         screenState = screenState.copy(isSudokuServiceEnabled = true)
    //     }
    //     override fun onServiceDisconnected(className: ComponentName) {
    //         screenState = screenState.copy(isSudokuServiceEnabled = false)
    //     }
    // }

    private fun startSudokuSolverService(mediaProjectionIntent: Intent) {
        lifecycleScope.launch {
            mediaProjectionKeeper.saveMediaProjectionIntent(intent =  mediaProjectionIntent)
            delay(100)
            val intent = Intent(this@AppActivity, SudokuSolverOverlayService::class.java).apply {
                putExtra(INTENT_COMMAND, INTENT_COMMAND_START)
            }
            // bindService(intent, connection, Context.BIND_AUTO_CREATE)
            startForegroundService(intent)
        }
    }

    private fun stopSudokuSolverService() {
        val intent = Intent(this, SudokuSolverOverlayService::class.java).apply {
            putExtra(INTENT_COMMAND, INTENT_COMMAND_EXIT)
        }
        startForegroundService(intent)
    }

    private fun openOverlaySettings() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}
