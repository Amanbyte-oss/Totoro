package com.aman.vanish.ai

import com.aman.vanish.ai.models.AiStructuredFilters
import org.koitharu.kotatsu.parsers.model.ContentType
import org.koitharu.kotatsu.parsers.model.Demographic
import org.koitharu.kotatsu.parsers.model.MangaListFilter
import org.koitharu.kotatsu.parsers.model.MangaState
import org.koitharu.kotatsu.parsers.model.MangaTag
import com.aman.vanish.core.parser.MangaRepository

/**
 * Filter capability matrix:
 * 
 * - MangaDex: supports genres? Yes (via tags), supports type? Yes
 * - MangaFire_EN: supports genres? Yes (via tags), supports type? Yes
 * - Comix: supports genres? Yes (via tags), supports type? Yes
 */
object FilterTranslator {
    suspend fun translate(filters: AiStructuredFilters, repository: MangaRepository): MangaListFilter {
        val options = try {
            repository.getFilterOptions()
        } catch (e: Exception) {
            null
        }

        val availableTags = options?.availableTags.orEmpty()
        
        // Translate Genres
        val tags = mutableSetOf<MangaTag>()
        filters.genres.forEach { genre ->
            val matchingTag = findMatchingTag(genre, availableTags)
            if (matchingTag != null) {
                tags.add(matchingTag)
            }
        }

        // Translate Excluded Tags
        val tagsExclude = mutableSetOf<MangaTag>()
        filters.excludeTags?.forEach { tagText ->
            val matchingTag = findMatchingTag(tagText, availableTags)
            if (matchingTag != null) {
                tagsExclude.add(matchingTag)
            }
        }

        // Translate Content Types
        val types = mutableSetOf<ContentType>()
        val typeVal = filters.type
        val mappedType = when (typeVal?.lowercase()) {
            "manga" -> ContentType.MANGA
            "manhwa" -> ContentType.MANHWA
            "manhua" -> ContentType.MANHUA
            else -> null
        }
        if (mappedType != null && typeVal != null) {
            val availableTypes = options?.availableContentTypes.orEmpty()
            if (availableTypes.contains(mappedType)) {
                types.add(mappedType)
            } else {
                // Fallback: search if type name is supported as a tag
                val matchingTag = findMatchingTag(typeVal, availableTags)
                if (matchingTag != null) {
                    tags.add(matchingTag)
                }
            }
        }

        // Translate Manga State (Status)
        val states = mutableSetOf<MangaState>()
        val mappedState = when (filters.status?.lowercase()) {
            "ongoing" -> MangaState.ONGOING
            "finished" -> MangaState.FINISHED
            else -> null
        }
        if (mappedState != null) {
            val availableStates = options?.availableStates.orEmpty()
            if (availableStates.contains(mappedState)) {
                states.add(mappedState)
            }
        }

        // Translate Demographic
        val demographics = mutableSetOf<Demographic>()
        val mappedDemographic = when (filters.demographic?.lowercase()) {
            "shounen", "shonen" -> Demographic.SHOUNEN
            "shoujo", "shojo" -> Demographic.SHOUJO
            "seinen" -> Demographic.SEINEN
            "josei" -> Demographic.JOSEI
            "kodomo" -> Demographic.KODOMO
            else -> null
        }
        if (mappedDemographic != null) {
            val availableDemographics = options?.availableDemographics.orEmpty()
            if (availableDemographics.contains(mappedDemographic)) {
                demographics.add(mappedDemographic)
            }
        }

        return MangaListFilter(
            tags = tags,
            tagsExclude = tagsExclude,
            types = types,
            states = states,
            demographics = demographics
        )
    }

    private fun findMatchingTag(genreName: String, availableTags: Collection<MangaTag>): MangaTag? {
        val name = genreName.trim()
        return availableTags.find { it.title.equals(name, ignoreCase = true) }
            ?: availableTags.find { it.title.contains(name, ignoreCase = true) || name.contains(it.title, ignoreCase = true) }
    }
}
