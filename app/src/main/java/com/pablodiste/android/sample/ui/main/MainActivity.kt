package com.pablodiste.android.sample.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pablodiste.android.sample.ui.realm.fetch.FetchExample
import com.pablodiste.android.sample.ui.realm.fetch.FetchExampleViewModel
import com.pablodiste.android.sample.ui.realm.get.GetExample
import com.pablodiste.android.sample.ui.realm.get.GetExampleViewModel
import com.pablodiste.android.sample.ui.realm.stream.StreamExample
import com.pablodiste.android.sample.ui.realm.stream.StreamExampleViewModel
import com.pablodiste.android.sample.ui.room.stream.RoomStreamExample
import com.pablodiste.android.sample.ui.room.stream.RoomStreamExampleViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppMainScreen()
        }
    }
}

@Composable
fun AppMainScreen() {
    val navController = rememberNavController()
    Surface(color = MaterialTheme.colors.background) {
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        val openDrawer = {
            scope.launch {
                drawerState.open()
            }
        }
        ModalDrawer(
            drawerState = drawerState,
            gesturesEnabled = drawerState.isOpen,
            drawerContent = {
                Drawer(
                    onDestinationClicked = { route ->
                        scope.launch {
                            drawerState.close()
                        }
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                )
            }
        ) {
            NavHost(
                navController = navController,
                startDestination = DrawerScreens.RoomStreamExample.route
            ) {
                composable(DrawerScreens.RealmStreamExample.route) {
                    val viewModel = viewModel<StreamExampleViewModel>()
                    StreamExample(viewModel, openDrawer = { openDrawer() })
                }
                composable(DrawerScreens.RoomStreamExample.route) {
                    val viewModel = viewModel<RoomStreamExampleViewModel>()
                    RoomStreamExample(viewModel, openDrawer = { openDrawer() })
                }
                composable(DrawerScreens.FetchExample.route) {
                    val viewModel = viewModel<FetchExampleViewModel>()
                    FetchExample(viewModel, openDrawer = { openDrawer() })
                }
                composable(DrawerScreens.GetExample.route) {
                    val viewModel = viewModel<GetExampleViewModel>()
                    GetExample(viewModel, openDrawer = { openDrawer() })
                }
            }
        }
    }
}

@Composable
fun TopBar(title: String = "", buttonIcon: ImageVector, onButtonClicked: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = title
            )
        },
        navigationIcon = {
            IconButton(onClick = { onButtonClicked() } ) {
                Icon(buttonIcon, contentDescription = "")
            }
        },
        backgroundColor = MaterialTheme.colors.primaryVariant
    )
}


sealed class DrawerScreens(val title: String, val route: String) {
    object RealmStreamExample : DrawerScreens("Stream Example (Realm)", "stream1")
    object RoomStreamExample : DrawerScreens("Stream Example (Room)", "stream2")
    object FetchExample : DrawerScreens("Fetch Example", "fetch1")
    object GetExample : DrawerScreens("Get Example", "get1")
}

private val screens = listOf(
    DrawerScreens.RealmStreamExample,
    DrawerScreens.RoomStreamExample,
    DrawerScreens.FetchExample,
    DrawerScreens.GetExample,
)

@Composable
fun Drawer(
    modifier: Modifier = Modifier,
    onDestinationClicked: (route: String) -> Unit
) {
    Column(
        modifier
            .fillMaxSize()
            .padding(top = 20.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .padding(bottom = 20.dp)
        ) {
            Text(text = "Store Examples", style = MaterialTheme.typography.h6)
        }
        screens.forEach { screen ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { onDestinationClicked(screen.route) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(text = screen.title, style = MaterialTheme.typography.body1)
            }
        }
    }
}

@Preview
@Composable
fun PreviewPeopleList() {
    AppMainScreen()
}
