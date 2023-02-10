package dev.pablodiste.datastore.sample.ui.main

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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.pablodiste.datastore.sample.ui.realm.fetch.FetchExample
import dev.pablodiste.datastore.sample.ui.realm.fetch.FetchExampleViewModel
import dev.pablodiste.datastore.sample.ui.realm.get.GetExample
import dev.pablodiste.datastore.sample.ui.realm.get.GetExampleViewModel
import dev.pablodiste.datastore.sample.ui.realm.stream.StreamExample
import dev.pablodiste.datastore.sample.ui.realm.stream.StreamExampleViewModel
import dev.pablodiste.datastore.sample.ui.room.concurrent.RoomConcurrentExample
import dev.pablodiste.datastore.sample.ui.room.concurrent.RoomConcurrentExampleViewModel
import dev.pablodiste.datastore.sample.ui.room.crud.RoomCrudExample
import dev.pablodiste.datastore.sample.ui.room.crud.RoomCrudExampleViewModel
import dev.pablodiste.datastore.sample.ui.room.dummyposts.DummyPostEdit
import dev.pablodiste.datastore.sample.ui.room.dummyposts.DummyPostEditViewModel
import dev.pablodiste.datastore.sample.ui.room.dummyposts.DummyPostView
import dev.pablodiste.datastore.sample.ui.room.dummyposts.DummyPostViewModel
import dev.pablodiste.datastore.sample.ui.room.dummyposts.DummyPostsList
import dev.pablodiste.datastore.sample.ui.room.dummyposts.DummyPostsViewModel
import dev.pablodiste.datastore.sample.ui.room.stream.RoomStreamDTOExample
import dev.pablodiste.datastore.sample.ui.room.stream.RoomStreamDTOExampleViewModel
import dev.pablodiste.datastore.sample.ui.room.stream.RoomStreamExample
import dev.pablodiste.datastore.sample.ui.room.stream.RoomStreamExampleViewModel
import dev.pablodiste.datastore.sample.viewmodels.viewModelFactory
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
                startDestination = DrawerScreens.Home.route
            ) {
                composable(DrawerScreens.Home.route) {
                    Home(openDrawer = { openDrawer() })
                }
                composable(DrawerScreens.DummyPosts.route) {
                    val viewModel = viewModel<DummyPostsViewModel>()
                    DummyPostsList(viewModel,
                        openDrawer = { openDrawer() },
                        onPostSelected = { postId -> navController.navigate("dummy_post/$postId") }
                    )
                }
                composable(DrawerScreens.ViewDummyPost.route,
                    arguments = listOf(navArgument("postId") { type = NavType.IntType })) {
                    val viewModel = viewModel<DummyPostViewModel>(factory = viewModelFactory {
                        DummyPostViewModel(it.arguments?.getInt("postId") ?: 0)
                    })
                    DummyPostView(viewModel,
                        openDrawer = { openDrawer() },
                        onDeletePressed = { navController.popBackStack() },
                        onEditPressed = { postId -> navController.navigate("dummy_post/edit/$postId") }
                    )
                }
                composable(DrawerScreens.EditDummyPost.route,
                    arguments = listOf(navArgument("postId") { type = NavType.IntType })) {
                    val viewModel = viewModel<DummyPostEditViewModel>(factory = viewModelFactory {
                        DummyPostEditViewModel(it.arguments?.getInt("postId") ?: 0)
                    })
                    DummyPostEdit(viewModel,
                        openDrawer = { openDrawer() }
                    ) { navController.popBackStack(route = "dummy_posts", inclusive = false) }
                }
                composable(DrawerScreens.RealmStreamExample.route) {
                    val viewModel = viewModel<StreamExampleViewModel>()
                    StreamExample(viewModel, openDrawer = { openDrawer() })
                }
                composable(DrawerScreens.RoomFetchExample.route) {
                    val viewModel = viewModel<dev.pablodiste.datastore.sample.ui.room.fetch.FetchExampleViewModel>()
                    dev.pablodiste.datastore.sample.ui.room.fetch.FetchExample(viewModel, openDrawer = { openDrawer() })
                }
                composable(DrawerScreens.RoomGetExample.route) {
                    val viewModel = viewModel<dev.pablodiste.datastore.sample.ui.room.get.GetExampleViewModel>()
                    dev.pablodiste.datastore.sample.ui.room.get.GetExample(viewModel, openDrawer = { openDrawer() })
                }
                composable(DrawerScreens.RoomErrorExample.route) {
                    val viewModel = viewModel<dev.pablodiste.datastore.sample.ui.room.errors.ErrorExampleViewModel>()
                    dev.pablodiste.datastore.sample.ui.room.errors.ErrorExample(viewModel, openDrawer = { openDrawer() })
                }
                composable(DrawerScreens.RoomConcurrentExample.route) {
                    val viewModel = viewModel<RoomConcurrentExampleViewModel>()
                    RoomConcurrentExample(viewModel, openDrawer = { openDrawer() })
                }
                composable(DrawerScreens.RoomCrudExample.route) {
                    val viewModel = viewModel<RoomCrudExampleViewModel>()
                    RoomCrudExample(viewModel, openDrawer = { openDrawer() })
                }
                composable(DrawerScreens.RoomStreamExample.route) {
                    val viewModel = viewModel<RoomStreamExampleViewModel>()
                    RoomStreamExample(viewModel, openDrawer = { openDrawer() })
                }
                composable(DrawerScreens.RoomStreamDTOExample.route) {
                    val viewModel = viewModel<RoomStreamDTOExampleViewModel>()
                    RoomStreamDTOExample(viewModel, openDrawer = { openDrawer() })
                }
                composable(DrawerScreens.RealmFetchExample.route) {
                    val viewModel = viewModel<FetchExampleViewModel>()
                    FetchExample(viewModel, openDrawer = { openDrawer() })
                }
                composable(DrawerScreens.RealmGetExample.route) {
                    val viewModel = viewModel<GetExampleViewModel>()
                    GetExample(viewModel, openDrawer = { openDrawer() })
                }
            }
        }
    }
}

@Composable
fun TopBar(title: String = "", buttonIcon: ImageVector, actions: @Composable (RowScope.() -> Unit) = {}, onButtonClicked: () -> Unit) {
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
        backgroundColor = MaterialTheme.colors.primaryVariant,
        actions = actions,
    )
}


sealed class DrawerScreens(val title: String, val route: String) {
    object Home : DrawerScreens("Store", "room_welcome")
    object RoomStreamExample : DrawerScreens("Stream Example (Room)", "room_stream")
    object RoomFetchExample : DrawerScreens("Fetch Example (Room)", "room_fetch")
    object RoomGetExample : DrawerScreens("Get Example (Room)", "room_get")
    object RoomErrorExample : DrawerScreens("Error Handling Example (Room)", "room_error")
    object RoomConcurrentExample : DrawerScreens("Concurrent Example (Room)", "room_concurrent")
    object RoomCrudExample : DrawerScreens("Crud Example (Room)", "room_crud")
    object RoomStreamDTOExample : DrawerScreens("Stream Example (DTO + Room + Ktor)", "room_stream_dto")
    object RealmStreamExample : DrawerScreens("Stream Example (Realm)", "realm_stream")
    object RealmFetchExample : DrawerScreens("Fetch Example (Realm)", "realm_fetch")
    object RealmGetExample : DrawerScreens("Get Example (Realm)", "realm_get")
    object DummyPosts : DrawerScreens("List of Posts (Room)", "dummy_posts")
    object ViewDummyPost : DrawerScreens("Post (Room)", "dummy_post/{postId}")
    object EditDummyPost : DrawerScreens("Edit Post (Room)", "dummy_post/edit/{postId}")
}

private val screens = listOf(
    DrawerScreens.RoomStreamExample,
    DrawerScreens.RoomFetchExample,
    DrawerScreens.RoomGetExample,
    DrawerScreens.RoomErrorExample,
    DrawerScreens.RoomConcurrentExample,
    DrawerScreens.RoomCrudExample,
    DrawerScreens.RoomStreamDTOExample,
    DrawerScreens.RealmStreamExample,
    DrawerScreens.RealmFetchExample,
    DrawerScreens.RealmGetExample,
    DrawerScreens.DummyPosts,
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
