package dev.pablodiste.datastore.sample.ui.room.concurrent

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
fun RoomConcurrentExample(viewModel: RoomConcurrentExampleViewModel, openDrawer: () -> Unit) {
    BaseScreen(title = "Store Concurrent Example (Room)", openDrawer = openDrawer,) {
        RoomConcurrentExamplePeopleList(viewModel)
    }
}

@Composable
fun RoomConcurrentExamplePeopleList(viewModel: RoomConcurrentExampleViewModel) {
    val uiState = viewModel.uiState.collectAsState()
    LazyColumn {
        items(uiState.value) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(text = it.title!!, style = MaterialTheme.typography.body1)
            }
        }
    }
}