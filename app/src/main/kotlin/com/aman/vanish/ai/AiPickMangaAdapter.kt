package com.aman.vanish.ai

import android.animation.ValueAnimator
import android.graphics.LinearGradient
import android.graphics.Shader
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import androidx.recyclerview.widget.RecyclerView
import com.aman.vanish.databinding.ItemAiPickMangaBinding
import org.koitharu.kotatsu.parsers.model.Manga
import com.aman.vanish.ai.models.AggregatedManga

class AiPickMangaAdapter(
    private val onMangaClicked: (Manga, Int) -> Unit,
) : RecyclerView.Adapter<AiPickMangaAdapter.ViewHolder>() {

    private var items: List<AggregatedManga> = emptyList()
    private var showSkeletons: Boolean = false
    private var lastAnimatedPosition = -1

    fun submitList(newItems: List<AggregatedManga>, showSkeletons: Boolean = false) {
        this.items = newItems
        this.showSkeletons = showSkeletons
        this.lastAnimatedPosition = -1
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAiPickMangaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (showSkeletons) {
            holder.bindSkeleton()
        } else {
            holder.bind(items[position], position)
            animateSlideUp(holder.itemView, position)
        }
    }

    override fun getItemCount(): Int = if (showSkeletons) 6 else items.size

    private fun animateSlideUp(view: View, position: Int) {
        if (position > lastAnimatedPosition) {
            view.translationY = 150f
            view.alpha = 0f
            view.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(350)
                .setStartDelay(position * 50L)
                .start()
            lastAnimatedPosition = position
        }
    }

    inner class ViewHolder(private val binding: ItemAiPickMangaBinding) : RecyclerView.ViewHolder(binding.root) {

        private var shimmerAnimator: ValueAnimator? = null

        // 11.12 — Coil with explicit size(200, 300) and crossfade
        fun bind(item: AggregatedManga, position: Int) {
            shimmerAnimator?.cancel()
            shimmerAnimator = null
            binding.root.clearAnimation()

            val manga = item.manga
            val sourceName = item.sourceName

            binding.textViewTitle.text = manga.title
            binding.textViewTitle.background = null
            binding.textViewSourceBadge.text = sourceName
            setSourceBadgeColor(binding.textViewSourceBadge, sourceName)
            binding.imageViewCover.background = null
            // 11.12 — Use project-native setImageAsync (crossfade is configured globally by Coil)
            binding.imageViewCover.setImageAsync(manga.coverUrl, manga)

            // 11.9 — Accessibility content description
            binding.root.contentDescription = "${manga.title}, from $sourceName"
            binding.root.isClickable = true
            binding.root.isFocusable = true
            binding.root.setOnClickListener { onMangaClicked(manga, position) }
        }

        // 11.7 — Upgraded shimmer: sweeping gradient instead of flat alpha pulse
        fun bindSkeleton() {
            shimmerAnimator?.cancel()
            binding.root.clearAnimation()
            binding.root.setOnClickListener(null)

            val surfaceVariantColor = com.google.android.material.color.MaterialColors.getColor(
                binding.root,
                com.google.android.material.R.attr.colorSurfaceVariant,
            )
            val highlightColor = com.google.android.material.color.MaterialColors.getColor(
                binding.root,
                com.google.android.material.R.attr.colorSurface,
            )

            binding.textViewTitle.text = ""
            binding.textViewTitle.setBackgroundColor(surfaceVariantColor)
            binding.textViewSourceBadge.text = "          "
            binding.textViewSourceBadge.setBackgroundColor(surfaceVariantColor)
            binding.imageViewCover.setImageResource(android.R.color.transparent)
            binding.imageViewCover.setBackgroundColor(surfaceVariantColor)

            // Animate a shimmer sweep across the cover placeholder
            shimmerAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
                duration = 1200
                repeatMode = ValueAnimator.RESTART
                repeatCount = ValueAnimator.INFINITE
                interpolator = LinearInterpolator()
                addUpdateListener { animator ->
                    val progress = animator.animatedValue as Float
                    val width = binding.imageViewCover.width.toFloat().coerceAtLeast(1f)
                    val shimmerStart = -width + (progress * width * 3f)
                    val gradient = LinearGradient(
                        shimmerStart, 0f, shimmerStart + width, 0f,
                        intArrayOf(surfaceVariantColor, highlightColor, surfaceVariantColor),
                        floatArrayOf(0f, 0.5f, 1f),
                        Shader.TileMode.CLAMP,
                    )
                    val paint = android.graphics.Paint().apply { shader = gradient }
                    val bitmap = android.graphics.Bitmap.createBitmap(
                        binding.imageViewCover.width.coerceAtLeast(1),
                        binding.imageViewCover.height.coerceAtLeast(1),
                        android.graphics.Bitmap.Config.ARGB_8888,
                    )
                    val canvas = android.graphics.Canvas(bitmap)
                    canvas.drawRect(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat(), paint)
                    binding.imageViewCover.setImageBitmap(bitmap)
                }
                start()
            }
        }

        private fun setSourceBadgeColor(textView: android.widget.TextView, sourceName: String) {
            val context = textView.context
            val (bgColorAttr, textColorAttr) = when (sourceName.uppercase().trim()) {
                "COMIX" -> Pair(com.google.android.material.R.attr.colorSecondaryContainer, com.google.android.material.R.attr.colorOnSecondaryContainer)
                "MANGAFIRE_EN" -> Pair(com.google.android.material.R.attr.colorTertiaryContainer, com.google.android.material.R.attr.colorOnTertiaryContainer)
                "MANGADEX" -> Pair(com.google.android.material.R.attr.colorPrimaryContainer, com.google.android.material.R.attr.colorOnPrimaryContainer)
                else -> Pair(com.google.android.material.R.attr.colorSurfaceVariant, com.google.android.material.R.attr.colorOnSurfaceVariant)
            }
            val bgColor = com.google.android.material.color.MaterialColors.getColor(textView, bgColorAttr)
            val textColor = com.google.android.material.color.MaterialColors.getColor(textView, textColorAttr)
            textView.setTextColor(textColor)
            val background = textView.background
            if (background is android.graphics.drawable.GradientDrawable) {
                background.setColor(bgColor)
            } else {
                val shape = context.getDrawable(com.aman.vanish.R.drawable.bg_source_badge) as? android.graphics.drawable.GradientDrawable
                shape?.setColor(bgColor)
                textView.background = shape
            }
        }
    }
}
