package com.pablodiste.android.sample.ui.room.errors

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pablodiste.android.sample.ui.main.BaseScreen

@Composable
fun ErrorExample(viewModel: ErrorExampleViewModel, openDrawer: () -> Unit) {
    BaseScreen(title = "Error handling Example (Room)", openDrawer = openDrawer) {
        ErrorExamplePerson(viewModel)
    }
}

@Composable
fun ErrorExamplePerson(viewModel: ErrorExampleViewModel) {
    val uiState = viewModel.uiState.collectAsState()
    Column(modifier = Modifier.padding(10.dp)) {
        if (uiState.value.loadingError) {
            Text(text = "Error fetching the data",
                modifier = Modifier.padding(bottom = 10.dp)
            )
        } else {
            Text(
                text = "There was no error",
                modifier = Modifier.padding(bottom = 10.dp)
            )
            Text(text = "Name: ${uiState.value.data.name.orEmpty()}")
            Text(text = "Height: ${uiState.value.data.height.orEmpty()}")
            Text(text = "Mass: ${uiState.value.data.mass.orEmpty()}")
        }
    }
}