package com.pablodiste.android.sample.ui.get

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
fun GetExample(viewModel: GetExampleViewModel, openDrawer: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopBar(
            title = "Store Get Example",
            buttonIcon = Icons.Filled.Menu,
            onButtonClicked = { openDrawer() }
        )
        GetExamplePerson(viewModel)
    }
}

@Composable
fun GetExamplePerson(viewModel: GetExampleViewModel) {
    val uiState = viewModel.uiState.collectAsState()
    Text(text = uiState.value.name.orEmpty())
}