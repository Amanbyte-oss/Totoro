package com.aman.vanish.picker.ui.manga

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.plus
import com.aman.vanish.R
import com.aman.vanish.core.parser.MangaDataRepository
import com.aman.vanish.core.prefs.AppSettings
import com.aman.vanish.favourites.domain.FavouritesRepository
import com.aman.vanish.history.data.HistoryRepository
import com.aman.vanish.list.domain.MangaListMapper
import com.aman.vanish.list.ui.MangaListViewModel
import com.aman.vanish.list.ui.model.ListHeader
import com.aman.vanish.list.ui.model.ListModel
import com.aman.vanish.list.ui.model.LoadingState
import javax.inject.Inject
import kotlinx.coroutines.flow.SharedFlow
import com.aman.vanish.local.data.LocalStorageChanges
import com.aman.vanish.local.domain.model.LocalManga

@HiltViewModel
class MangaPickerViewModel @Inject constructor(
	private val settings: AppSettings,
	mangaDataRepository: MangaDataRepository,
	private val historyRepository: HistoryRepository,
	private val favouritesRepository: FavouritesRepository,
	private val mangaListMapper: MangaListMapper,
	@LocalStorageChanges localStorageChanges: SharedFlow<LocalManga?>,
) : MangaListViewModel(settings, mangaDataRepository, localStorageChanges) {

	override val content: StateFlow<List<ListModel>>
		get() = flow {
			emit(loadList())
		}.stateIn(viewModelScope + Dispatchers.Default, SharingStarted.Lazily, listOf(LoadingState()))

	override fun onRefresh() = Unit

	override fun onRetry() = Unit

	private suspend fun loadList() = buildList {
		val history = historyRepository.getList(0, Int.MAX_VALUE)
		if (history.isNotEmpty()) {
			add(ListHeader(R.string.history))
			mangaListMapper.toListModelList(this, history, settings.listMode)
		}
		val categories = favouritesRepository.observeCategoriesForLibrary().first()
		for (category in categories) {
			val favorites = favouritesRepository.getManga(category.id)
			if (favorites.isNotEmpty()) {
				add(ListHeader(category.title))
				mangaListMapper.toListModelList(this, favorites, settings.listMode)
			}
		}
	}
}
