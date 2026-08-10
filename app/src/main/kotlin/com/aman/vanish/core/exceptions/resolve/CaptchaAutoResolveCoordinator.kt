package com.aman.vanish.core.exceptions.resolve

import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import com.aman.vanish.core.exceptions.CloudFlareProtectedException
import com.aman.vanish.core.ui.DefaultActivityLifecycleCallbacks
import dagger.hilt.android.qualifiers.ApplicationContext
import org.koitharu.kotatsu.parsers.model.MangaSource
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Stub of CaptchaAutoResolveCoordinator.
 * Auto-solve removed — manual flow implemented in [ManualCaptchaActivity].
 */
@Singleton
class CaptchaAutoResolveCoordinator @Inject constructor(
	@ApplicationContext private val context: Context,
) : DefaultActivityLifecycleCallbacks, DefaultLifecycleObserver {
	fun registerHiddenActivity(activity: Any) {}
	fun unregisterHiddenActivity(activity: Any) {}
	fun notifyResolveResult(source: MangaSource, success: Boolean) {}
	fun isResolveActive(source: MangaSource): Boolean = false
	suspend fun awaitActiveResolve(source: MangaSource): Boolean? = null
	
	suspend fun <T> runWithVerification(
		source: MangaSource,
		mayStartVerification: Boolean,
		block: suspend () -> T,
	): T {
		return block()
	}

	fun notifyVerificationIneffective(source: MangaSource) {}
	suspend fun resolveIfEnabled(exception: CloudFlareProtectedException): Boolean = false
	suspend fun resolve(source: MangaSource, exception: CloudFlareProtectedException): Boolean = false
}
