package com.aman.vanish.core.util

import kotlinx.coroutines.CoroutineExceptionHandler
import com.aman.vanish.core.util.ext.printStackTraceDebug
import com.aman.vanish.core.util.ext.report
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

class AcraCoroutineErrorHandler : AbstractCoroutineContextElement(CoroutineExceptionHandler),
	CoroutineExceptionHandler {

	override fun handleException(context: CoroutineContext, exception: Throwable) {
		exception.printStackTraceDebug()
		exception.report()
	}
}
