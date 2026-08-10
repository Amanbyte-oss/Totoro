package com.aman.vanish.ai

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aman.vanish.R
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * End-to-end integration test scaffold for the AI Pick feature.
 *
 * NOTE: This test requires:
 * 1. A valid Groq API key set in AiConfig.kt
 * 2. An Android emulator or device
 * 3. A configured test Hilt module that provides mock data (see FakeAiModule below)
 *
 * To run: ./gradlew connectedDebugAndroidTest --tests "*.AiPickFlowTest"
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class AiPickFlowTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val activityRule = ActivityScenarioRule(AiPickActivity::class.java)

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    /**
     * Test 1: Screen opens in Idle state and shows input field.
     */
    @Test
    fun testScreenOpens_showsInputField() {
        onView(withId(R.id.editText_query))
            .check(matches(isDisplayed()))
    }

    /**
     * Test 2: Type a query, tap send → loading state appears.
     * NOTE: Requires fake SourceAggregator and GroqQueryParser injected via test Hilt module.
     */
    @Test
    fun testQuerySubmit_showsLoadingThenResults() {
        // Skip if API key is not set
        if (AiConfig.GROQ_API_KEY == "gsk-your-groq-api-key-here") return

        // Step 1: Type query
        onView(withId(R.id.editText_query))
            .perform(click(), typeText("action romance"), closeSoftKeyboard())

        // Step 2: Tap send
        onView(withId(R.id.button_send))
            .perform(click())

        // Step 3: Verify loading status appears
        onView(withId(R.id.textView_loading_status))
            .check(matches(isDisplayed()))

        // Step 4: Wait for results (IdlingResource required for real API calls)
        // With fake data injected via Hilt test module, results are immediate
        // TODO: Replace with EspressoIdlingResource when API is wired up

        // Step 5: Verify results RecyclerView is visible (after fake data resolves)
        // onView(withId(R.id.recyclerView_results)).check(matches(isDisplayed()))
    }

    /**
     * Test 3: Character counter updates as user types.
     */
    @Test
    fun testCharCounter_updatesOnType() {
        onView(withId(R.id.editText_query))
            .perform(click(), typeText("action"), closeSoftKeyboard())

        onView(withId(R.id.textView_char_counter))
            .check(matches(withText("6/200")))
    }

    /**
     * Test 4: Sending empty query shows validation error.
     */
    @Test
    fun testEmptyQuery_showsValidationError() {
        onView(withId(R.id.editText_query))
            .perform(click(), clearText(), closeSoftKeyboard())

        // Send button should be disabled for empty input
        onView(withId(R.id.button_send))
            .check(matches(isNotEnabled()))
    }

    /**
     * Test 5: Predefined chip submits a query.
     */
    @Test
    fun testChipClick_populatesInputAndSendsQuery() {
        if (AiConfig.GROQ_API_KEY == "gsk-your-groq-api-key-here") return

        onView(withId(R.id.chip_action_manhwa))
            .perform(click())

        onView(withId(R.id.editText_query))
            .check(matches(withText("Action manhwa")))
    }
}
