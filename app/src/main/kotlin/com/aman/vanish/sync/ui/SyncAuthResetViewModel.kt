package com.aman.vanish.sync.ui

import android.content.Context
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import com.aman.vanish.R
import com.aman.vanish.core.ui.BaseViewModel
import com.aman.vanish.core.util.ext.MutableEventFlow
import com.aman.vanish.core.util.ext.call
import com.aman.vanish.sync.data.SyncAuthApi
import javax.inject.Inject

@HiltViewModel
class SyncAuthResetViewModel @Inject constructor(
	@ApplicationContext context: Context,
	private val api: SyncAuthApi,
) : BaseViewModel() {

	val onPasswordResetSucceeded = MutableEventFlow<Unit>()
	val syncURL = MutableStateFlow(context.resources.getStringArray(R.array.sync_url_list).first())
	val resetToken = MutableStateFlow<String?>(null)

	fun resetPassword(password: String) {
		val urlValue = syncURL.value
		val resetTokenValue = resetToken.value ?: return // Token should never be null because the fragment exits if none is provided

		launchLoadingJob(Dispatchers.Default) {
			api.resetPassword(urlValue, resetTokenValue, password)
			onPasswordResetSucceeded.call(Unit)
		}
	}
}
