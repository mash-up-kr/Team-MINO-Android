package team.mino.core.common.android.extension

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import team.mino.core.errorhandling.UncaughtErrorHandler
import timber.log.Timber

private val uncaughtExceptionHandler =
    CoroutineExceptionHandler { _, throwable ->
        Timber.e(throwable)
        UncaughtErrorHandler.dispatch(throwable)
    }

fun ViewModel.launchSafely(block: suspend CoroutineScope.() -> Unit): Job =
    viewModelScope.launch(uncaughtExceptionHandler, block = block)
