package com.aman.vanish.explore.ui.adapter

import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding
import com.aman.vanish.R
import com.aman.vanish.core.model.getSummary
import com.aman.vanish.core.model.getTitle
import com.aman.vanish.core.ui.BaseListAdapter
import com.aman.vanish.core.ui.list.AdapterDelegateClickListenerAdapter
import com.aman.vanish.core.ui.list.OnListItemClickListener
import com.aman.vanish.core.util.ext.drawableStart
import com.aman.vanish.core.util.ext.recyclerView
import com.aman.vanish.core.util.ext.setProgressIcon
import com.aman.vanish.core.util.ext.setTooltipCompat
import com.aman.vanish.core.util.ext.textAndVisible
import com.aman.vanish.databinding.ItemExploreButtonsBinding
import com.aman.vanish.databinding.ItemExploreSourceGridBinding
import com.aman.vanish.databinding.ItemExploreSourceListBinding
import com.aman.vanish.databinding.ItemRecommendationBinding
import com.aman.vanish.databinding.ItemRecommendationMangaBinding
import com.aman.vanish.explore.ui.model.ExploreButtons
import com.aman.vanish.explore.ui.model.MangaSourceItem
import com.aman.vanish.explore.ui.model.RecommendationsItem
import com.aman.vanish.list.ui.adapter.ListItemType
import com.aman.vanish.list.ui.model.ListModel
import com.aman.vanish.list.ui.model.MangaCompactListModel
import org.koitharu.kotatsu.parsers.model.Manga

fun exploreButtonsAD(
	clickListener: View.OnClickListener,
) = adapterDelegateViewBinding<ExploreButtons, ListModel, ItemExploreButtonsBinding>(
	{ layoutInflater, parent -> ItemExploreButtonsBinding.inflate(layoutInflater, parent, false) },
) {

	binding.buttonBookmarks.setOnClickListener(clickListener)
	binding.buttonDownloads.setOnClickListener(clickListener)
	binding.buttonLocal.setOnClickListener(clickListener)
	binding.buttonAiPick.setOnClickListener(clickListener)
	binding.buttonPresets.setOnClickListener(clickListener)
	binding.buttonPresetsDropdown.setOnClickListener(clickListener)

	bind {
		val presetName = item.activePresetName
		if (presetName != null) {
			binding.buttonPresets.text = presetName
		} else {
			binding.buttonPresets.setText(R.string.source_presets)
		}
	}
}

fun exploreRecommendationItemAD(
	itemClickListener: OnListItemClickListener<Manga>,
) = adapterDelegateViewBinding<RecommendationsItem, ListModel, ItemRecommendationBinding>(
	{ layoutInflater, parent -> ItemRecommendationBinding.inflate(layoutInflater, parent, false) },
) {

	val adapter = BaseListAdapter<MangaCompactListModel>()
		.addDelegate(ListItemType.MANGA_LIST, recommendationMangaItemAD(itemClickListener))
	binding.pager.adapter = adapter
	binding.pager.recyclerView?.isNestedScrollingEnabled = false
	binding.dots.bindToViewPager(binding.pager)

	bind {
		adapter.items = item.manga
	}
}

fun recommendationMangaItemAD(
	itemClickListener: OnListItemClickListener<Manga>,
) = adapterDelegateViewBinding<MangaCompactListModel, MangaCompactListModel, ItemRecommendationMangaBinding>(
	{ layoutInflater, parent -> ItemRecommendationMangaBinding.inflate(layoutInflater, parent, false) },
) {

	binding.root.setOnClickListener { v ->
		itemClickListener.onItemClick(item.manga, v)
	}
	bind {
		binding.textViewTitle.text = item.manga.title
		binding.textViewSubtitle.textAndVisible = item.subtitle
		binding.imageViewCover.setImageAsync(item.manga.coverUrl, item.manga.source)
	}
}


fun exploreSourceListItemAD(
	listener: OnListItemClickListener<MangaSourceItem>,
) = adapterDelegateViewBinding<MangaSourceItem, ListModel, ItemExploreSourceListBinding>(
	{ layoutInflater, parent ->
		ItemExploreSourceListBinding.inflate(
			layoutInflater,
			parent,
			false,
		)
	},
	on = { item, _, _ -> item is MangaSourceItem && !item.isGrid },
) {

	AdapterDelegateClickListenerAdapter(this, listener).attach(itemView)
	val iconPinned = ContextCompat.getDrawable(context, R.drawable.ic_pin_small)

	bind {
		binding.textViewTitle.text = item.source.getTitle(context)
		binding.textViewTitle.drawableStart = if (item.source.isPinned) iconPinned else null
		binding.textViewSubtitle.text = item.source.getSummary(context)
		binding.imageViewIcon.setImageAsync(item.source)
	}
}

fun exploreSourceGridItemAD(
	listener: OnListItemClickListener<MangaSourceItem>,
) = adapterDelegateViewBinding<MangaSourceItem, ListModel, ItemExploreSourceGridBinding>(
	{ layoutInflater, parent ->
		ItemExploreSourceGridBinding.inflate(
			layoutInflater,
			parent,
			false,
		)
	},
	on = { item, _, _ -> item is MangaSourceItem && item.isGrid },
) {

	AdapterDelegateClickListenerAdapter(this, listener).attach(itemView)
	val iconPinned = ContextCompat.getDrawable(context, R.drawable.ic_pin_small)

	bind {
		val title = item.source.getTitle(context)
		itemView.setTooltipCompat(
			buildSpannedString {
				bold {
					append(title)
				}
				appendLine()
				append(item.source.getSummary(context))
			},
		)
		binding.textViewTitle.text = title
		binding.textViewTitle.drawableStart = if (item.source.isPinned) iconPinned else null
		binding.imageViewIcon.setImageAsync(item.source)
	}
}
