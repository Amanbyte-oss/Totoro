package com.aman.vanish.picker.ui.page

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import com.aman.vanish.R
import com.aman.vanish.core.exceptions.resolve.SnackbarErrorObserver
import com.aman.vanish.core.prefs.AppSettings
import com.aman.vanish.core.ui.BaseFragment
import com.aman.vanish.core.ui.list.BoundsScrollListener
import com.aman.vanish.core.ui.list.OnListItemClickListener
import com.aman.vanish.core.ui.util.PagerNestedScrollHelper
import com.aman.vanish.core.util.ext.consumeAll
import com.aman.vanish.core.util.ext.observe
import com.aman.vanish.core.util.ext.observeEvent
import com.aman.vanish.core.util.ext.showOrHide
import com.aman.vanish.databinding.FragmentPagesBinding
import com.aman.vanish.details.ui.pager.pages.PageThumbnail
import com.aman.vanish.details.ui.pager.pages.PageThumbnailAdapter
import com.aman.vanish.list.ui.GridSpanResolver
import com.aman.vanish.list.ui.adapter.ListItemType
import com.aman.vanish.list.ui.adapter.TypedListSpacingDecoration
import com.aman.vanish.list.ui.model.ListModel
import org.koitharu.kotatsu.parsers.util.ifNullOrEmpty
import com.aman.vanish.picker.ui.PageImagePickActivity
import javax.inject.Inject

@AndroidEntryPoint
class PagePickerFragment :
	BaseFragment<FragmentPagesBinding>(),
	OnListItemClickListener<PageThumbnail> {

	@Inject
	lateinit var settings: AppSettings

	private val viewModel by viewModels<PagePickerViewModel>()

	private var thumbnailsAdapter: PageThumbnailAdapter? = null
	private var spanResolver: GridSpanResolver? = null
	private var scrollListener: ScrollListener? = null

	private val spanSizeLookup = SpanSizeLookup()

	override fun onCreateViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentPagesBinding {
		return FragmentPagesBinding.inflate(inflater, container, false)
	}

	override fun onViewBindingCreated(binding: FragmentPagesBinding, savedInstanceState: Bundle?) {
		super.onViewBindingCreated(binding, savedInstanceState)
		spanResolver = GridSpanResolver(binding.root.resources)
		thumbnailsAdapter = PageThumbnailAdapter(
			clickListener = this@PagePickerFragment,
		)
		viewModel.gridScale.observe(viewLifecycleOwner, ::onGridScaleChanged) // before rv initialization
		with(binding.recyclerView) {
			addItemDecoration(TypedListSpacingDecoration(context, false))
			adapter = thumbnailsAdapter
			setHasFixedSize(true)
			PagerNestedScrollHelper(this).bind(viewLifecycleOwner)
			addOnLayoutChangeListener(spanResolver)
			addOnScrollListener(ScrollListener().also { scrollListener = it })
			(layoutManager as GridLayoutManager).let {
				it.spanSizeLookup = spanSizeLookup
				it.spanCount = checkNotNull(spanResolver).spanCount
			}
		}
		viewModel.thumbnails.observe(viewLifecycleOwner, ::onThumbnailsChanged)
		viewModel.isNoChapters.observe(viewLifecycleOwner, ::onNoChaptersChanged)
		viewModel.onError.observeEvent(viewLifecycleOwner, SnackbarErrorObserver(binding.recyclerView, this))
		viewModel.isLoading.observe(viewLifecycleOwner) { binding.progressBar.showOrHide(it) }
		viewModel.isLoadingDown.observe(viewLifecycleOwner) { binding.progressBarBottom.showOrHide(it) }
		viewModel.manga.observe(viewLifecycleOwner, Lifecycle.State.RESUMED) {
			activity?.title = it?.toManga()?.title.ifNullOrEmpty { getString(R.string.pick_manga_page) }
		}
	}

	override fun onDestroyView() {
		spanResolver = null
		scrollListener = null
		thumbnailsAdapter = null
		spanSizeLookup.invalidateCache()
		super.onDestroyView()
	}

	override fun onApplyWindowInsets(v: View, insets: WindowInsetsCompat): WindowInsetsCompat {
		val typeBask = WindowInsetsCompat.Type.systemBars()
		val barsInsets = insets.getInsets(typeBask)
		viewBinding?.recyclerView?.setPadding(
			barsInsets.left,
			barsInsets.top,
			barsInsets.right,
			barsInsets.bottom,
		)
		return insets.consumeAll(typeBask)
	}

	override fun onItemClick(item: PageThumbnail, view: View) {
		val manga = viewModel.manga.value?.toManga() ?: return
		(activity as PageImagePickActivity).onPagePicked(manga, item.page)
	}

	override fun onItemLongClick(item: PageThumbnail, view: View): Boolean = false

	override fun onItemContextClick(item: PageThumbnail, view: View): Boolean = false

	private suspend fun onThumbnailsChanged(list: List<ListModel>) {
		val adapter = thumbnailsAdapter ?: return
		adapter.emit(list)
		spanSizeLookup.invalidateCache()
		viewBinding?.recyclerView?.let {
			scrollListener?.postInvalidate(it)
		}
	}

	private fun onGridScaleChanged(scale: Float) {
		spanSizeLookup.invalidateCache()
		spanResolver?.setGridSize(scale, requireViewBinding().recyclerView)
	}

	private fun onNoChaptersChanged(isNoChapters: Boolean) {
		with(viewBinding ?: return) {
			textViewHolder.isVisible = isNoChapters
			recyclerView.isInvisible = isNoChapters
		}
	}

	private inner class ScrollListener : BoundsScrollListener(3, 3) {

		override fun onScrolledToStart(recyclerView: RecyclerView) = Unit

		override fun onScrolledToEnd(recyclerView: RecyclerView) {
			viewModel.loadNextChapter()
		}
	}

	private inner class SpanSizeLookup : GridLayoutManager.SpanSizeLookup() {

		init {
			isSpanIndexCacheEnabled = true
			isSpanGroupIndexCacheEnabled = true
		}

		override fun getSpanSize(position: Int): Int {
			val total = (viewBinding?.recyclerView?.layoutManager as? GridLayoutManager)?.spanCount ?: return 1
			return when (thumbnailsAdapter?.getItemViewType(position)) {
				ListItemType.PAGE_THUMB.ordinal -> 1
				else -> total
			}
		}

		fun invalidateCache() {
			invalidateSpanGroupIndexCache()
			invalidateSpanIndexCache()
		}
	}
}
