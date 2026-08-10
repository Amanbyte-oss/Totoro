package com.aman.vanish.favourites.ui.categories

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.aman.vanish.core.model.FavouriteCategory
import com.aman.vanish.core.ui.list.OnListItemClickListener

interface FavouriteCategoriesListListener : OnListItemClickListener<FavouriteCategory?> {

	fun onDragHandleTouch(holder: RecyclerView.ViewHolder): Boolean

	fun onEditClick(item: FavouriteCategory, view: View)

	fun onShowAllClick(isChecked: Boolean)
}
