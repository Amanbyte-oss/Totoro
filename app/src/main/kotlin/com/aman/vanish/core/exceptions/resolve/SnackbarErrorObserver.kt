package com.aman.vanish.core.exceptions.resolve

import android.view.View
import androidx.core.util.Consumer
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.aman.vanish.R
import com.aman.vanish.core.util.ext.getDisplayMessage
import com.aman.vanish.core.util.ext.isSerializable
import com.aman.vanish.main.ui.owners.BottomNavOwner
import com.aman.vanish.main.ui.owners.BottomSheetOwner
import org.koitharu.kotatsu.parsers.exception.ParseException
import android.content.Intent
import com.aman.vanish.ui.captcha.ManualCaptchaActivity
import com.aman.vanish.core.exceptions.resolve.CaptchaResultBus

class SnackbarErrorObserver(
	host: View,
	fragment: Fragment?,
	resolver: ExceptionResolver?,
	onResolved: Consumer<Boolean>?,
) : ErrorObserver(host, fragment, resolver, onResolved) {

	constructor(
		host: View,
		fragment: Fragment?,
	) : this(host, fragment, null, null)

	override suspend fun emit(value: Throwable) {
		if (tryAutoResolve(value)) {
			return
		}
		if (value.message?.startsWith("Verification failed") == true) {
			val snackbar = Snackbar.make(host, "Verification failed", Snackbar.LENGTH_LONG)
			when (activity) {
				is BottomNavOwner -> snackbar.anchorView = activity.bottomNav
				is BottomSheetOwner -> snackbar.anchorView = activity.bottomSheet
			}
			val blockedEvent = CaptchaResultBus.blockedSources.value.values.firstOrNull()
			if (blockedEvent != null) {
				snackbar.setAction("Retry") {
					val intent = ManualCaptchaActivity.newIntent(host.context, blockedEvent.url, blockedEvent.sourceName)
					intent.putExtra(ManualCaptchaActivity.EXTRA_USER_AGENT, blockedEvent.userAgent)
					host.context.startActivity(intent)
				}
			}
			snackbar.show()
			return
		}
		val snackbar = Snackbar.make(host, value.getDisplayMessage(host.context.resources), Snackbar.LENGTH_SHORT)
		when (activity) {
			is BottomNavOwner -> snackbar.anchorView = activity.bottomNav
			is BottomSheetOwner -> snackbar.anchorView = activity.bottomSheet
		}
		if (canResolve(value)) {
			snackbar.setAction(ExceptionResolver.getResolveStringId(value)) {
				resolve(value)
			}
		} else if (value is ParseException) {
			val router = router()
			if (router != null && value.isSerializable()) {
				snackbar.setAction(R.string.details) {
					router.showErrorDialog(value)
				}
			}
		}
		snackbar.show()
	}
}
