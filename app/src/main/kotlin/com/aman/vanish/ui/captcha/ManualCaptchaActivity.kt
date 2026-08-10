package com.aman.vanish.ui.captcha

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.aman.vanish.R
import com.aman.vanish.core.network.webview.WebViewExecutor
import com.aman.vanish.core.exceptions.resolve.CaptchaResultBus
import com.aman.vanish.databinding.ActivityManualCaptchaBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.net.URI
import javax.inject.Inject

@AndroidEntryPoint
class ManualCaptchaActivity : AppCompatActivity() {

    companion object {
        const val TAG = "ManualCaptchaActivity"
        const val EXTRA_URL = "url"
        const val EXTRA_SOURCE_NAME = "source_name"
        const val EXTRA_USER_AGENT = "source_user_agent"
        const val RESULT_SOLVED = RESULT_FIRST_USER + 1
        const val RESULT_CANCELLED = RESULT_FIRST_USER + 2
        
        fun newIntent(context: Context, url: String, sourceName: String): Intent {
            return Intent(context, ManualCaptchaActivity::class.java).apply {
                putExtra(EXTRA_URL, url)
                putExtra(EXTRA_SOURCE_NAME, sourceName)
            }
        }
    }

    private lateinit var binding: ActivityManualCaptchaBinding
    private var targetUrl: String = ""
    private var sourceName: String = ""
    private var sourceUserAgent: String? = null
    private var verificationJob: Job? = null

    @Inject
    lateinit var webViewExecutor: WebViewExecutor
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManualCaptchaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        targetUrl = intent.getStringExtra(EXTRA_URL) ?: ""
        sourceName = intent.getStringExtra(EXTRA_SOURCE_NAME) ?: "Unknown Source"
        sourceUserAgent = intent.getStringExtra(EXTRA_USER_AGENT)
        
        if (targetUrl.isBlank()) {
            CaptchaResultBus.emitResult(targetUrl, false)
            setResult(RESULT_CANCELLED)
            finish()
            return
        }
        
        setupToolbar()
        setupWebView()
        runVerification()
    }
    
    private fun setupToolbar() {
        binding.toolbar.title = "Just a moment..."
        
        // Extract domain name from URL for the subtitle
        val domain = runCatching {
            URI(targetUrl).host
        }.getOrNull() ?: targetUrl
        
        binding.toolbar.subtitle = domain
        
        binding.toolbar.setNavigationOnClickListener {
            cancelAndFinish()
        }
        binding.toolbar.inflateMenu(R.menu.menu_captcha)
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_close -> {
                    cancelAndFinish()
                    true
                }
                else -> false
            }
        }
    }
    
    private fun setupWebView() {
        val webView = webViewExecutor.getWebView()
        (webView.parent as? ViewGroup)?.removeView(webView)
        binding.webViewContainer.removeAllViews()
        binding.webViewContainer.addView(
            webView, 
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
    }
    
    private fun runVerification() {
        verificationJob?.cancel()
        
        verificationJob = lifecycleScope.launch {
            try {
                // Timeout is 60 seconds (default inside tryResolveCaptcha is 60_000)
                val success = webViewExecutor.tryResolveCaptcha(targetUrl, sourceUserAgent)
                if (success) {
                    onVerificationSuccess()
                } else {
                    onVerificationFailure()
                }
            } catch (e: Exception) {
                onVerificationFailure()
            }
        }
    }
    
    private fun onVerificationSuccess() {
        CaptchaResultBus.emitResult(targetUrl, true)
        setResult(RESULT_SOLVED)
        finish()
    }
    
    private fun onVerificationFailure() {
        cancelAndFinish()
    }
    
    private fun cancelAndFinish() {
        verificationJob?.cancel()
        CaptchaResultBus.emitResult(targetUrl, false)
        setResult(RESULT_CANCELLED)
        finish()
    }
    
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        cancelAndFinish()
    }
}
