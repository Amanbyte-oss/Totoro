package com.aman.vanish.ai

import org.koitharu.kotatsu.parsers.model.Manga

object DeduplicationEngine {
    
    fun deduplicate(mangaList: List<Pair<Manga, String>>): List<Pair<Manga, String>> {
        if (mangaList.isEmpty()) return emptyList()

        // 1. Sort by source quality descending so that best source is preferred when resolving duplicates
        val sortedList = mangaList.sortedByDescending { getSourceQuality(it.second) }
        
        // 2. Loop and keep the highest quality source for duplicates
        val finalDeduplicated = mutableListOf<Pair<Manga, String>>()
        for (item in sortedList) {
            val normTitle = TitleNormalizer.normalize(item.first.title)
            val isDuplicate = finalDeduplicated.any { existing ->
                val existingNorm = TitleNormalizer.normalize(existing.first.title)
                normTitle == existingNorm || getLevenshteinDistance(normTitle, existingNorm) <= 2
            }
            if (!isDuplicate) {
                finalDeduplicated.add(item)
            }
        }

        // Return deduplicated list sorted by source quality (best sources first)
        return finalDeduplicated.sortedByDescending { getSourceQuality(it.second) }
    }

    private fun getSourceQuality(sourceName: String): Int {
        return when (sourceName.uppercase().trim()) {
            "MANGADEX" -> 100
            "MANGAFIRE_EN" -> 90
            "COMIX" -> 80
            else -> 0
        }
    }

    private fun getLevenshteinDistance(s: String, t: String): Int {
        if (s == t) return 0
        if (s.isEmpty()) return t.length
        if (t.isEmpty()) return s.length

        val d = Array(s.length + 1) { IntArray(t.length + 1) }

        for (i in 0..s.length) d[i][0] = i
        for (j in 0..t.length) d[0][j] = j

        for (i in 1..s.length) {
            for (j in 1..t.length) {
                val cost = if (s[i - 1] == t[j - 1]) 0 else 1
                d[i][j] = minOf(
                    d[i - 1][j] + 1,
                    d[i][j - 1] + 1,
                    d[i - 1][j - 1] + cost
                )
            }
        }
        return d[s.length][t.length]
    }
}
