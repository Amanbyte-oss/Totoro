package com.aman.vanish.local.domain

import com.aman.vanish.core.util.MultiMutex
import org.koitharu.kotatsu.parsers.model.Manga
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MangaLock @Inject constructor() : MultiMutex<Manga>()
