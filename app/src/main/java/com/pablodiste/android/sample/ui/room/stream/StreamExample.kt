package com.pablodiste.android.sample.ui.room.stream

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pablodiste.android.sample.ui.main.TopBar

@Composable
fun RoomStreamExample(viewModel: RoomStreamExampleViewModel, openDrawer: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopBar(
            title = "Store Stream Example (Room)",
            buttonIcon = Icons.Filled.Menu,
            onButtonClicked = { openDrawer() }
        )
        RoomStreamExamplePeopleList(viewModel)
    }
}

@Composable
fun RoomStreamExamplePeopleList(viewModel: RoomStreamExampleViewModel) {
    val uiState = viewModel.uiState.collectAsState()
    LazyColumn {
        items(uiState.value) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(text = it.name!!, style = MaterialTheme.typography.body1)
            }
        }
    }
}