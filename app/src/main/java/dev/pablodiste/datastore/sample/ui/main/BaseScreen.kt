package dev.pablodiste.datastore.sample.ui.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun BaseScreen(
    title: String,
    openDrawer: () -> Unit,
    actions: @Composable (RowScope.() -> Unit) = {},
    content: @Composable (ColumnScope.() -> Unit)
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopBar(
            title = title,
            buttonIcon = Icons.Filled.Menu,
            onButtonClicked = { openDrawer() },
            actions = actions
        )
        content()
    }
}