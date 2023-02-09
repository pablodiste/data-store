package dev.pablodiste.datastore.sample.ui.room.dummyposts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.pablodiste.datastore.sample.models.room.DummyPost
import dev.pablodiste.datastore.sample.ui.main.BaseScreen

@Composable
fun DummyPostsList(viewModel: DummyPostsViewModel, openDrawer: () -> Unit, onPostSelected: (postId: Int) -> Unit) {
    BaseScreen(title = "List of Posts (Room)", openDrawer = openDrawer,) {
        val uiState by viewModel.uiState.collectAsState()
        DummyPostsList(uiState, onPostSelected)
    }
}

@Composable
fun DummyPostsList(posts: List<DummyPost> = listOf(), onPostSelected: (postId: Int) -> Unit = {}) {
    LazyColumn {
        items(posts) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clickable { onPostSelected(it.id) }
            ) {
                Text(text = it.title!!, style = MaterialTheme.typography.body1)
            }
        }
    }
}