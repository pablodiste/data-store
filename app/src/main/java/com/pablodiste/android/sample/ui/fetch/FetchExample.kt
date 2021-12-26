package com.pablodiste.android.sample.ui.fetch

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.pablodiste.android.sample.ui.main.TopBar

@Composable
fun FetchExample(viewModel: FetchExampleViewModel, openDrawer: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopBar(
            title = "Store Fetch Example",
            buttonIcon = Icons.Filled.Menu,
            onButtonClicked = { openDrawer() }
        )
        FetchExamplePerson(viewModel)
    }
}

@Composable
fun FetchExamplePerson(viewModel: FetchExampleViewModel) {
    val uiState = viewModel.uiState.collectAsState()
    Text(text = uiState.value.name.orEmpty())
}