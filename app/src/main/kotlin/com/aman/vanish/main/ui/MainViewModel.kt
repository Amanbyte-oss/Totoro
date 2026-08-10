package com.aman.vanish.main.ui

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.plus
import com.aman.vanish.core.exceptions.EmptyHistoryException
import com.aman.vanish.core.prefs.AppSettings
import com.aman.vanish.core.prefs.observeAsFlow
import com.aman.vanish.core.prefs.observeAsStateFlow
import com.aman.vanish.core.ui.BaseViewModel
import com.aman.vanish.core.util.ext.MutableEventFlow
import com.aman.vanish.core.util.ext.call
import com.aman.vanish.explore.data.MangaSourcesRepository
import com.aman.vanish.history.data.HistoryRepository
import com.aman.vanish.main.domain.ReadingResumeEnabledUseCase
import org.koitharu.kotatsu.parsers.model.Manga
import com.aman.vanish.tracker.domain.TrackingRepository
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
	private val historyRepository: HistoryRepository,
	trackingRepository: TrackingRepository,
	private val settings: AppSettings,
	readingResumeEnabledUseCase: ReadingResumeEnabledUseCase,
	private val sourcesRepository: MangaSourcesRepository,
) : BaseViewModel() {

	val onOpenReader = MutableEventFlow<Manga>()
	val onFirstStart = MutableEventFlow<Unit>()

	val isResumeEnabled = readingResumeEnabledUseCase()
		.withErrorHandling()
		.stateIn(
			scope = viewModelScope + Dispatchers.Default,
			started = SharingStarted.WhileSubscribed(5000),
			initialValue = false,
		)

	val feedCounter = trackingRepository.observeUnreadUpdatesCount()
		.withErrorHandling()
		.stateIn(viewModelScope + Dispatchers.Default, SharingStarted.Lazily, 0)

	val isBottomNavPinned = settings.observeAsFlow(
		AppSettings.KEY_NAV_PINNED,
	) {
		isNavBarPinned
	}.flowOn(Dispatchers.Default)

	val isIncognitoModeEnabled = settings.observeAsStateFlow(
		scope = viewModelScope + Dispatchers.Default,
		key = AppSettings.KEY_INCOGNITO_MODE,
		valueProducer = { isIncognitoModeEnabled },
	)

	init {
		launchJob(Dispatchers.Default) {
			if (sourcesRepository.isSetupRequired()) {
				onFirstStart.call(Unit)
			}
		}
	}

	fun openLastReader() {
		launchLoadingJob(Dispatchers.Default) {
			val manga = historyRepository.getLastOrNull() ?: throw EmptyHistoryException()
			onOpenReader.call(manga)
		}
	}

	fun setIncognitoMode(isEnabled: Boolean) {
		settings.isIncognitoModeEnabled = isEnabled
	}
}
