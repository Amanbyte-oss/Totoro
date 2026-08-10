package com.aman.vanish.favourites.domain

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import com.aman.vanish.core.os.NetworkState
import com.aman.vanish.core.prefs.AppSettings
import com.aman.vanish.list.domain.ListFilterOption
import com.aman.vanish.list.domain.MangaListQuickFilter
import org.koitharu.kotatsu.parsers.model.ContentType

class FavoritesListQuickFilter @AssistedInject constructor(
	@Assisted private val categoryId: Long,
	private val settings: AppSettings,
	private val repository: FavouritesRepository,
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
		add(ListFilterOption.ContentType(ContentType.MANGA))
		add(ListFilterOption.ContentType(ContentType.MANHWA))
		add(ListFilterOption.ContentType(ContentType.MANHUA))
		repository.findPopularTagTitles(categoryId, 3).mapTo(this) {
			ListFilterOption.TagTitle(it)
		}
		repository.findPopularSources(categoryId, 3).mapTo(this) {
			ListFilterOption.Source(it)
		}
	}

	@AssistedFactory
	interface Factory {

		fun create(categoryId: Long): FavoritesListQuickFilter
	}
}
