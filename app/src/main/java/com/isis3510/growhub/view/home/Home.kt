package com.isis3510.growhub.view.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.isis3510.growhub.model.objects.Category
import com.isis3510.growhub.model.objects.Event
import com.isis3510.growhub.view.navigation.BottomNavigationBar
import com.isis3510.growhub.view.theme.GrowhubTheme
import com.isis3510.growhub.viewmodel.*

/* ------------------------------------------------------------------ */
/*  VIEW – MAIN                                                       */
/* ------------------------------------------------------------------ */

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainView(
    navController: NavHostController,
    onLogout: () -> Unit,
    onClickChat: () -> Unit = {},             // ← parámetros extra con default
    onSearch: () -> Unit = {},
    /* ViewModels (se pueden inyectar en tests / previews) */
    authViewModel: AuthViewModel = viewModel(),
    categoriesViewModel: CategoriesViewModel = viewModel(),
    eventsViewModel: HomeEventsViewModel = viewModel(),
    locationViewModel: LocationViewModel = viewModel()
) {
    /* ------------ barra de estado del sistema (HEAD) --------------- */
    val sysUiController = rememberSystemUiController()
    SideEffect { sysUiController.setStatusBarColor(Color(0xFF4A43EC), darkIcons = false) }

    Scaffold(
        topBar = {
            SimpleTopBar(
                authViewModel = authViewModel,
                locationViewModel = locationViewModel,
                onLogout = onLogout,
                onSearch = onSearch
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onClickChat,
                shape = MaterialTheme.shapes.medium,
                containerColor = Color(0xFF5669FF),
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Chat, contentDescription = "Chatbot")
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { innerPadding ->

        /* -------------------- CONTENIDO SCROLLABLE -------------------- */
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp) // espacio para FAB + bottom bar
        ) {
            /* 1) Categorías ------------------------------------------------ */
            items(listOf(Unit)) {                     // sección única
                SimpleCategoriesView(
                    categoriesViewModel = categoriesViewModel,
                    modifier = Modifier.fillMaxWidth(),
                    onCategoryClick = { /* TODO navegación futura */ }
                )
            }

            /* 2) Eventos --------------------------------------------------- */
            items(listOf(Unit)) {
                EventsView(
                    eventsViewModel = eventsViewModel,
                    modifier = Modifier.fillMaxWidth(),
                    onEventClick = { /* TODO navegación a detalle */ }
                )
            }
        }

        /* -------------------- BOTTOM NAVIGATION ----------------------- */
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            BottomNavigationBar(navController = navController)
        }
    }
}

/* ------------------------------------------------------------------ */
/*  PLACEHOLDERS de develop (simplificados para compilar)             */
/*  Si ya existen en tu código, puedes eliminar estas versiones.      */
/* ------------------------------------------------------------------ */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SimpleTopBar(
    authViewModel: AuthViewModel,
    locationViewModel: LocationViewModel,
    onLogout: () -> Unit,
    onSearch: () -> Unit
) {
    /* Implementación mínima: solo un Row con título y botón Logout */
    TopAppBar(
        title = { Text("GrowHub") },
        actions = {
            IconButton(onClick = onLogout) {
                Icon(Icons.Default.Chat, contentDescription = "Logout") // icono placeholder
            }
        }
    )
}

@Composable
private fun SimpleCategoriesView(
    categoriesViewModel: CategoriesViewModel,
    modifier: Modifier = Modifier,
    onCategoryClick: (Category) -> Unit = {}
) {
    val sample = listOf(
        Category("Music"), Category("Tech"), Category("Sports"), Category("Art")
    )
    LazyColumn(modifier = modifier) {
        items(sample) { cat ->
            Text(
                text = cat.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
        }
    }
}

@Composable
private fun EventsView(
    eventsViewModel: HomeEventsViewModel,
    modifier: Modifier = Modifier,
    onEventClick: (Event) -> Unit = {}
) {
    val sample = listOf(
        Event(id = "", name = "Concert", imageUrl = "", description = "",
            cost = 0, attendees = emptyList(), startDate = "", endDate = "",
            category = "", location = "", city = "", skills = emptyList(),
            isUniversity = true, creator = ""),
        Event(id = "", name = "Hackathon", imageUrl = "", description = "",
            cost = 0, attendees = emptyList(), startDate = "", endDate = "",
            category = "", location = "", city = "", skills = emptyList(),
            isUniversity = true, creator = ""),
    )
    LazyColumn(modifier = modifier) {
        items(sample) { ev ->
            Text(
                text = ev.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
        }
    }
}

/* ------------------------------------------------------------------ */
/*  PREVIEW                                                           */
/* ------------------------------------------------------------------ */

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun MainViewPreview() {
    GrowhubTheme {
        MainView(
            navController = rememberNavController(),
            onLogout = {},
            onClickChat = {},
            onSearch = {}
        )
    }
}
