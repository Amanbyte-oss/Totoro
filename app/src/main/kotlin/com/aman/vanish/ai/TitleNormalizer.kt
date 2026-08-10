package com.aman.vanish.ai

object TitleNormalizer {
    fun normalize(title: String): String {
        // Normalize Unicode characters (e.g., "é" -> "e")
        val decomposed = java.text.Normalizer.normalize(title, java.text.Normalizer.Form.NFD)
        val asciiOnly = decomposed.replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
        
        var clean = asciiOnly.lowercase()
        // Remove content in brackets: (webtoon), [official]
        clean = clean.replace(Regex("\\([^)]*\\)|\\[[^]]*\\]"), "")
        
        // Remove specific suffixes
        val suffixes = listOf("season 1", "season 2", "part 1", "part 2", "ch.", "vol.")
        suffixes.forEach { suffix ->
            if (clean.endsWith(suffix)) {
                clean = clean.substring(0, clean.length - suffix.length).trim()
            }
        }
        
        // Remove punctuation: !@#$%^&*()_+-=[]{}|;':",./<>?
        clean = clean.replace(Regex("[!@#\$%^&*()_+\\-=\\[\\]{}|;':\",./<>?]"), "")
        
        // Collapse spaces
        clean = clean.replace(Regex("\\s+"), " ").trim()
        return clean
    }
}
