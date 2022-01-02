package com.pablodiste.android.sample.ui.room.fetch

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
    Column(modifier = Modifier.padding(10.dp)) {
        Text(text = "This information is fetched from the API and then cached",
             modifier = Modifier.padding(bottom = 10.dp))
        Text(text = "Name: ${uiState.value.name.orEmpty()}")
        Text(text = "Height: ${uiState.value.height.orEmpty()}")
        Text(text = "Mass: ${uiState.value.mass.orEmpty()}")
    }
}