package com.antifraud.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antifraud.app.viewmodel.AnalysisUiState
import com.antifraud.app.viewmodel.AnalysisViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: AnalysisViewModel,
    onNavigateToResult: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    val inputText by viewModel.inputText.collectAsState()
    val analysisState by viewModel.analysisState.collectAsState()

    LaunchedEffect(analysisState) {
        if (analysisState is AnalysisUiState.Success) {
            onNavigateToResult()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "反欺诈检测",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(Icons.Default.History, contentDescription = "历史记录")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "设置")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Banner card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🛡️",
                        fontSize = 40.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "AI 反欺诈智能分析",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "输入可疑文本，AI 将自动分析诈骗风险等级",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Input area
            OutlinedTextField(
                value = inputText,
                onValueChange = viewModel::onInputTextChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 160.dp),
                placeholder = {
                    Text(
                        text = "请在此输入可疑短信、电话内容或聊天记录...\n\n例如：对方声称是公安局，说我涉嫌洗钱，要求我配合调查并转账至安全账户...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                label = { Text("待分析内容") },
                shape = RoundedCornerShape(12.dp),
                maxLines = 10,
                trailingIcon = {
                    if (inputText.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onInputTextChanged("") }) {
                            Icon(Icons.Default.Search, contentDescription = null)
                        }
                    }
                }
            )

            // Character count
            Text(
                text = "${inputText.length} 字",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.align(Alignment.End)
            )

            // Analyze button
            Button(
                onClick = viewModel::analyze,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = inputText.isNotBlank() && analysisState !is AnalysisUiState.Loading,
                shape = RoundedCornerShape(12.dp)
            ) {
                AnimatedVisibility(
                    visible = analysisState is AnalysisUiState.Loading,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                }
                AnimatedVisibility(
                    visible = analysisState !is AnalysisUiState.Loading,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Text(
                        text = "开始分析",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Error message
            AnimatedVisibility(visible = analysisState is AnalysisUiState.Error) {
                val errorMsg = (analysisState as? AnalysisUiState.Error)?.message ?: ""
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "⚠️ $errorMsg",
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Tips section
            Text(
                text = "常见诈骗类型",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            val tips = listOf(
                "冒充公检法" to "对方自称警察/检察官/法官，要求转账至「安全账户」",
                "网络刷单" to "声称刷单返利，需先垫付本金，最终无法提现",
                "冒充客服" to "谎称账号异常、退款操作，诱导点击链接或转账",
                "杀猪盘" to "网恋后诱导投资、博彩，平台为假，本金无法提取",
                "虚假贷款" to "声称低息贷款，收取「手续费」「保证金」后消失"
            )
            tips.forEach { (type, desc) ->
                TipItem(type = type, desc = desc)
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun TipItem(type: String, desc: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = type,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.width(80.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
