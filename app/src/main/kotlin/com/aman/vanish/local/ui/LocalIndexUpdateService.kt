package com.aman.vanish.local.ui

import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import com.aman.vanish.core.ui.CoroutineIntentService
import com.aman.vanish.local.data.index.LocalMangaIndex
import javax.inject.Inject

@AndroidEntryPoint
class LocalIndexUpdateService : CoroutineIntentService() {

	@Inject
	lateinit var localMangaIndex: LocalMangaIndex

	override suspend fun IntentJobContext.processIntent(intent: Intent) {
		localMangaIndex.update()
	}

	override fun IntentJobContext.onError(error: Throwable) = Unit
}
