package com.zhukovartemvl.sudokusolver.service.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.zhukovartemvl.sudokusolver.INTENT_COMMAND
import com.zhukovartemvl.sudokusolver.INTENT_COMMAND_EXIT
import com.zhukovartemvl.sudokusolver.service.SudokuSolverOverlayService

class ExitServiceReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val exitIntent = Intent(context.applicationContext, SudokuSolverOverlayService::class.java)
        exitIntent.putExtra(INTENT_COMMAND, INTENT_COMMAND_EXIT)
        context.startForegroundService(exitIntent)
    }
}
