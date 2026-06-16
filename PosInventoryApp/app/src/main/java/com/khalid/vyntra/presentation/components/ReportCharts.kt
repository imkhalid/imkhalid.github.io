package com.khalid.vyntra.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.common.data.ExtraStore

/**
 * Single-series line chart with optional X-axis labels. Used for daily sales trends.
 */
@Composable
fun TrendLineChart(
    title: String,
    values: List<Double>,
    xLabels: List<String>? = null,
    modifier: Modifier = Modifier
) {
    if (values.isEmpty()) {
        ChartCard(title = title, modifier = modifier) {
            EmptyChartMessage()
        }
        return
    }

    val producer = remember { CartesianChartModelProducer() }
    val labelKey = remember { ExtraStore.Key<List<String>>() }

    LaunchedEffect(values, xLabels) {
        producer.runTransaction {
            lineSeries { series(values) }
            if (xLabels != null) {
                extras { it[labelKey] = xLabels }
            }
        }
    }

    ChartCard(title = title, modifier = modifier) {
        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberLineCartesianLayer(),
                startAxis = VerticalAxis.rememberStart(),
                bottomAxis = HorizontalAxis.rememberBottom(
                    valueFormatter = CartesianValueFormatter { ctx, x, _ ->
                        val labels = ctx.model.extraStore.getOrNull(labelKey)
                        labels?.getOrNull(x.toInt()) ?: x.toInt().toString()
                    }
                )
            ),
            modelProducer = producer,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )
    }
}

/**
 * Multi-bar comparison chart. Each bar is a labelled category (e.g. Sales / Purchases / Profit).
 */
@Composable
fun ComparisonBarChart(
    title: String,
    bars: List<Pair<String, Double>>,
    modifier: Modifier = Modifier
) {
    if (bars.isEmpty()) {
        ChartCard(title = title, modifier = modifier) {
            EmptyChartMessage()
        }
        return
    }

    val producer = remember { CartesianChartModelProducer() }
    val labelKey = remember { ExtraStore.Key<List<String>>() }

    LaunchedEffect(bars) {
        producer.runTransaction {
            columnSeries { series(bars.map { it.second }) }
            extras { it[labelKey] = bars.map { it.first } }
        }
    }

    ChartCard(title = title, modifier = modifier) {
        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberColumnCartesianLayer(),
                startAxis = VerticalAxis.rememberStart(),
                bottomAxis = HorizontalAxis.rememberBottom(
                    valueFormatter = CartesianValueFormatter { ctx, x, _ ->
                        val labels = ctx.model.extraStore.getOrNull(labelKey)
                        labels?.getOrNull(x.toInt()) ?: ""
                    }
                )
            ),
            modelProducer = producer,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        )
    }
}

@Composable
private fun ChartCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun EmptyChartMessage() {
    Text(
        text = "Not enough data to plot.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(8.dp)
    )
}
