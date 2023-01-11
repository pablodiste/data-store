package dev.pablodiste.datastore.sample.ui.room.dummyposts

import androidx.compose.foundation.layout.Column
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
fun DummyPostView(viewModel: DummyPostViewModel, openDrawer: () -> Unit) {
    BaseScreen(title = "Post Details Screen (Room)", openDrawer = openDrawer) {
        val uiState by viewModel.uiState.collectAsState()
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