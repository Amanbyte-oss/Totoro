package com.aman.vanish.tracker.ui.updates

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.plus
import com.aman.vanish.R
import com.aman.vanish.core.parser.MangaDataRepository
import com.aman.vanish.core.prefs.AppSettings
import com.aman.vanish.core.prefs.ListMode
import com.aman.vanish.core.prefs.observeAsFlow
import com.aman.vanish.core.ui.model.DateTimeAgo
import com.aman.vanish.core.util.ext.calculateTimeAgo
import com.aman.vanish.core.util.ext.onFirst
import com.aman.vanish.list.domain.ListFilterOption
import com.aman.vanish.list.domain.MangaListMapper
import com.aman.vanish.list.domain.QuickFilterListener
import com.aman.vanish.list.ui.MangaListViewModel
import com.aman.vanish.list.ui.model.EmptyState
import com.aman.vanish.list.ui.model.ListHeader
import com.aman.vanish.list.ui.model.ListModel
import com.aman.vanish.list.ui.model.LoadingState
import com.aman.vanish.list.ui.model.toErrorState
import com.aman.vanish.tracker.domain.TrackingRepository
import com.aman.vanish.tracker.domain.UpdatesListQuickFilter
import com.aman.vanish.tracker.domain.model.MangaTracking
import javax.inject.Inject
import com.aman.vanish.local.data.LocalStorageChanges
import com.aman.vanish.local.domain.model.LocalManga
import kotlinx.coroutines.flow.SharedFlow

@HiltViewModel
class UpdatesViewModel @Inject constructor(
	private val repository: TrackingRepository,
	settings: AppSettings,
	private val mangaListMapper: MangaListMapper,
	private val quickFilter: UpdatesListQuickFilter,
	mangaDataRepository: MangaDataRepository,
	@LocalStorageChanges localStorageChanges: SharedFlow<LocalManga?>,
) : MangaListViewModel(settings, mangaDataRepository, localStorageChanges), QuickFilterListener by quickFilter {

	override val content = combine(
		quickFilter.appliedOptions.flatMapLatest { filterOptions ->
			repository.observeUpdatedManga(
				limit = 0,
				filterOptions = filterOptions,
			)
		},
		quickFilter.appliedOptions,
		settings.observeAsFlow(AppSettings.KEY_UPDATED_GROUPING) { isUpdatedGroupingEnabled },
		observeListModeWithTriggers(),
	) { mangaList, filters, grouping, mode ->
		when {
			mangaList.isEmpty() -> listOfNotNull(
				quickFilter.filterItem(filters),
				EmptyState(
					icon = R.drawable.ic_empty_history,
					textPrimary = R.string.text_history_holder_primary,
					textSecondary = R.string.text_history_holder_secondary,
					actionStringRes = 0,
				),
			)

			else -> mangaList.toUi(mode, filters, grouping)
		}
	}.onStart {
		loadingCounter.increment()
	}.onFirst {
		loadingCounter.decrement()
	}.catch {
		emit(listOf(it.toErrorState(canRetry = false)))
	}.stateIn(viewModelScope + Dispatchers.Default, SharingStarted.Eagerly, listOf(LoadingState()))

	init {
		launchJob(Dispatchers.Default) {
			repository.gc()
		}
	}

	override fun onRefresh() = Unit

	override fun onRetry() = Unit

	fun remove(ids: Set<Long>) {
		launchJob(Dispatchers.Default) {
			repository.clearUpdates(ids)
		}
	}

	private suspend fun List<MangaTracking>.toUi(
		mode: ListMode,
		filters: Set<ListFilterOption>,
		grouped: Boolean,
	): List<ListModel> {
		val result = ArrayList<ListModel>(if (grouped) (size * 1.4).toInt() else size + 1)
		quickFilter.filterItem(filters)?.let(result::add)
		var prevHeader: DateTimeAgo? = null
		for (item in this) {
			if (grouped) {
				val header = item.lastChapterDate?.let { calculateTimeAgo(it) }
				if (header != prevHeader) {
					if (header != null) {
						result += ListHeader(header)
					}
					prevHeader = header
				}
			}
			result += mangaListMapper.toListModel(item.manga, mode)
		}
		return result
	}
}
