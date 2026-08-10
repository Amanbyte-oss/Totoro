package com.aman.vanish.reader.ui

import com.aman.vanish.bookmarks.domain.Bookmark
import org.koitharu.kotatsu.parsers.model.MangaChapter
import com.aman.vanish.reader.ui.pager.ReaderPage

interface ReaderNavigationCallback {

	fun onPageSelected(page: ReaderPage): Boolean

	fun onChapterSelected(chapter: MangaChapter): Boolean

	fun onBookmarkSelected(bookmark: Bookmark): Boolean = onPageSelected(
		ReaderPage(bookmark.toMangaPage(), bookmark.page, bookmark.chapterId),
	)
}
