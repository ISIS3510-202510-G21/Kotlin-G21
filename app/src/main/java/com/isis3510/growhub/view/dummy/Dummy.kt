package com.isis3510.growhub.view.dummy

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "This is a placeholder for route testing: $title",
            fontSize = 18.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PlaceholderScreenPreview() {
    PlaceholderScreen(title = "Test Screen")
}