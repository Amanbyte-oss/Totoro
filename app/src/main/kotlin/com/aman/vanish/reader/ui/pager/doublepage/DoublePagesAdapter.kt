package com.aman.vanish.reader.ui.pager.doublepage

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import com.aman.vanish.core.exceptions.resolve.ExceptionResolver
import com.aman.vanish.core.os.NetworkState
import com.aman.vanish.databinding.ItemPageBinding
import com.aman.vanish.reader.domain.PageLoader
import com.aman.vanish.reader.ui.config.ReaderSettings
import com.aman.vanish.reader.ui.pager.BaseReaderAdapter

class DoublePagesAdapter(
	private val lifecycleOwner: LifecycleOwner,
	loader: PageLoader,
	readerSettingsProducer: ReaderSettings.Producer,
	networkState: NetworkState,
	exceptionResolver: ExceptionResolver,
) : BaseReaderAdapter<DoublePageHolder>(loader, readerSettingsProducer, networkState, exceptionResolver) {

	override fun onBindViewHolder(holder: DoublePageHolder, position: Int) {
		val item = getItem(position)
		if (item.index < 0) {
			holder.bindSpacer()
		} else {
			super.onBindViewHolder(holder, position)
		}
	}

	override fun onCreateViewHolder(
		parent: ViewGroup,
		loader: PageLoader,
		readerSettingsProducer: ReaderSettings.Producer,
		networkState: NetworkState,
		exceptionResolver: ExceptionResolver,
	) = DoublePageHolder(
		owner = lifecycleOwner,
		binding = ItemPageBinding.inflate(LayoutInflater.from(parent.context), parent, false),
		loader = loader,
		readerSettingsProducer = readerSettingsProducer,
		networkState = networkState,
		exceptionResolver = exceptionResolver,
	)
}
