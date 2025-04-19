package com.isis3510.growhub.view.home

// Import the specific ViewModels needed
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.isis3510.growhub.R
import com.isis3510.growhub.view.navigation.BottomNavigationBar
import com.isis3510.growhub.view.theme.GrowhubTheme
import com.isis3510.growhub.viewmodel.AuthViewModel
import com.isis3510.growhub.viewmodel.CategoriesViewModel
import com.isis3510.growhub.viewmodel.HomeEventsViewModel
import com.isis3510.growhub.viewmodel.LocationViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainView(
    navController: NavHostController,
    onLogout: () -> Unit,
    onClickChat: () -> Unit,
    onSearch: () -> Unit,
    // HERE WE MUST INJECT THE VIEW MODELS INTO THE CONSTRUCTOR
    authViewModel: AuthViewModel = viewModel(),
    categoriesViewModel: CategoriesViewModel = viewModel(),
    eventsViewModel: HomeEventsViewModel = viewModel(),
    locationViewModel: LocationViewModel = viewModel()
) {
    Scaffold(
        topBar = {
            TopBarView(
                authViewModel = authViewModel,
                locationViewModel = locationViewModel,
                onLogout = onLogout,
                onSearch = onSearch
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onClickChat() },
                modifier = Modifier.size(60.dp).offset(y=(-20).dp),
                shape = CircleShape,
                containerColor = Color(0xFF5669FF),
                contentColor = Color.White
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.chatbot),
                    contentDescription = "Chatbot",
                    modifier = Modifier
                        .fillMaxSize(),
                    tint = Color.Unspecified
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { innerPadding ->
        // Main scrollable content area
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 32.dp), // Increased from 16.dp for more space
            verticalArrangement = Arrangement.spacedBy(16.dp) // Space between items remains 16.dp
        ) {
            // 1. Categories Section
            item {
                CategoriesView(
                    categoriesViewModel = categoriesViewModel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp), // Padding below TopBar overlap area remains
                    onCategoryClick = { category ->
                        //TODO:Navigation to the Category detail
                        Log.d("HomeView", "Category clicked: ${category.name}")
                        // navController.navigate(...)
                    }
                )
            }

            // 2. Events Section
            item {
                EventsView(
                    eventsViewModel = eventsViewModel,
                    modifier = Modifier.fillMaxWidth(),
                    onEventClick = { event ->
                        //TODO: Navigation to the Event Detail
                        Log.d("HomeView", "Event clicked: ${event.name}")
                        // navController.navigate(...)
                    }
                )
            }
        }
        Box(modifier = Modifier.fillMaxSize().offset(y = 50.dp), contentAlignment = Alignment.BottomCenter) {
            BottomNavigationBar(navController = navController)
        }
    }
}



@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun MainViewPreview() {
    val navController = rememberNavController()
    GrowhubTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            // Preview requires providing ViewModels or using static data
            // Passing default viewModel() might result in empty states
            MainView(
                navController = navController,
                onLogout = {},
                onClickChat = {},
                onSearch = {},
                // Provide fake/mock ViewModels here for a better preview if needed
            )
        }
    }
}