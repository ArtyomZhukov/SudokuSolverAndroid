package com.zhukovartemvl.sudokusolver.ui.main_screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MainScreenView(
    state: MainScreenState,
    onGrantOverlayPermissionsClick: () -> Unit,
    onGrantAccessibilityPermissionsClick: () -> Unit,
    onStartService: () -> Unit,
    onStopService: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar {
                Text(text = "Sudoku Solver")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (!state.isOverlayPermissionGranted) {
                Button(onClick = onGrantOverlayPermissionsClick) {
                    Text(text = "Grant overlay permissions")
                }
                Spacer(modifier = Modifier.height(50.dp))
            }
            if (!state.isAccessibilityPermissionGranted) {
                Button(onClick = onGrantAccessibilityPermissionsClick) {
                    Text(text = "Grant accessibility permissions")
                }
            }
            if (state.isOverlayPermissionGranted && state.isAccessibilityPermissionGranted) {
                // if (state.isSudokuServiceEnabled) {
                    Button(onClick = onStartService) {
                        Text(text = "Start service")
                    }
                // } else {
                    Button(onClick = onStopService) {
                        Text(text = "Stop service")
                    }
                // }
            }
        }
    }
}
