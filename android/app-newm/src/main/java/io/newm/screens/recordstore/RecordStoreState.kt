package io.newm.screens.recordstore

import com.slack.circuit.runtime.CircuitUiState


sealed class RecordStoreState : CircuitUiState {
    data object Loading : RecordStoreState()
    data class Content(
        val eventSink: (RecordStoreUiEvent) -> Unit,
    ) : RecordStoreState()
    data object Error : RecordStoreState()
}
