package dev.pablodiste.datastore.sample.ui.room.dummyposts

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.pablodiste.datastore.sample.models.room.DummyPost
import dev.pablodiste.datastore.sample.ui.main.BaseScreen
import kotlinx.coroutines.launch

@Composable
fun DummyPostView(
    viewModel: DummyPostViewModel,
    openDrawer: () -> Unit,
    onDeletePressed: () -> Boolean,
    onEditPressed: (postId: Int) -> Unit) {

    val coroutineScope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()

    BaseScreen(
        title = "Post Details Screen (Room)",
        openDrawer = openDrawer,
        actions = {
            IconButton(onClick = { onEditPressed(uiState?.id ?: 0) }) {
                Icon(Icons.Filled.Edit, contentDescription = "Edit")
            }
            IconButton(onClick = {
                coroutineScope.launch {
                    viewModel.delete()
                    onDeletePressed()
                }
            }) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete")
            }
        }
    ) {
        DummyPostDetails(uiState)
    }
}

@Composable
fun DummyPostDetails(dummyPost: DummyPost?) {
    dummyPost ?: return
    Column(modifier = Modifier.padding(8.dp)) {
        Text(text = dummyPost.title.orEmpty(), style = MaterialTheme.typography.h5)
        Text(text = dummyPost.body.orEmpty(), style = MaterialTheme.typography.body1)
    }
}