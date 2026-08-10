package com.aman.vanish.ai

import com.aman.vanish.ai.db.AiPromptHistoryEntity
import com.aman.vanish.ai.models.AggregatedManga
import com.aman.vanish.main.ui.owners.AppBarOwner
import com.aman.vanish.core.util.ext.consumeAll
import android.content.res.Configuration
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher

import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aman.vanish.R
import com.aman.vanish.core.nav.router
import com.aman.vanish.core.ui.BaseFragment
import com.aman.vanish.core.util.ext.observe
import com.aman.vanish.databinding.FragmentAiPickBinding
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koitharu.kotatsu.parsers.model.Manga

@AndroidEntryPoint
class AiPickFragment : BaseFragment<FragmentAiPickBinding>() {

    private val viewModel by viewModels<AiPickViewModel>()
    
    // 1.C — Nullify adapter reference to prevent memory leaks on view destruction
    private var adapter: AiPickMangaAdapter? = null

    private val profanityList = listOf("fuck", "shit", "asshole", "bitch", "bastard")

    private var thinkingAnimator: android.animation.ValueAnimator? = null
    private var debounceJob: Job? = null
    private var countDownTimer: CountDownTimer? = null

    override fun onCreateViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentAiPickBinding {
        return FragmentAiPickBinding.inflate(inflater, container, false)
    }

    override fun onViewBindingCreated(binding: FragmentAiPickBinding, savedInstanceState: Bundle?) {
        super.onViewBindingCreated(binding, savedInstanceState)
        (activity as? AppBarOwner)?.appBar?.visibility = View.GONE

        android.util.Log.i("AiPickFlow", "AI Pick screen opened")

        // 11.5 — FTUE onboarding (show only once)
        if (AiPickOnboardingBottomSheet.shouldShow(requireContext())) {
            AiPickOnboardingBottomSheet().show(childFragmentManager, AiPickOnboardingBottomSheet.TAG)
        }

        // 11.13 & 10. — Adapt columns by screen width
        val config = resources.configuration
        val widthDp = config.screenWidthDp
        val isLandscape = config.orientation == Configuration.ORIENTATION_LANDSCAPE
        val spanCount = when {
            widthDp < 360 -> if (isLandscape) 3 else 2
            widthDp in 360..599 -> if (isLandscape) 4 else 3
            else -> if (isLandscape) 5 else 4
        }

        // 1.C — Adapter callbacks without strong fragment reference context
        adapter = AiPickMangaAdapter { manga, position ->
            viewModel.logResultClicked(position, manga.source.name)
            openMangaDetails(manga)
        }
        binding.recyclerViewResults.layoutManager = GridLayoutManager(context, spanCount)
        binding.recyclerViewResults.adapter = adapter

        // Dismiss keyboard when results are scrolled
        binding.recyclerViewResults.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) dismissKeyboard()
            }
        })

        // Keyboard auto-focus
        binding.editTextQuery.requestFocus()
        binding.editTextQuery.postDelayed({
            val imm = ContextCompat.getSystemService(requireContext(), InputMethodManager::class.java)
            imm?.showSoftInput(binding.editTextQuery, InputMethodManager.SHOW_IMPLICIT)
        }, 200)

        // 10. — Input debouncing: wait 300ms after last keystroke before enabling send button
        binding.editTextQuery.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val len = s?.length ?: 0
                binding.textViewCharCounter.text = getString(R.string.ai_char_counter, len)
                binding.textViewCharCounter.setTextColor(
                    if (len > 180) ContextCompat.getColor(requireContext(), com.google.android.material.R.color.design_error)
                    else ContextCompat.getColor(requireContext(), android.R.color.darker_gray),
                )
                
                binding.buttonSend.isEnabled = false
                debounceJob?.cancel()
                debounceJob = viewLifecycleOwner.lifecycleScope.launch {
                    delay(300)
                    if (AiConfig.GROQ_API_KEY != "gsk-your-groq-api-key-here") {
                        binding.buttonSend.isEnabled = len > 0 && !s.isNullOrBlank()
                    }
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        binding.buttonSend.isEnabled = !binding.editTextQuery.text.isNullOrBlank() &&
            AiConfig.GROQ_API_KEY != "gsk-your-groq-api-key-here"

        binding.editTextQuery.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) { sendQuery(); true } else false
        }

        binding.buttonBack.setOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }
        binding.buttonSend.setOnClickListener { sendQuery() }
        binding.buttonClearResults.setOnClickListener {
            binding.editTextQuery.text = null
            binding.editTextQuery.error = null
            viewModel.clear()
        }
        binding.buttonRetry.setOnClickListener { viewModel.retry() }

        // Suggestion chips
        binding.chipExample1.setOnClickListener { submitPredefinedQuery("Action romance") }
        binding.chipExample2.setOnClickListener { submitPredefinedQuery("Isekai fantasy") }

        // SwipeRefreshLayout (Pull-to-refresh forces cache bypass)
        binding.swipeRefreshLayout.setOnRefreshListener {
            val rawInput = binding.editTextQuery.text.toString()
            val validationResult = validateAndSanitizeInput(rawInput)
            if (validationResult.isSuccess) {
                binding.editTextQuery.error = null
                viewModel.sendQuery(validationResult.getOrThrow(), forceRefresh = true)
            } else {
                binding.swipeRefreshLayout.isRefreshing = false
                binding.editTextQuery.error = validationResult.exceptionOrNull()?.message
            }
        }

        // 9.A — Runtime API Key Safety Check
        if (AiConfig.GROQ_API_KEY == "gsk-your-groq-api-key-here") {
            binding.editTextQuery.isEnabled = false
            binding.buttonSend.isEnabled = false
            listOf(
                binding.chipExample1, binding.chipExample2,
            ).forEach { it.isClickable = false }
            view?.post {
                Snackbar.make(
                    binding.root,
                    "Developer: Configure API key in AiConfig.kt",
                    Snackbar.LENGTH_INDEFINITE,
                ).show()
            }
        }

        // Overflow menu (Clear history)
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
                menu.add(Menu.NONE, MENU_CLEAR_HISTORY, Menu.NONE, R.string.ai_clear_history)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
            }
            override fun onMenuItemSelected(item: MenuItem): Boolean {
                return if (item.itemId == MENU_CLEAR_HISTORY) {
                    viewModel.clearHistory()
                    Snackbar.make(binding.root, "AI search history cleared", Snackbar.LENGTH_SHORT).show()
                    true
                } else false
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        // Observe states
        viewModel.uiState.observe(viewLifecycleOwner) { renderState(it) }
        viewModel.recentPrompts.observe(viewLifecycleOwner) { renderHistoryChips(it) }
        viewModel.remainingQuota.observe(viewLifecycleOwner) { quota ->
            binding.textViewQuotaBadge.text = "AI: $quota remaining"
            binding.textViewQuotaBadge.isVisible = quota < 200
        }
    }



    private fun renderHistoryChips(history: List<AiPromptHistoryEntity>) {
        val binding = requireViewBinding()
        binding.chipGroupHistory.removeAllViews()
        if (history.isEmpty()) {
            binding.chipGroupHistory.isVisible = false
            return
        }
        binding.chipGroupHistory.isVisible = true
        history.take(15).forEach { entry ->
            val chip = Chip(requireContext()).apply {
                text = entry.prompt
                isClickable = true
                isFocusable = true
                contentDescription = "Recent search: ${entry.prompt}"
                setOnClickListener { submitPredefinedQuery(entry.prompt) }
            }
            binding.chipGroupHistory.addView(chip)
        }
    }

    private fun sendQuery() {
        val binding = requireViewBinding()
        val rawInput = binding.editTextQuery.text.toString()
        val validationResult = validateAndSanitizeInput(rawInput)
        if (validationResult.isSuccess) {
            binding.editTextQuery.error = null
            dismissKeyboard()
            val queryText = validationResult.getOrThrow()
            viewModel.sendQuery(queryText)
        } else {
            binding.editTextQuery.error = validationResult.exceptionOrNull()?.message
        }
    }

    private fun submitPredefinedQuery(queryText: String) {
        val binding = requireViewBinding()
        binding.editTextQuery.setText(queryText)
        binding.editTextQuery.error = null
        dismissKeyboard()
        viewModel.sendQuery(queryText)
    }

    private fun validateAndSanitizeInput(input: String): Result<String> {
        var sanitized = input
            .replace(Regex("<[^>]*>"), "")
            .replace("\u0000", "")
            .trim()

        sanitized = sanitized.replace(Regex("([^a-zA-Z0-9\\s])\\1{2,}"), "$1")

        if (sanitized.isEmpty()) return Result.failure(Exception("Please enter a query"))
        if (sanitized.length > 200) return Result.failure(Exception("Query cannot exceed 200 characters"))

        val lower = sanitized.lowercase()
        for (word in profanityList) {
            if (lower.contains(word)) {
                Toast.makeText(context, "Please keep it clean 🙏", Toast.LENGTH_SHORT).show()
                return Result.failure(Exception("Inappropriate language detected"))
            }
        }

        return Result.success(sanitized)
    }

    private fun dismissKeyboard() {
        val view = view?.findFocus() ?: requireViewBinding().editTextQuery
        val imm = ContextCompat.getSystemService(requireContext(), InputMethodManager::class.java)
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun openMangaDetails(manga: Manga) {
        router.openDetails(manga)
    }

    private fun renderState(state: AiPickUiState) {
        val binding = requireViewBinding()
        countDownTimer?.cancel()
        countDownTimer = null

        // Animate all layout changes for a fast and smooth feel
        androidx.transition.TransitionManager.beginDelayedTransition(binding.root)

        when (state) {
            is AiPickUiState.Idle -> {
                stopThinkingAnimation()
                binding.cardBanner.isVisible = false
                binding.cardCachedBadge.isVisible = false
                binding.layoutIdle.isVisible = true
                binding.progressBarLoading.isVisible = false
                binding.layoutError.isVisible = false
                binding.swipeRefreshLayout.isVisible = false
                binding.swipeRefreshLayout.isRefreshing = false
                binding.buttonClearResults.isVisible = false
                if (AiConfig.GROQ_API_KEY != "gsk-your-groq-api-key-here") {
                    binding.editTextQuery.isEnabled = true
                    binding.buttonSend.isEnabled = !binding.editTextQuery.text.isNullOrBlank()
                }
                binding.textViewLoadingStatus.isVisible = false
                binding.textViewIdleTitle.text = "What are you in the mood for?"
                binding.textViewIdleDesc.text = "Describe a genre, type, or vibe"
                binding.textAiMessage.isVisible = false
            }
            is AiPickUiState.Loading -> {
                binding.cardBanner.isVisible = false
                binding.cardCachedBadge.isVisible = false
                binding.layoutIdle.isVisible = false
                binding.progressBarLoading.isVisible = true
                binding.swipeRefreshLayout.isVisible = false
                binding.buttonClearResults.isVisible = false
                binding.editTextQuery.isEnabled = false
                binding.buttonSend.isEnabled = false
                binding.textViewLoadingStatus.isVisible = true
                binding.textAiMessage.isVisible = false
                
                binding.textViewLoadingStatus.announceForAccessibility(state.message)
                
                if (state.message.contains("Thinking")) {
                    startThinkingAnimation(binding.textViewLoadingStatus)
                } else {
                    stopThinkingAnimation()
                    binding.textViewLoadingStatus.text = state.message
                }
                
                binding.layoutError.isVisible = false
            }
            is AiPickUiState.Error -> {
                stopThinkingAnimation()
                binding.cardBanner.isVisible = false
                binding.cardCachedBadge.isVisible = false
                binding.layoutIdle.isVisible = false
                binding.progressBarLoading.isVisible = false
                binding.layoutError.isVisible = true
                binding.swipeRefreshLayout.isVisible = false
                binding.swipeRefreshLayout.isRefreshing = false
                binding.buttonClearResults.isVisible = false
                binding.textAiMessage.isVisible = false
                
                val rawMsg = state.message
                val displayMsg = when {
                    rawMsg.contains("offline") || rawMsg.contains("All manga sources are offline") -> 
                        "Manga sources are temporarily unavailable"
                    rawMsg.contains("key not configured") || rawMsg.contains("key not set") ->
                        "Developer: Configure API key in AiConfig.kt"
                    else -> rawMsg
                }
                binding.textViewErrorMessage.text = displayMsg
                binding.buttonRetry.isEnabled = true

                if (rawMsg.contains("Rate limit")) {
                    binding.buttonRetry.isEnabled = false
                    countDownTimer = object : CountDownTimer(30000, 1000) {
                        override fun onTick(millisUntilFinished: Long) {
                            binding.textViewErrorMessage.text = "Rate limit hit. Try again in ${millisUntilFinished / 1000} seconds."
                        }
                        override fun onFinish() {
                            binding.textViewErrorMessage.text = "Rate limit check completed. You can try again now."
                            binding.buttonRetry.isEnabled = true
                        }
                    }.start()
                }

                if (AiConfig.GROQ_API_KEY != "gsk-your-groq-api-key-here") {
                    binding.editTextQuery.isEnabled = true
                    binding.buttonSend.isEnabled = !binding.editTextQuery.text.isNullOrBlank()
                }
                binding.textViewLoadingStatus.isVisible = false
            }
            is AiPickUiState.Success -> {
                stopThinkingAnimation()
                binding.progressBarLoading.isVisible = false
                binding.layoutError.isVisible = false
                binding.buttonClearResults.isVisible = true
                if (AiConfig.GROQ_API_KEY != "gsk-your-groq-api-key-here") {
                    binding.editTextQuery.isEnabled = true
                    binding.buttonSend.isEnabled = !binding.editTextQuery.text.isNullOrBlank()
                }
                binding.swipeRefreshLayout.isRefreshing = false
                binding.textViewLoadingStatus.isVisible = false

                if (state.mangaList.isEmpty()) {
                    binding.layoutIdle.isVisible = true
                    binding.swipeRefreshLayout.isVisible = false
                    binding.textViewIdleTitle.text = "No matches found"
                    binding.textViewIdleDesc.text = "Try: 'action romance', 'isekai fantasy'"
                    binding.textAiMessage.isVisible = false
                } else {
                    binding.layoutIdle.isVisible = false

                    // Fade in aiMessage TextView
                    binding.textAiMessage.text = state.aiMessage
                    binding.textAiMessage.visibility = View.VISIBLE
                    binding.textAiMessage.alpha = 0f
                    binding.textAiMessage.animate().alpha(1f).setDuration(300).start()

                    // Fade in SwipeRefreshLayout/RecyclerView
                    binding.swipeRefreshLayout.alpha = 0f
                    binding.swipeRefreshLayout.isVisible = true
                    binding.swipeRefreshLayout.animate().alpha(1f).setDuration(300).start()

                    adapter?.submitList(state.mangaList, showSkeletons = false)
                }
            }
        }
    }

    private fun startThinkingAnimation(statusView: TextView) {
        stopThinkingAnimation()
        statusView.text = "Thinking..."
        thinkingAnimator = android.animation.ObjectAnimator.ofFloat(statusView, "alpha", 0.4f, 1f, 0.4f).apply {
            duration = 1200
            repeatCount = android.animation.ValueAnimator.INFINITE
            start()
        }
    }

    private fun stopThinkingAnimation() {
        thinkingAnimator?.cancel()
        thinkingAnimator = null
        viewBinding?.textViewLoadingStatus?.alpha = 1f
    }

    override fun onDestroyView() {
        val binding = viewBinding
        if (binding != null) {
            binding.recyclerViewResults.adapter = null
            binding.recyclerViewResults.clearOnScrollListeners()
        }
        stopThinkingAnimation()
        debounceJob?.cancel()
        debounceJob = null
        countDownTimer?.cancel()
        countDownTimer = null
        adapter = null
        super.onDestroyView()
    }

    override fun onApplyWindowInsets(v: View, insets: WindowInsetsCompat): WindowInsetsCompat {
        val type = WindowInsetsCompat.Type.ime() or WindowInsetsCompat.Type.systemBars()
        val barsInsets = insets.getInsets(type)
        v.setPadding(
            barsInsets.left,
            0,
            barsInsets.right,
            barsInsets.bottom,
        )
        val binding = viewBinding
        if (binding != null) {
            val offset = (8 * resources.displayMetrics.density).toInt()
            
            val backLp = binding.buttonBack.layoutParams as ConstraintLayout.LayoutParams
            backLp.topMargin = barsInsets.top + offset
            binding.buttonBack.layoutParams = backLp

            val clearLp = binding.buttonClearResults.layoutParams as ConstraintLayout.LayoutParams
            clearLp.topMargin = barsInsets.top + offset
            binding.buttonClearResults.layoutParams = clearLp

            binding.buttonBack.setPadding(0, 0, 0, 0)
            binding.buttonClearResults.setPadding(0, 0, 0, 0)
        }
        return insets.consumeAll(type)
    }

    companion object {
        private const val MENU_CLEAR_HISTORY = 1001
    }
}
