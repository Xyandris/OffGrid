package com.example.offgrid

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationInstrumentedTest {
    @Test
    fun launchAndRecreationShowChatsAndSettingsCanReturnToChats() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            onView(withId(R.id.contacts_sarah)).check(matches(isDisplayed()))
            scenario.recreate()
            onView(withId(R.id.contacts_sarah)).check(matches(isDisplayed()))

            onView(withId(R.id.contacts_tab_settings)).perform(click())
            onView(withId(R.id.developmentSection)).check(matches(isDisplayed()))
            onView(withId(R.id.btnBack)).perform(scrollTo(), click())
            onView(withId(R.id.contacts_sarah)).check(matches(isDisplayed()))

            onView(withId(R.id.contacts_tab_settings)).perform(click())
            pressBack()
            onView(withId(R.id.contacts_sarah)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun developmentLoginCanSwitchToRegisterAndReturnToSettings() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.contacts_tab_settings)).perform(click())
            onView(withId(R.id.btnLogin)).perform(scrollTo(), click())
            onView(withId(R.id.btnSubmitLogin)).check(matches(isDisplayed()))

            onView(withId(R.id.btnOpenRegister)).perform(scrollTo(), click())
            onView(withId(R.id.btnSubmitRegister)).check(matches(isDisplayed()))
            pressBack()
            onView(withId(R.id.developmentSection)).check(matches(isDisplayed()))

            // The replaced login form must not remain between Settings and Chats.
            pressBack()
            onView(withId(R.id.contacts_sarah)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun developmentRegisterCanSwitchToLoginAndReturnToSettings() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.contacts_tab_settings)).perform(click())
            onView(withId(R.id.btnRegister)).perform(scrollTo(), click())
            onView(withId(R.id.btnSubmitRegister)).check(matches(isDisplayed()))

            onView(withId(R.id.btnOpenLogin)).perform(scrollTo(), click())
            onView(withId(R.id.btnSubmitLogin)).check(matches(isDisplayed()))
            onView(withId(R.id.btnBack)).perform(scrollTo(), click())
            onView(withId(R.id.developmentSection)).check(matches(isDisplayed()))

            pressBack()
            onView(withId(R.id.contacts_sarah)).check(matches(isDisplayed()))
        }
    }
}
