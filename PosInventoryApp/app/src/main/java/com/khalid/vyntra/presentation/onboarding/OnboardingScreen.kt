package com.khalid.vyntra.presentation.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch

private data class OnboardingPage(
    val icon: ImageVector,
    val tint: Color,
    val title: String,
    val body: String
)

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val pages = rememberOnboardingPages()
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    fun finish() {
        viewModel.markOnboardingComplete()
        onFinished()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Skip in the top-right — always reachable without committing to the flow
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = { finish() },
                    enabled = pagerState.currentPage < pages.lastIndex
                ) { Text("Skip") }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { pageIndex ->
                OnboardingPageContent(pages[pageIndex])
            }

            // Indicator + nav
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                PageDots(
                    pageCount = pages.size,
                    currentPage = pagerState.currentPage
                )
                Spacer(Modifier.height(16.dp))

                // Animate label change between pages so the CTA feels alive.
                AnimatedContent(
                    targetState = pagerState.currentPage == pages.lastIndex,
                    transitionSpec = {
                        fadeIn(tween(200)) togetherWith fadeOut(tween(200))
                    },
                    label = "OnboardingCta"
                ) { isLast ->
                    Button(
                        onClick = {
                            if (isLast) {
                                finish()
                            } else {
                                scope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Text(
                            text = if (isLast) "Get started" else "Next",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null
                        )
                    }
                }
            }
        }
    }
}

// ── Page ─────────────────────────────────────────────────────────────────────

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Tonal glyph container — the "illustration" for a code-only onboarding.
        // Reads as premium without needing PNG artwork.
        Box(
            modifier = Modifier
                .size(180.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            page.tint.copy(alpha = 0.18f),
                            page.tint.copy(alpha = 0.04f)
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(112.dp)
                    .background(page.tint.copy(alpha = 0.14f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = page.icon,
                    contentDescription = null,
                    tint = page.tint,
                    modifier = Modifier.size(56.dp)
                )
            }
        }

        Spacer(Modifier.height(40.dp))

        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = page.body,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

// ── Page dots ────────────────────────────────────────────────────────────────

@Composable
private fun PageDots(pageCount: Int, currentPage: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            val isActive = index == currentPage
            val color = if (isActive) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(width = if (isActive) 24.dp else 8.dp, height = 8.dp)
                    .background(color, CircleShape)
            )
        }
    }
}

// ── Pages content (composable so MaterialTheme is in scope) ─────────────────

@Composable
private fun rememberOnboardingPages(): List<OnboardingPage> {
    val cs = MaterialTheme.colorScheme
    return androidx.compose.runtime.remember {
        listOf(
            OnboardingPage(
                icon = Icons.Filled.Receipt,
                tint = cs.primary,
                title = "Create invoices in seconds",
                body = "Build professional invoices for every sale — search products, add tax and discounts, take payment, all on one screen."
            ),
            OnboardingPage(
                icon = Icons.Filled.People,
                tint = cs.secondary,
                title = "Manage customers & products",
                body = "Keep a tidy catalog and customer book. Track outstanding balances, low stock and top buyers without a spreadsheet."
            ),
            OnboardingPage(
                icon = Icons.Filled.Print,
                tint = cs.tertiary,
                title = "Print or share any invoice",
                body = "Send via WhatsApp, email or Telegram in two taps. Print to any Wi-Fi, Bluetooth or system printer — no setup required."
            ),
            OnboardingPage(
                icon = Icons.Filled.QueryStats,
                tint = cs.primary,
                title = "Reports & live analytics",
                body = "See today's revenue, weekly trends and profit/loss at a glance. Every report exports to PDF or CSV."
            ),
            OnboardingPage(
                icon = Icons.Filled.WorkspacePremium,
                tint = cs.secondary,
                title = "Vyntra Premium — when you grow",
                body = "Unlock unlimited products, ad-free experience and cloud backup whenever you're ready. Free to keep using forever."
            )
        )
    }
}

