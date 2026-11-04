package com.example.offgrid

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isSelected
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TabNavigationInstrumentedTest {
    @Test
    fun conversationRowsOpenTheSelectedChatAndBackReturnsToChats() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.contacts_sarah)).perform(scrollTo(), click())
            onView(withId(R.id.chat_contact_name)).check(matches(
                androidx.test.espresso.matcher.ViewMatchers.withText("Sarah Miller")
            ))
            onView(withId(R.id.chat_back)).perform(click())
            onView(withId(R.id.contacts_alex)).perform(scrollTo(), click())
            onView(withId(R.id.chat_contact_name)).check(matches(
                androidx.test.espresso.matcher.ViewMatchers.withText("Alex Johnson")
            ))
            pressBack()
            onView(withId(R.id.chatList)).check(matches(isDisplayed()))
        }
    }

    private data class Tab(val buttonId: Int, val screenId: Int)

    private val tabs = listOf(
        Tab(R.id.tab_chats, R.id.chatList),
        Tab(R.id.tab_search, R.id.searchScreen),
        Tab(R.id.tab_requests, R.id.requestsScreen),
        Tab(R.id.tab_settings, R.id.settingsScreen)
    )

    private fun visit(tab: Tab) {
        onView(withId(tab.buttonId)).perform(click())
        onView(withId(tab.screenId)).check(matches(isDisplayed()))
        onView(withId(tab.buttonId)).check(matches(isSelected()))
    }

    @Test
    fun launchAndRecreationShowChats() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            onView(withId(R.id.chatList)).check(matches(isDisplayed()))
            onView(withId(R.id.tab_chats)).check(matches(isSelected()))
            scenario.recreate()
            onView(withId(R.id.chatList)).check(matches(isDisplayed()))
            onView(withId(R.id.tab_chats)).check(matches(isSelected()))
        }
    }

    @Test
    fun everyTabCanReachEveryOtherTabWithoutStackingVisits() {
        ActivityScenario.launch(MainActivity::class.java).use {
            for (source in tabs) {
                for (target in tabs) {
                    visit(source)
                    visit(target)
                }
            }
            // The final tab is Settings; previous tab visits must not be underneath it.
            pressBack()
            onView(withId(R.id.chatList)).check(matches(isDisplayed()))

            visit(tabs[2])
            visit(tabs[2])
            visit(tabs[2])
            pressBack()
            onView(withId(R.id.chatList)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun backFromSearchRequestsAndSettingsReturnsToChats() {
        ActivityScenario.launch(MainActivity::class.java).use {
            visit(tabs[1])
            pressBack()
            onView(withId(R.id.chatList)).check(matches(isDisplayed()))

            visit(tabs[2])
            onView(withId(R.id.requests_back)).perform(click())
            onView(withId(R.id.chatList)).check(matches(isDisplayed()))
            visit(tabs[2])
            pressBack()
            onView(withId(R.id.chatList)).check(matches(isDisplayed()))

            visit(tabs[3])
            onView(withId(R.id.settings_back)).perform(click())
            onView(withId(R.id.chatList)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun developmentLoginIsEditableAndCanSwitchToRegister() {
        ActivityScenario.launch(MainActivity::class.java).use {
            visit(tabs[3])
            onView(withId(R.id.btnLogin)).perform(scrollTo(), click())
            onView(withId(R.id.login_email)).perform(
                scrollTo(), typeText("dev@example.com"), closeSoftKeyboard()
            )
            onView(withId(R.id.login_password)).perform(
                scrollTo(), typeText("development"), closeSoftKeyboard()
            )
            onView(withId(R.id.login_submit)).perform(scrollTo()).check(matches(isDisplayed()))

            onView(withId(R.id.login_open_register)).perform(scrollTo(), click())
            onView(withId(R.id.register_submit)).perform(scrollTo()).check(matches(isDisplayed()))
            pressBack()
            onView(withId(R.id.settingsScreen)).check(matches(isDisplayed()))
            pressBack()
            onView(withId(R.id.chatList)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun developmentRegisterCanSwitchToLoginAndReturnToSettings() {
        ActivityScenario.launch(MainActivity::class.java).use {
            visit(tabs[3])
            onView(withId(R.id.btnRegister)).perform(scrollTo(), click())
            onView(withId(R.id.register_name)).perform(
                scrollTo(), typeText("Development User"), closeSoftKeyboard()
            )
            onView(withId(R.id.register_open_login)).perform(scrollTo(), click())
            onView(withId(R.id.login_submit)).perform(scrollTo()).check(matches(isDisplayed()))
            onView(withId(R.id.login_back)).perform(click())
            onView(withId(R.id.settingsScreen)).check(matches(isDisplayed()))
            onView(withId(R.id.settings_back)).perform(click())
            onView(withId(R.id.chatList)).check(matches(isDisplayed()))
        }
    }
}
