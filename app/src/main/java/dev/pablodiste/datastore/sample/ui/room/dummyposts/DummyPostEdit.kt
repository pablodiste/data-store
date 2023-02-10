package dev.pablodiste.datastore.sample.ui.room.dummyposts

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.pablodiste.datastore.sample.models.room.DummyPost
import dev.pablodiste.datastore.sample.ui.main.BaseScreen
import kotlinx.coroutines.launch

@Composable
fun DummyPostEdit(viewModel: DummyPostEditViewModel,
                  openDrawer: () -> Unit,
                  onSavePressed: () -> Boolean,
    ) {
    val coroutineScope = rememberCoroutineScope()
    BaseScreen(
        title = "Post Details Screen (Room)",
        openDrawer = openDrawer,
        actions = {
            IconButton(onClick = {
                coroutineScope.launch {
                    viewModel.update()
                    onSavePressed()
                }
            }) {
                Icon(Icons.Filled.Check, contentDescription = "Save")
            }
        }
    ) {
        DummyPostEditor(viewModel)
    }
}

@Composable
fun DummyPostEditor(viewModel: DummyPostEditViewModel) {
    val dummyPost: DummyPost = viewModel.uiState ?: return
    Column(modifier = Modifier.padding(8.dp)) {
        OutlinedTextField(
            value = dummyPost.title.orEmpty(),
            modifier = Modifier.fillMaxWidth(),
            onValueChange = { value -> viewModel.uiState = dummyPost.copy(title = value) },
            placeholder = { Text(text = "Title", style = TextStyle(fontSize = 18.sp, color = Color.LightGray)) },
        )
        OutlinedTextField(
            value = dummyPost.body.orEmpty(),
            modifier = Modifier.fillMaxWidth().height(120.dp),
            onValueChange = { value -> viewModel.uiState = dummyPost.copy(body = value) },
            placeholder = { Text(text = "Body", style = TextStyle(fontSize = 18.sp, color = Color.LightGray)) },
        )
    }
}