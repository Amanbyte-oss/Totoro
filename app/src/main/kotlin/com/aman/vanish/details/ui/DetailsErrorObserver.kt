package com.aman.vanish.details.ui

import com.google.android.material.snackbar.Snackbar
import com.aman.vanish.R
import com.aman.vanish.core.exceptions.CloudFlareProtectedException
import com.aman.vanish.core.exceptions.UnsupportedSourceException
import com.aman.vanish.core.exceptions.resolve.ErrorObserver
import com.aman.vanish.core.exceptions.resolve.ExceptionResolver
import com.aman.vanish.core.prefs.SourceSettings
import com.aman.vanish.core.util.ext.findCloudFlareException
import com.aman.vanish.core.util.ext.getDisplayMessage
import com.aman.vanish.core.util.ext.isNetworkError
import com.aman.vanish.core.util.ext.isSerializable
import org.koitharu.kotatsu.parsers.exception.NotFoundException
import org.koitharu.kotatsu.parsers.exception.ParseException
import com.aman.vanish.core.model.MangaSource

class DetailsErrorObserver(
	override val activity: DetailsActivity,
	private val viewModel: DetailsViewModel,
	private val resolver: ExceptionResolver?,
) : ErrorObserver(
	activity.viewBinding.scrollView, null, resolver,
	{ isResolved ->
		if (isResolved) {
			viewModel.reload()
		}
	},
) {

	override suspend fun emit(value: Throwable) {
		// Manga details is an explicit user action ("opened a manga"), so auto-resolve is appropriate.
		// If the per-source toggle disables it, or auto-resolve doesn't succeed, fall back to the
		// standard snackbar with the "Solve" action below.
		val cf = value.findCloudFlareException()
		if (cf is CloudFlareProtectedException && resolver != null) {
			val autoDisabled = SourceSettings(host.context, MangaSource(cf.source)).isCaptchaAutoResolveDisabled
			if (!autoDisabled) {
				val resolved = resolver.resolve(cf, tryAutoResolve = true)
				if (resolved) {
					viewModel.reload()
					return
				}
			}
		}
		val snackbar = Snackbar.make(host, value.getDisplayMessage(host.context.resources), Snackbar.LENGTH_SHORT)
		snackbar.setAnchorView(activity.viewBinding.containerBottomSheet)
		if (value is NotFoundException || value is UnsupportedSourceException) {
			snackbar.duration = Snackbar.LENGTH_INDEFINITE
		}
		when {
			canResolve(value) -> {
				snackbar.setAction(ExceptionResolver.getResolveStringId(value)) {
					resolve(value)
				}
			}

			value is ParseException -> {
				val router = router()
				if (router != null && value.isSerializable()) {
					snackbar.setAction(R.string.details) {
						router.showErrorDialog(value)
					}
				}
			}

			value.isNetworkError() -> {
				snackbar.setAction(R.string.try_again) {
					viewModel.reload()
				}
			}
		}
		snackbar.show()
	}
}
