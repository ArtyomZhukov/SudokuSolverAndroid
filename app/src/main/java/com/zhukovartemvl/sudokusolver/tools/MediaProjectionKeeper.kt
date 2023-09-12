package com.zhukovartemvl.sudokusolver.tools

import android.content.Intent

class MediaProjectionKeeper {

    private var mediaProjectionIntent: Intent? = null

    fun saveMediaProjectionIntent(intent: Intent) {
        mediaProjectionIntent = intent
    }

    fun getMediaProjectionIntent(): Intent {
        // Cloning the Intent allows reuse.
        // Otherwise, the Intent gets consumed and MediaProjection cannot be started multiple times.
        return mediaProjectionIntent?.clone() as? Intent ?: throw Exception("mediaProjectionIntent must be not null")
    }
}
