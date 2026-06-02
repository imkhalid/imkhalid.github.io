package com.khalid.vyntra.presentation.premium

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.khalid.vyntra.presentation.theme.ProfitGreen

/**
 * Premium upgrade screen. Lists feature benefits, plan picker (monthly /
 * yearly), free-vs-premium comparison, CTA + Restore + legal links.
 *
 * Launches the existing one-time Play Billing purchase flow. When real
 * subscription products are configured in Play Console, swap [PremiumViewModel.upgrade]
 * to pick the matching product based on the selected plan.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumScreen(
    onNavigateBack: () -> Unit,
    viewModel: PremiumViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val activity = context as? Activity
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is PremiumEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
                PremiumEvent.PurchaseLaunched -> { /* Play Billing UI takes over */ }
                PremiumEvent.RestoreTriggered -> { /* Snackbar handled separately */ }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vyntra Premium") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(Modifier.height(8.dp)) }
            item { HeroBanner(isProUser = uiState.isProUser) }

            // Feature highlights
            item { SectionLabel("Why upgrade") }
            item { FeatureGrid() }

            // Plan picker — hidden when the user is already Pro
            if (!uiState.isProUser) {
                item { SectionLabel("Choose a plan") }
                item {
                    PlanPicker(
                        selected = uiState.selectedPlan,
                        storePrice = uiState.priceFromStore,
                        onSelect = viewModel::selectPlan
                    )
                }
            }

            // Comparison table
            item { SectionLabel("Free vs Premium") }
            item { ComparisonTable() }

            // CTA + Restore
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { activity?.let(viewModel::upgrade) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = !uiState.isProUser && activity != null,
                        shape = MaterialTheme.shapes.large,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.WorkspacePremium,
                            contentDescription = null
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (uiState.isProUser) "You're already Pro 🎉" else "Upgrade to Premium",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    TextButton(
                        onClick = viewModel::restorePurchases,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Restore purchases") }
                }
            }

            // Terms & Privacy
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { uriHandler.openUri(TERMS_URL) }) {
                        Text("Terms", style = MaterialTheme.typography.labelMedium)
                    }
                    Text("·", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    TextButton(onClick = { uriHandler.openUri(PRIVACY_URL) }) {
                        Text("Privacy", style = MaterialTheme.typography.labelMedium)
                    }
                    Text("·", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    TextButton(onClick = { openPlaySubscriptionsManagement(context) }) {
                        Text("Manage subscription", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            // Fine print
            item {
                Text(
                    text = "Subscriptions auto-renew until cancelled. You can manage your " +
                            "subscription from the Google Play Store at any time.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .alpha(0.7f)
                )
            }
        }
    }
}

// ── Hero banner ─────────────────────────────────────────────────────────────

@Composable
private fun HeroBanner(isProUser: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.tertiary
                        )
                    ),
                    shape = MaterialTheme.shapes.large
                )
                .padding(24.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.WorkspacePremium,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier
                                .padding(10.dp)
                                .size(24.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = if (isProUser) "Vyntra Premium" else "Run your business at full speed",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = if (isProUser)
                        "All premium features are unlocked on this account."
                    else
                        "Unlock unlimited invoices, customers, advanced reports and cloud backup.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.92f)
                )
            }
        }
    }
}

// ── Feature grid ────────────────────────────────────────────────────────────

private data class PremiumFeature(
    val icon: ImageVector,
    val title: String,
    val description: String
)

private val features = listOf(
    PremiumFeature(Icons.Filled.Block, "Ad-free experience", "No banner or interstitial ads anywhere in the app."),
    PremiumFeature(Icons.Filled.Receipt, "Unlimited invoices", "Create as many invoices as you need — no daily caps."),
    PremiumFeature(Icons.Filled.Inventory2, "Unlimited products", "Catalog can grow without limit beyond the 50-SKU free tier."),
    PremiumFeature(Icons.Filled.People, "Unlimited customers", "Track every buyer's history and outstanding balance."),
    PremiumFeature(Icons.Filled.QueryStats, "Advanced reports", "P&L, top-selling, tax, vendor payable and more."),
    PremiumFeature(Icons.Filled.CloudUpload, "Cloud backup", "Auto-backup your data so you never lose a sale."),
    PremiumFeature(Icons.Filled.HeadsetMic, "Priority support", "Skip the line — premium users get first-class help.")
)

@Composable
private fun FeatureGrid() {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        features.forEach { feature ->
            FeatureCard(feature)
        }
    }
}

@Composable
private fun FeatureCard(feature: PremiumFeature) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = feature.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = feature.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = feature.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ── Plan picker ─────────────────────────────────────────────────────────────

@Composable
private fun PlanPicker(
    selected: PremiumPlan,
    storePrice: String?,
    onSelect: (PremiumPlan) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        PlanCard(
            plan = PremiumPlan.MONTHLY,
            isSelected = selected == PremiumPlan.MONTHLY,
            modifier = Modifier.weight(1f),
            badge = null,
            onClick = { onSelect(PremiumPlan.MONTHLY) }
        )
        PlanCard(
            plan = PremiumPlan.YEARLY,
            isSelected = selected == PremiumPlan.YEARLY,
            // If the Play Store gave us a real price, show it on the yearly
            // card — much more credible than the hardcoded display price.
            storePrice = storePrice,
            modifier = Modifier.weight(1f),
            badge = "MOST POPULAR",
            onClick = { onSelect(PremiumPlan.YEARLY) }
        )
    }
}

@Composable
private fun PlanCard(
    plan: PremiumPlan,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badge: String? = null,
    storePrice: String? = null
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary
                      else MaterialTheme.colorScheme.outlineVariant
    Card(
        modifier = modifier
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                             else MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = borderColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box {
            if (badge != null) {
                Surface(
                    shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 0.dp, topStart = 0.dp, topEnd = 10.dp),
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Text(
                        text = badge,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onTertiary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = plan.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = storePrice ?: plan.displayPrice,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = plan.cadence,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                if (plan.savings != null) {
                    Spacer(Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = ProfitGreen.copy(alpha = 0.16f)
                    ) {
                        Text(
                            text = plan.savings,
                            style = MaterialTheme.typography.labelSmall,
                            color = ProfitGreen,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }
    }
}

// ── Comparison table ────────────────────────────────────────────────────────

private data class ComparisonRow(val label: String, val free: String, val premium: String)

private val comparisonRows = listOf(
    ComparisonRow("Products in catalog", "Up to 50", "Unlimited"),
    ComparisonRow("Sales per day", "Up to 20", "Unlimited"),
    ComparisonRow("Customers", "Limited", "Unlimited"),
    ComparisonRow("Advanced reports", "—", "Included"),
    ComparisonRow("Cloud backup", "—", "Included"),
    ComparisonRow("Ads", "Banner + interstitial", "Ad-free"),
    ComparisonRow("Support", "Community", "Priority")
)

@Composable
private fun ComparisonTable() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(Modifier.weight(1.4f))
                Text(
                    text = "FREE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "PREMIUM",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(8.dp))
            comparisonRows.forEachIndexed { i, row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = row.label,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1.4f)
                    )
                    ComparisonValue(text = row.free, isPremium = false, modifier = Modifier.weight(1f))
                    ComparisonValue(text = row.premium, isPremium = true, modifier = Modifier.weight(1f))
                }
                if (i < comparisonRows.lastIndex) {
                    androidx.compose.material3.HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ComparisonValue(text: String, isPremium: Boolean, modifier: Modifier = Modifier) {
    val isCheckmark = text.equals("Included", ignoreCase = true) || text.equals("Unlimited", ignoreCase = true) || text.equals("Ad-free", ignoreCase = true)
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isPremium && isCheckmark) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = ProfitGreen,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(4.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = when {
                isPremium && isCheckmark -> ProfitGreen
                isPremium -> MaterialTheme.colorScheme.onSurface
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
            fontWeight = if (isPremium) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

// ── Helpers ─────────────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp)
    )
}

/**
 * Open Play Store's subscriptions management page. Used so paying users can
 * cancel without leaving Vyntra → Settings → ...
 */
private fun openPlaySubscriptionsManagement(context: android.content.Context) {
    val intent = Intent(
        Intent.ACTION_VIEW,
        android.net.Uri.parse("https://play.google.com/store/account/subscriptions")
    ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
    runCatching { context.startActivity(intent) }
}

// Replace with real legal URLs before release.
private const val TERMS_URL = "https://vyntra.app/terms"
private const val PRIVACY_URL = "https://vyntra.app/privacy"
