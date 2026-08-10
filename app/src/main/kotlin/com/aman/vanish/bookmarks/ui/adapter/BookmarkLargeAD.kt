package com.aman.vanish.bookmarks.ui.adapter

import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding
import com.aman.vanish.bookmarks.domain.Bookmark
import com.aman.vanish.core.ui.list.AdapterDelegateClickListenerAdapter
import com.aman.vanish.core.ui.list.OnListItemClickListener
import com.aman.vanish.databinding.ItemBookmarkLargeBinding
import com.aman.vanish.list.ui.model.ListModel

fun bookmarkLargeAD(
	clickListener: OnListItemClickListener<Bookmark>,
) = adapterDelegateViewBinding<Bookmark, ListModel, ItemBookmarkLargeBinding>(
	{ inflater, parent -> ItemBookmarkLargeBinding.inflate(inflater, parent, false) },
) {
	AdapterDelegateClickListenerAdapter(this, clickListener).attach(itemView)

	bind {
		binding.imageViewThumb.setImageAsync(item)
		binding.progressView.setProgress(item.percent, false)
	}
}
