package com.pablodiste.android.sample

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModelProvider
import com.pablodiste.android.sample.ui.main.MainViewModel

class MainActivity2 : ComponentActivity() {

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        viewModel.getPeople()
        setContent {
            PeopleList(viewModel)
        }
    }
}

@Composable
fun MessageCard(name: String) {
    Text(text = "Hello $name!")
}

@Composable
fun PeopleList(viewModel: MainViewModel) {
    val uiState = viewModel.uiState.collectAsState()
    LazyColumn {
        items(uiState.value) {
            MessageCard(it.name!!)
        }
    }
}

@Preview
@Composable
fun PreviewPeopleList() {
    /*
    PeopleList(listOf(
        People().apply { name = "A" },
        People().apply { name = "B" },
        People().apply { name = "C" },
    ))

     */
}
