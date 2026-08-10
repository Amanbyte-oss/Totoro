package com.aman.vanish.list.domain

interface QuickFilterListener {

	fun setFilterOption(option: ListFilterOption, isApplied: Boolean)

	fun toggleFilterOption(option: ListFilterOption)

	fun clearFilter()
}
