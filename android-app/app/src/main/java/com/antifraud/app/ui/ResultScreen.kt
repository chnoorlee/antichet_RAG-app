package com.antifraud.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antifraud.app.network.model.AnalysisResponse
import com.antifraud.app.network.model.RiskLevel
import com.antifraud.app.ui.theme.*
import com.antifraud.app.viewmodel.AnalysisUiState
import com.antifraud.app.viewmodel.AnalysisViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    viewModel: AnalysisViewModel,
    onBack: () -> Unit
) {
    val analysisState by viewModel.analysisState.collectAsState()
    val result = (analysisState as? AnalysisUiState.Success)?.response

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("分析结果", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.resetAnalysis()
                        onBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        if (result == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("无结果数据")
            }
        } else {
            ResultContent(
                response = result,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun ResultContent(response: AnalysisResponse, modifier: Modifier = Modifier) {
    val riskLevel = RiskLevel.from(response.data.riskLevel)
    val isDirectHit = response.resultType == "Direct_Hit"

    val (bgColor, borderColor, textColor, icon, riskDesc) = when (riskLevel) {
        RiskLevel.HIGH -> RiskCardStyle(
            bg = RiskHighBg, border = RiskHighBorder,
            text = Color(0xFFB71C1C), icon = "🚨",
            desc = "高度疑似诈骗！请立即停止操作，不要转账！"
        )
        RiskLevel.MEDIUM -> RiskCardStyle(
            bg = RiskMediumBg, border = RiskMediumBorder,
            text = Color(0xFFE65100), icon = "⚠️",
            desc = "存在诈骗嫌疑，请提高警惕，谨慎操作。"
        )
        RiskLevel.LOW -> RiskCardStyle(
            bg = RiskLowBg, border = RiskLowBorder,
            text = Color(0xFF1B5E20), icon = "✅",
            desc = "暂未发现明显诈骗特征，但仍需保持警惕。"
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Risk level card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(bgColor)
                .border(2.dp, borderColor, RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(icon, fontSize = 48.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = riskLevel.label,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = textColor
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = riskDesc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor,
                    fontWeight = FontWeight.Medium
                )
                if (isDirectHit) {
                    Spacer(Modifier.height(8.dp))
                    SuggestionChip(
                        onClick = {},
                        label = { Text("直接命中已知案例") },
                        icon = { Icon(Icons.Default.Warning, null, modifier = Modifier.size(16.dp)) }
                    )
                }
            }
        }

        // Recommended action
        if (!response.data.recommendedAction.isNullOrEmpty()) {
            InfoSection(title = "📋 建议行动") {
                Text(
                    text = response.data.recommendedAction,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        // Matched cases (Direct_Hit)
        if (!response.data.matchedCases.isNullOrEmpty()) {
            InfoSection(title = "📂 匹配案例") {
                response.data.matchedCases.forEachIndexed { idx, case ->
                    if (idx > 0) HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (!case.fraudType.isNullOrEmpty()) {
                                AssistChip(
                                    onClick = {},
                                    label = { Text(case.fraudType, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                            Text(
                                text = "相似度 ${(case.confidence * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = case.description,
                            style = MaterialTheme.typography.bodySmall
                        )
                        if (case.keyIndicators.isNotEmpty()) {
                            Text(
                                text = "关键词：${case.keyIndicators.joinToString("、")}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        }

        // RRF Score
        if (response.data.rrfScore != null && response.data.rrfScore > 0) {
            InfoSection(title = "📊 检索评分") {
                LinearProgressIndicator(
                    progress = { response.data.rrfScore.toFloat().coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
                Text(
                    text = "RRF Score: ${"%.4f".format(response.data.rrfScore)}",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // Relevant cases (RAG)
        val relevantCases = response.data.context?.relevantCases
        if (!relevantCases.isNullOrEmpty()) {
            InfoSection(title = "🗂 相关参考案例") {
                relevantCases.forEach { case ->
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("•", modifier = Modifier.padding(end = 6.dp, top = 2.dp))
                        Column {
                            if (!case.fraudType.isNullOrEmpty()) {
                                Text(
                                    text = case.fraudType,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(text = case.description, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

        // Anti-fraud tips
        val tips = response.data.context?.antiFraudTips
        if (!tips.isNullOrEmpty()) {
            InfoSection(title = "💡 反诈提示") {
                tips.forEach { tip ->
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("•", modifier = Modifier.padding(end = 6.dp, top = 2.dp))
                        Column {
                            Text(
                                text = tip.title,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(text = tip.content, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

        // Emergency notice for HIGH risk
        if (riskLevel == RiskLevel.HIGH) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🚔 紧急提示",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "如已遭受损失，请立即拨打 110 报警，或拨打全国反诈热线 96110。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun InfoSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            content()
        }
    }
}

private data class RiskCardStyle(
    val bg: Color,
    val border: Color,
    val text: Color,
    val icon: String,
    val desc: String
)
