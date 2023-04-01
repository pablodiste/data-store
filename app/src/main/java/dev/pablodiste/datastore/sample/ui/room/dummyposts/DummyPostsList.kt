package dev.pablodiste.datastore.sample.ui.room.dummyposts

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.pablodiste.datastore.sample.models.room.DummyPost
import dev.pablodiste.datastore.sample.ui.main.BaseScreen
import kotlinx.coroutines.launch

@Composable
fun DummyPostsList(viewModel: DummyPostsViewModel, openDrawer: () -> Unit, onPostSelected: (postId: Int) -> Unit) {
    BaseScreen(title = "List of Posts (Room)", openDrawer = openDrawer,) {
        val uiState by viewModel.uiState.collectAsState()
        val loading by viewModel.loading.collectAsState()
        Log.d("DummyPostsList", "Loading: $loading")
        DummyPostsList(uiState, loading, onPostSelected) { viewModel.refresh() }
    }
}

@Composable
@OptIn(ExperimentalMaterialApi::class)
fun DummyPostsList(
    posts: List<DummyPost> = listOf(),
    loading: Boolean = false,
    onPostSelected: (postId: Int) -> Unit = {},
    onRefresh: suspend () -> Unit = {}) {

    val refreshScope = rememberCoroutineScope()
    var refreshing by remember { mutableStateOf(false) }

    fun launchRefresh() = refreshScope.launch {
        onRefresh.invoke()
    }
    val state = rememberPullRefreshState(loading, ::launchRefresh)

    Box(Modifier.pullRefresh(state)) {
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
        PullRefreshIndicator(loading, state, Modifier.align(Alignment.TopCenter))
    }
}