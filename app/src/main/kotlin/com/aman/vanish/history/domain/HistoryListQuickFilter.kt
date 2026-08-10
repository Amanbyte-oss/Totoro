package com.aman.vanish.history.domain

import com.aman.vanish.core.os.NetworkState
import com.aman.vanish.core.prefs.AppSettings
import com.aman.vanish.history.data.HistoryRepository
import com.aman.vanish.list.domain.ListFilterOption
import com.aman.vanish.list.domain.MangaListQuickFilter
import javax.inject.Inject

class HistoryListQuickFilter @Inject constructor(
	private val settings: AppSettings,
	private val repository: HistoryRepository,
	networkState: NetworkState,
) : MangaListQuickFilter(settings) {

	init {
		setFilterOption(ListFilterOption.Downloaded, !networkState.value)
	}

	override suspend fun getAvailableFilterOptions(): List<ListFilterOption> = buildList {
		add(ListFilterOption.Downloaded)
		if (settings.isTrackerEnabled) {
			add(ListFilterOption.Macro.NEW_CHAPTERS)
		}
		add(ListFilterOption.Macro.COMPLETED)
		add(ListFilterOption.Macro.FAVORITE)
		add(ListFilterOption.NOT_FAVORITE)
		if (!settings.isNsfwContentDisabled) {
			add(ListFilterOption.Macro.NSFW)
		}
		repository.getPopularTags(3).mapTo(this) {
			ListFilterOption.Tag(it)
		}
		repository.getPopularSources(3).mapTo(this) {
			ListFilterOption.Source(it)
		}
	}
}
