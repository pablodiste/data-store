package com.pablodiste.android.sample.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pablodiste.android.sample.ui.stream1.StreamExampleViewModel
import com.pablodiste.android.sample.ui.stream1.StreamExample
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
                startDestination = DrawerScreens.StreamExample.route
            ) {
                composable(DrawerScreens.StreamExample.route) {
                    val viewModel = viewModel<StreamExampleViewModel>()
                    StreamExample(viewModel, openDrawer = { openDrawer() })
                }
                /*
                composable(DrawerScreens.Account.route) {
                    Account(
                        openDrawer = {
                            openDrawer()
                        }
                    )
                }
                composable(DrawerScreens.Help.route) {
                    Help(
                        navController = navController
                    )
                }

                 */
            }
        }
    }
}

@Composable
fun Account(openDrawer: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopBar(
            title = "Account",
            buttonIcon = Icons.Filled.Menu,
            onButtonClicked = { openDrawer() }
        )
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Account.", style = MaterialTheme.typography.h4)
        }
    }
}

@Composable
fun Help(navController: NavController) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopBar(
            title = "Help",
            buttonIcon = Icons.Filled.ArrowBack,
            onButtonClicked = { navController.popBackStack() }
        )
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Help.", style = MaterialTheme.typography.h4)
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
    object StreamExample : DrawerScreens("Stream Example", "stream1")
    object Account : DrawerScreens("Account", "account")
    object Help : DrawerScreens( "Help", "help")
}

private val screens = listOf(
    DrawerScreens.StreamExample,
    DrawerScreens.Account,
    DrawerScreens.Help
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
