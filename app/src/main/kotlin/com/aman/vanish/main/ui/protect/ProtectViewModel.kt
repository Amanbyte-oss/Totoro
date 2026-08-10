package com.aman.vanish.main.ui.protect

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import com.aman.vanish.core.exceptions.WrongPasswordException
import com.aman.vanish.core.prefs.AppSettings
import com.aman.vanish.core.ui.BaseViewModel
import com.aman.vanish.core.util.ext.MutableEventFlow
import com.aman.vanish.core.util.ext.call
import org.koitharu.kotatsu.parsers.util.md5
import javax.inject.Inject

private const val PASSWORD_COMPARE_DELAY = 1_000L

@HiltViewModel
class ProtectViewModel @Inject constructor(
	private val settings: AppSettings,
	private val protectHelper: AppProtectHelper,
) : BaseViewModel() {

	private var job: Job? = null

	val onUnlockSuccess = MutableEventFlow<Unit>()

	val isBiometricEnabled
		get() = settings.isBiometricProtectionEnabled

	val isNumericPassword
		get() = settings.isAppPasswordNumeric

	fun tryUnlock(password: String) {
		if (job?.isActive == true) {
			return
		}
		job = launchLoadingJob {
			val passwordHash = password.md5()
			val appPasswordHash = settings.appPassword
			if (passwordHash == appPasswordHash) {
				unlock()
			} else {
				delay(PASSWORD_COMPARE_DELAY)
				throw WrongPasswordException()
			}
		}
	}

	fun unlock() {
		protectHelper.unlock()
		onUnlockSuccess.call(Unit)
	}
}
