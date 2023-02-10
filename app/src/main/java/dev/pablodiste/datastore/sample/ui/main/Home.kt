package dev.pablodiste.datastore.sample.ui.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun Home(openDrawer: () -> Unit) {
    BaseScreen(title = "Store", openDrawer = openDrawer,) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text("Explore the examples from the menu")
        }
    }
}
