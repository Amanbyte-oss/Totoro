package com.aman.vanish.reader.ui

import com.aman.vanish.reader.ui.pager.ReaderPage

data class ReaderContent(
	val pages: List<ReaderPage>,
	val state: ReaderState?
)