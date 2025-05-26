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
fun FollowersScreen(
    onNavigateBack: () -> Unit
) {
    val ctx = LocalContext.current
    val vm: FollowViewModel = viewModel()
    val followers by vm.followersItems.collectAsState()
    val following by vm.followingItems.collectAsState()

    LaunchedEffect(Unit) {
        vm.error.collect { msg ->
            Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Followers") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(Modifier
            .fillMaxSize()
            .padding(innerPadding)
        ) {
            LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                items(followers) { item ->
                    // isFollowing = si este follower también está en nuestra lista de following
                    val isFollowing = following.any { it.userId == item.userId }
                    FollowRow(
                        item = item,
                        isFollowing = isFollowing,
                        onToggle = { shouldFollow ->
                            vm.toggleFollow(item.userId, shouldFollow)
                        }
                    )
                }
            }
        }
    }
}