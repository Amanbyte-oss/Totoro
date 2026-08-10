package com.aman.vanish.sync.ui

import android.accounts.AccountManager
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
import com.aman.vanish.sync.domain.SyncAuthResult
import javax.inject.Inject

@HiltViewModel
class SyncAuthViewModel @Inject constructor(
	@ApplicationContext context: Context,
	private val api: SyncAuthApi,
) : BaseViewModel() {

	val onAccountAlreadyExists = MutableEventFlow<Unit>()
	val onTokenObtained = MutableEventFlow<SyncAuthResult>()
	val onPasswordReset = MutableEventFlow<Unit>()
	val syncURL = MutableStateFlow(context.resources.getStringArray(R.array.sync_url_list).first())

	init {
		launchJob(Dispatchers.Default) {
			val am = AccountManager.get(context)
			val accounts = am.getAccountsByType(context.getString(R.string.account_type_sync))
			if (accounts.isNotEmpty()) {
				onAccountAlreadyExists.call(Unit)
			}
		}
	}

	fun obtainToken(email: String, password: String) {
		val urlValue = syncURL.value
		launchLoadingJob(Dispatchers.Default) {
			val token = api.authenticate(urlValue, email, password)
			val result = SyncAuthResult(syncURL.value, email, password, token)
			onTokenObtained.call(result)
		}
	}

	fun forgotPassword(email: String) {
		val urlValue = syncURL.value
		launchLoadingJob(Dispatchers.Default) {
			api.forgotPassword(urlValue, email)
			onPasswordReset.call(Unit)
		}
	}
}
