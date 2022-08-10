package com.pablodiste.android.sample.ui.main

import androidx.compose.foundation.layout.Column
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
fun Home(openDrawer: () -> Unit) {
    BaseScreen(title = "Store", openDrawer = openDrawer) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text("Explore the examples from the menu")
        }
    }
}
