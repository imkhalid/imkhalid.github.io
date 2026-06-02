package com.khalid.vyntra.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khalid.vyntra.data.preferences.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Tiny VM whose only job is to flip the "has seen onboarding" flag when the
 * user finishes or skips the welcome flow. The flow itself is stateless UI.
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    fun markOnboardingComplete() {
        viewModelScope.launch {
            preferencesManager.setHasSeenOnboarding(true)
        }
    }
}
