package com.sweak.qralarm.features.custom_object.list

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sweak.qralarm.core.domain.custom_object.CustomObject
import com.sweak.qralarm.core.domain.custom_object.CustomObjectsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MyObjectRow(val item: CustomObject, val thumbnail: Bitmap?, val alarmsUsing: Int)

data class MyObjectsState(
    val rows: List<MyObjectRow> = emptyList(),
    val pendingDelete: MyObjectRow? = null,
    val pendingRename: MyObjectRow? = null
)

@HiltViewModel
class MyObjectsViewModel @Inject constructor(
    private val repository: CustomObjectsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MyObjectsState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllObjects().collect { objects ->
                val rows = objects.map { item ->
                    MyObjectRow(
                        item,
                        repository.loadReferenceJpegs(item.objectId).firstOrNull()?.let {
                            BitmapFactory.decodeByteArray(it, 0, it.size)
                        },
                        repository.countAlarmsUsingObject(item.objectId)
                    )
                }
                _state.update { it.copy(rows = rows) }
            }
        }
    }

    fun requestDelete(row: MyObjectRow?) = _state.update { it.copy(pendingDelete = row) }
    fun requestRename(row: MyObjectRow?) = _state.update { it.copy(pendingRename = row) }

    fun confirmDelete() = viewModelScope.launch {
        state.value.pendingDelete?.let { repository.deleteObject(it.item.objectId) }
        _state.update { it.copy(pendingDelete = null) }
    }

    fun confirmRename(name: String) = viewModelScope.launch {
        val row = state.value.pendingRename
        if (row != null && name.isNotBlank()) repository.renameObject(row.item.objectId, name)
        _state.update { it.copy(pendingRename = null) }
    }
}
