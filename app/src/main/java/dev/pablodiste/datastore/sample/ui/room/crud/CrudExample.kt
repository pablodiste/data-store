package dev.pablodiste.datastore.sample.ui.room.crud

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.pablodiste.datastore.sample.ui.main.BaseScreen

@Composable
fun RoomCrudExample(viewModel: RoomCrudExampleViewModel, openDrawer: () -> Unit) {
    BaseScreen(title = "Store CRUD Example (Room)", openDrawer = openDrawer,) {
        RoomCrudExamplePeopleList(viewModel)
    }
}

@Composable
fun RoomCrudExamplePeopleList(viewModel: RoomCrudExampleViewModel) {
    val uiState = viewModel.uiState.collectAsState()
    RoomCrud(text = uiState.value,
    create = { viewModel.create() },
    update = { viewModel.update() },
    delete = { viewModel.delete() })
}

@Composable
fun RoomCrud(
    create: () -> Unit = {},
    update: () -> Unit = {},
    delete: () -> Unit = {},
    text: String
) {
    Column(modifier = Modifier.padding(5.dp)) {
        Row(horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = create) {
                Text(text = "Create")
            }
            Spacer(Modifier.width(5.dp))
            Button(onClick = update) {
                Text(text = "Update")
            }
            Spacer(Modifier.width(5.dp))
            Button(onClick = delete) {
                Text(text = "Delete")
            }
        }
        Text(text = text, style = MaterialTheme.typography.body1)
    }
}

@Preview
@Composable
fun RoomCrudPreview() {
    RoomCrud(text = "Hello World")
}