package com.sweak.qralarm.features.custom_object.list

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sweak.qralarm.R
import com.sweak.qralarm.core.designsystem.icon.QRAlarmIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyObjectsScreen(
    onBackClicked: () -> Unit,
    onAddClicked: () -> Unit,
    onRetakeClicked: (objectId: Long) -> Unit
) {
    val viewModel = hiltViewModel<MyObjectsViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.my_objects)) },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(QRAlarmIcons.BackArrow, stringResource(R.string.content_description_back_arrow_icon))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClicked) {
                Icon(QRAlarmIcons.Add, stringResource(R.string.custom_object_add_new))
            }
        }
    ) { padding ->
        if (state.rows.isEmpty()) {
            Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.custom_object_none_yet))
            }
        }
        LazyColumn(Modifier.padding(padding).fillMaxSize()) {
            items(state.rows, key = { it.item.objectId }) { row ->
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    row.thumbnail?.let { Image(it.asImageBitmap(), null, Modifier.size(56.dp)) }
                        ?: Box(Modifier.size(56.dp))
                    Column(Modifier.weight(1f)) {
                        Text(row.item.name, style = MaterialTheme.typography.titleMedium)
                        Text(
                            stringResource(R.string.my_objects_used_by, row.alarmsUsing),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    IconButton(onClick = { viewModel.requestRename(row) }) {
                        Icon(QRAlarmIcons.Edit, stringResource(R.string.my_objects_rename))
                    }
                    IconButton(onClick = { onRetakeClicked(row.item.objectId) }) {
                        Icon(QRAlarmIcons.Camera, stringResource(R.string.custom_object_retake))
                    }
                    IconButton(onClick = { viewModel.requestDelete(row) }) {
                        Icon(QRAlarmIcons.Delete, stringResource(R.string.delete))
                    }
                }
            }
        }
    }

    state.pendingDelete?.let { row ->
        AlertDialog(
            onDismissRequest = { viewModel.requestDelete(null) },
            title = { Text(stringResource(R.string.my_objects_delete_title, row.item.name)) },
            text = if (row.alarmsUsing > 0) {
                { Text(stringResource(R.string.my_objects_delete_in_use, row.alarmsUsing)) }
            } else null,
            confirmButton = { TextButton(onClick = { viewModel.confirmDelete() }) { Text(stringResource(R.string.delete)) } },
            dismissButton = { TextButton(onClick = { viewModel.requestDelete(null) }) { Text(stringResource(R.string.cancel)) } }
        )
    }
    state.pendingRename?.let { row ->
        var name by rememberSaveable(row.item.objectId) { mutableStateOf(row.item.name) }
        AlertDialog(
            onDismissRequest = { viewModel.requestRename(null) },
            title = { Text(stringResource(R.string.my_objects_rename)) },
            text = {
                OutlinedTextField(value = name, onValueChange = { name = it.take(40) }, singleLine = true)
            },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmRename(name) }, enabled = name.isNotBlank()) {
                    Text(stringResource(R.string.custom_object_save))
                }
            },
            dismissButton = { TextButton(onClick = { viewModel.requestRename(null) }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}
