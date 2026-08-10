package com.aman.vanish.bookmarks.ui.adapter

import android.content.Context
import com.aman.vanish.bookmarks.domain.Bookmark
import com.aman.vanish.core.ui.BaseListAdapter
import com.aman.vanish.core.ui.list.OnListItemClickListener
import com.aman.vanish.core.ui.list.fastscroll.FastScroller
import com.aman.vanish.list.ui.adapter.ListHeaderClickListener
import com.aman.vanish.list.ui.adapter.ListItemType
import com.aman.vanish.list.ui.adapter.emptyStateListAD
import com.aman.vanish.list.ui.adapter.errorStateListAD
import com.aman.vanish.list.ui.adapter.listHeaderAD
import com.aman.vanish.list.ui.adapter.loadingFooterAD
import com.aman.vanish.list.ui.adapter.loadingStateAD
import com.aman.vanish.list.ui.model.ListModel

class BookmarksAdapter(
	clickListener: OnListItemClickListener<Bookmark>,
	headerClickListener: ListHeaderClickListener?,
) : BaseListAdapter<ListModel>(), FastScroller.SectionIndexer {

	init {
		addDelegate(ListItemType.PAGE_THUMB, bookmarkLargeAD(clickListener))
		addDelegate(ListItemType.HEADER, listHeaderAD(headerClickListener))
		addDelegate(ListItemType.STATE_ERROR, errorStateListAD(null))
		addDelegate(ListItemType.FOOTER_LOADING, loadingFooterAD())
		addDelegate(ListItemType.STATE_LOADING, loadingStateAD())
		addDelegate(ListItemType.STATE_EMPTY, emptyStateListAD(null))
	}

	override fun getSectionText(context: Context, position: Int): CharSequence? {
		return findHeader(position)?.getText(context)
	}
}
