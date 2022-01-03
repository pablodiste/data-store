package com.pablodiste.android.sample.ui.realm.stream

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
import com.pablodiste.android.sample.ui.main.BaseScreen

@Composable
fun StreamExample(viewModel: StreamExampleViewModel, openDrawer: () -> Unit) {
    BaseScreen(title = "Store Stream Example (Realm)", openDrawer = openDrawer) {
        StreamExamplePeopleList(viewModel)
    }
}

@Composable
fun StreamExamplePeopleList(viewModel: StreamExampleViewModel) {
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