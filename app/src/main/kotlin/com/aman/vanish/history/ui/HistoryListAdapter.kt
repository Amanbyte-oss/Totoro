package com.aman.vanish.history.ui

import android.content.Context
import com.aman.vanish.core.ui.list.fastscroll.FastScroller
import com.aman.vanish.list.ui.adapter.MangaListAdapter
import com.aman.vanish.list.ui.adapter.MangaListListener
import com.aman.vanish.list.ui.size.ItemSizeResolver

class HistoryListAdapter(
	listener: MangaListListener,
	sizeResolver: ItemSizeResolver,
) : MangaListAdapter(listener, sizeResolver), FastScroller.SectionIndexer {

	override fun getSectionText(context: Context, position: Int): CharSequence? {
		return findHeader(position)?.getText(context)
	}
}
