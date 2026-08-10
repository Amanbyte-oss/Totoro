package com.aman.vanish.details.ui.adapter

import android.content.Context
import com.aman.vanish.core.ui.BaseListAdapter
import com.aman.vanish.core.ui.list.OnListItemClickListener
import com.aman.vanish.core.ui.list.fastscroll.FastScroller
import com.aman.vanish.details.ui.model.ChapterListItem
import com.aman.vanish.list.ui.adapter.ListItemType
import com.aman.vanish.list.ui.adapter.listHeaderAD
import com.aman.vanish.list.ui.model.ListHeader
import com.aman.vanish.list.ui.model.ListModel

class ChaptersAdapter(
	onItemClickListener: OnListItemClickListener<ChapterListItem>,
) : BaseListAdapter<ListModel>(), FastScroller.SectionIndexer {

	private var hasVolumes = false

	init {
		addDelegate(ListItemType.HEADER, listHeaderAD(null))
		addDelegate(ListItemType.CHAPTER_LIST, chapterListItemAD(onItemClickListener))
		addDelegate(ListItemType.CHAPTER_GRID, chapterGridItemAD(onItemClickListener))
	}

	override suspend fun emit(value: List<ListModel>?) {
		super.emit(value)
		hasVolumes = value != null && value.any { it is ListHeader }
	}

	override fun getSectionText(context: Context, position: Int): CharSequence? {
		return if (hasVolumes) {
			findHeader(position)?.getText(context)
		} else {
			val chapter = (items.getOrNull(position) as? ChapterListItem)?.chapter ?: return null
			chapter.numberString()
		}
	}
}
