package team.mino.core.errorhandling

fun interface UncaughtErrorReporter {
    fun reportFatal(throwable: Throwable)
}
