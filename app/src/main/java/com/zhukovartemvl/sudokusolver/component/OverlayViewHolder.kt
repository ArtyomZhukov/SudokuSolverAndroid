package com.zhukovartemvl.sudokusolver.component

import android.content.Context
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.runtime.Recomposer
import androidx.compose.ui.platform.AndroidUiDispatcher
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.compositionContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.zhukovartemvl.sudokusolver.service.ServiceLifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class OverlayViewHolder(val params: WindowManager.LayoutParams, context: Context) {

    val view = ComposeView(context)

    init {
        params.gravity = Gravity.TOP or Gravity.START

        view.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        composeViewConfigure(view)
    }

    private fun composeViewConfigure(composeView: ComposeView) {
        val lifecycleOwner = ServiceLifecycleOwner()
        lifecycleOwner.performRestore(null)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

        composeView.setViewTreeLifecycleOwner(lifecycleOwner)
        composeView.setViewTreeViewModelStoreOwner(composeView.findViewTreeViewModelStoreOwner())
        composeView.setViewTreeSavedStateRegistryOwner(lifecycleOwner)

        val coroutineContext = AndroidUiDispatcher.CurrentThread
        val runRecomposeScope = CoroutineScope(coroutineContext)
        val recomposer = Recomposer(coroutineContext)
        composeView.compositionContext = recomposer
        // todo do i need to manually cancel this scope/job when service onDestroy???
        runRecomposeScope.launch {
            recomposer.runRecomposeAndApplyChanges()
        }
    }
}
// TODO move into overlayviewholder
// https://gist.github.com/handstandsam/6ecff2f39da72c0b38c07aa80bbb5a2f
