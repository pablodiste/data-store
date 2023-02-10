package dev.pablodiste.datastore.sample.ui.room.stream

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.pablodiste.datastore.sample.ui.main.BaseScreen

@Composable
fun RoomStreamDTOExample(viewModel: RoomStreamDTOExampleViewModel, openDrawer: () -> Unit) {
    BaseScreen(title = "Store Stream Example (DTO + Room + Ktor)", openDrawer = openDrawer,) {
        RoomStreamDTOExamplePeopleList(viewModel)
    }
}

@Composable
fun RoomStreamDTOExamplePeopleList(viewModel: RoomStreamDTOExampleViewModel) {
    val uiState = viewModel.uiState.collectAsState()
    LazyColumn {
        items(uiState.value) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(text = it.name!!, style = MaterialTheme.typography.body1)
            }
        }
    }
}