package com.pablodiste.android.sample.ui.room.errors

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pablodiste.android.datastore.throttling.ThrottlingState
import com.pablodiste.android.sample.models.room.People
import com.pablodiste.android.sample.ui.main.BaseScreen
import java.util.*

@Composable
fun ErrorExample(viewModel: ErrorExampleViewModel, openDrawer: () -> Unit) {
    BaseScreen(title = "Error handling Example (Room)", openDrawer = openDrawer) {
        val uiState = viewModel.uiState.collectAsState()
        val throttlingState = viewModel.throttlingState.collectAsState()

        ErrorExamplePerson(uiState.value.loadingError, uiState.value.data, throttlingState.value) {
            viewModel.makeRequest()
        }
    }
}

@Composable
@Preview
fun ErrorExamplePerson(
    loadingError: Boolean = true,
    person: People? = null,
    throttlingState: ThrottlingState = ThrottlingState(isThrottling = false),
    retry: () -> Unit = {}
) {
    Column(modifier = Modifier.padding(10.dp)) {
        if (loadingError) {
            Column {
                Text(text = "Error fetching the data",
                    modifier = Modifier.padding(bottom = 10.dp)
                )
                val timeToNextCall = if (throttlingState.timestampUntilNextCall == 0L) "0" else
                    "" + (throttlingState.timestampUntilNextCall - Date().time ) / 1000
                Text(text = "Throttling: ${throttlingState.isThrottling}, " +
                            "Time to Next Allowed Call: ${timeToNextCall}s",
                    modifier = Modifier.padding(bottom = 10.dp)
                )
                Button(onClick = retry) {
                    Text(text = "Retry")
                }
            }
        } else {
            Text(
                text = "There was no error",
                modifier = Modifier.padding(bottom = 10.dp)
            )
            Text(text = "Name: ${person?.name.orEmpty()}")
            Text(text = "Height: ${person?.height.orEmpty()}")
            Text(text = "Mass: ${person?.mass.orEmpty()}")
        }
    }
}