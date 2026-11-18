package com.sweak.qralarm.features.emergency.explanations

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.sweak.qralarm.R
import com.sweak.qralarm.core.designsystem.icon.QRAlarmIcons
import com.sweak.qralarm.core.domain.emergency.EmergencyExplanation
import com.sweak.qralarm.core.domain.emergency.EmergencyExplanationsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class EmergencyExplanationsViewModel @Inject constructor(
    private val repository: EmergencyExplanationsRepository
) : ViewModel() {
    val explanations = repository.getAllExplanations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun delete(id: Long) = viewModelScope.launch { repository.deleteExplanation(id) }
    fun deleteAll() = viewModelScope.launch { repository.deleteAllExplanations() }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyExplanationsScreen(onBackClicked: () -> Unit) {
    val viewModel = hiltViewModel<EmergencyExplanationsViewModel>()
    val explanations by viewModel.explanations.collectAsStateWithLifecycle()
    var confirmDeleteAll by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.emergency_explanation_history)) },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(QRAlarmIcons.BackArrow, stringResource(R.string.content_description_back_arrow_icon))
                    }
                },
                actions = {
                    if (explanations.isNotEmpty()) {
                        TextButton(onClick = { confirmDeleteAll = true }) {
                            Text(stringResource(R.string.emergency_explanation_delete_all))
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (explanations.isEmpty()) {
            Box(Modifier.padding(padding).fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.emergency_explanation_none))
            }
        }
        LazyColumn(
            Modifier.padding(padding).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(explanations, key = { it.explanationId }) { explanation ->
                ExplanationRow(explanation, onDelete = { viewModel.delete(explanation.explanationId) })
            }
        }
    }

    if (confirmDeleteAll) {
        AlertDialog(
            onDismissRequest = { confirmDeleteAll = false },
            title = { Text(stringResource(R.string.emergency_explanation_delete_all_confirm)) },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteAll(); confirmDeleteAll = false }) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = { TextButton(onClick = { confirmDeleteAll = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}

@Composable
private fun ExplanationRow(explanation: EmergencyExplanation, onDelete: () -> Unit) {
    var expanded by rememberSaveable(explanation.explanationId) { mutableStateOf(false) }
    Card(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp).animateContentSize().clickable { expanded = !expanded }
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                            .format(Date(explanation.createdAtMillis)),
                        style = MaterialTheme.typography.titleMedium
                    )
                    val alarmTime = if (explanation.alarmHourOfDay != null && explanation.alarmMinute != null) {
                        "%02d:%02d".format(explanation.alarmHourOfDay, explanation.alarmMinute)
                    } else null
                    listOfNotNull(alarmTime, explanation.alarmLabel, explanation.dismissalSummary)
                        .takeIf { it.isNotEmpty() }
                        ?.let { Text(it.joinToString(" · "), style = MaterialTheme.typography.bodySmall) }
                }
                IconButton(onClick = onDelete) {
                    Icon(QRAlarmIcons.Delete, stringResource(R.string.delete))
                }
            }
            Text(
                explanation.text,
                maxLines = if (expanded) Int.MAX_VALUE else 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
