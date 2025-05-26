package com.isis3510.growhub.view.profile

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.isis3510.growhub.viewmodel.FollowViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowingScreen(
    onNavigateBack: () -> Unit
) {
    val ctx = LocalContext.current
    val vm: FollowViewModel = viewModel()
    val following  by vm.followingItems.collectAsState()
    val suggestions by vm.suggestionsItems.collectAsState()

    // Errores en Toast
    LaunchedEffect(Unit) {
        vm.error.collect { msg ->
            Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Following") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1) Tu lista de following
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                items(following) { item ->
                    FollowRow(
                        item = item,
                        isFollowing = true,
                        onToggle = { vm.toggleFollow(item.userId, it) }
                    )
                }
            }
            // 2) Sección de sugerencias
            if (suggestions.isNotEmpty()) {
                HorizontalDivider()
                Text(
                    "Suggestions",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                ) {
                    items(suggestions) { item ->
                        FollowRow(
                            item = item,
                            isFollowing = false,
                            onToggle = { vm.toggleFollow(item.userId, it) }
                        )
                    }
                }
            }
        }
    }
}
