package com.khalid.vyntra.presentation.premium

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khalid.vyntra.util.BillingManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Which plan card the user currently has highlighted in the picker.
 * Note: today the underlying [BillingManager] only ships a single one-time
 * IAP product (`pro_unlock`), so both plans launch the same purchase flow.
 * The UI distinction is preserved for when real Play subscriptions are wired
 * up; swap the click handler to launch the matching subscription product.
 */
enum class PremiumPlan(val displayPrice: String, val cadence: String, val savings: String?) {
    MONTHLY(displayPrice = "Rs 499", cadence = "/ month", savings = null),
    YEARLY(displayPrice = "Rs 3,999", cadence = "/ year", savings = "Save 33%")
}

data class PremiumUiState(
    val isProUser: Boolean = false,
    val selectedPlan: PremiumPlan = PremiumPlan.YEARLY,
    val priceFromStore: String? = null
)

sealed interface PremiumEvent {
    data object PurchaseLaunched : PremiumEvent
    data object RestoreTriggered : PremiumEvent
    data class ShowMessage(val message: String) : PremiumEvent
}

@HiltViewModel
class PremiumViewModel @Inject constructor(
    private val billingManager: BillingManager
) : ViewModel() {

    private val _selectedPlan = MutableStateFlow(PremiumPlan.YEARLY)

    private val _event = Channel<PremiumEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    val uiState: StateFlow<PremiumUiState> = combine(
        billingManager.isProUser,
        billingManager.productDetails,
        _selectedPlan
    ) { isPro, productDetails, plan ->
        val storePrice = productDetails
            ?.oneTimePurchaseOfferDetails
            ?.formattedPrice
        PremiumUiState(
            isProUser = isPro,
            selectedPlan = plan,
            priceFromStore = storePrice
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PremiumUiState()
    )

    fun selectPlan(plan: PremiumPlan) {
        _selectedPlan.value = plan
    }

    /**
     * Launches the Play Billing purchase flow. Must be called from an Activity
     * context because Billing requires it.
     */
    fun upgrade(activity: Activity) {
        billingManager.launchPurchaseFlow(activity)
        viewModelScope.launch { _event.send(PremiumEvent.PurchaseLaunched) }
    }

    fun restorePurchases() {
        billingManager.restorePurchases()
        viewModelScope.launch {
            _event.send(PremiumEvent.RestoreTriggered)
            _event.send(PremiumEvent.ShowMessage("Checking for previous purchases…"))
        }
    }
}

