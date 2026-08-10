package com.aman.vanish.explore.ui.preset

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import com.aman.vanish.core.nav.AppRouter
import com.aman.vanish.core.model.isNsfw
import com.aman.vanish.core.prefs.AppSettings
import com.aman.vanish.core.ui.BaseViewModel
import com.aman.vanish.core.util.ext.MutableEventFlow
import com.aman.vanish.core.util.ext.call
import com.aman.vanish.explore.data.SourcePreset
import com.aman.vanish.explore.data.SourcePresetsRepository
import com.aman.vanish.explore.data.MangaSourcesRepository
import org.koitharu.kotatsu.parsers.model.MangaParserSource
import javax.inject.Inject

@HiltViewModel
class SourcePresetEditViewModel @Inject constructor(
	savedStateHandle: SavedStateHandle,
	private val presetsRepository: SourcePresetsRepository,
	private val sourcesRepository: MangaSourcesRepository,
	private val settings: AppSettings,
) : BaseViewModel() {

	private val presetId = savedStateHandle[AppRouter.KEY_ID] ?: NO_ID

	val onSaved = MutableEventFlow<Unit>()
	val preset = MutableStateFlow<SourcePreset?>(null)

	val allLocales: Set<String> = sourcesRepository.allMangaSources
		.mapNotNullTo(LinkedHashSet()) { it.locale.takeIf { l -> l.isNotEmpty() } }

	init {
		launchLoadingJob(Dispatchers.Default) {
			preset.value = if (presetId != NO_ID) {
				presetsRepository.getById(presetId)
			} else {
				null
			}
		}
	}

	fun save(title: String, selectedLanguages: Set<String>) {
		launchLoadingJob(Dispatchers.Default) {
			check(title.isNotEmpty())
			if (presetId == NO_ID) {
				val initialSources = getSourcesForLanguages(selectedLanguages)
				presetsRepository.createPreset(title, selectedLanguages, initialSources)
			} else {
				presetsRepository.updatePreset(presetId, title, selectedLanguages)
			}
			onSaved.call(Unit)
		}
	}

	private fun getSourcesForLanguages(languages: Set<String>): Set<String> {
		if (languages.isEmpty()) return emptySet()
		val skipNsfw = settings.isNsfwContentDisabled
		return sourcesRepository.allMangaSources
			.filter { it.locale in languages && (!skipNsfw || !it.isNsfw()) }
			.mapTo(HashSet()) { it.name }
	}

	companion object {
		const val NO_ID = -1L
	}
}
