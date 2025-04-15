package com.isis3510.growhub.view.home

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.isis3510.growhub.model.objects.Category
import com.isis3510.growhub.viewmodel.CategoriesViewModel
import com.isis3510.growhub.viewmodel.CategoriesUiState

// Predefined list of colors for categories
private val categoryColors = listOf(
    Color(0xffef635a), Color(0xfff59762), Color(0xff29d697),
    Color(0xff3b5998), Color(0xff8e44ad), Color(0xff2c3e50),
    Color(0xff16a085), Color(0xfff39c12)
)

/**
 * Extension function for LazyListState to check if the user has scrolled near the end.
 * @param buffer The number of items from the end to start triggering the load.
 * @return True if scrolled near the end, false otherwise.
 */
fun LazyListState.isScrolledToEnd(buffer: Int = 2): Boolean {
    // Get information about the last visible item
    val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull() ?: return false
    // Calculate the threshold index
    val thresholdIndex = layoutInfo.totalItemsCount - 1 - buffer
    // Check if the last visible item's index is at or past the threshold
    return lastVisibleItem.index >= thresholdIndex
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CategoriesView(
    modifier: Modifier = Modifier,
    categoriesViewModel: CategoriesViewModel,
    onCategoryClick: (Category) -> Unit // Callback for when a category is clicked
) {
    // Observe the UI state from the ViewModel using lifecycle awareness
    val uiState by categoriesViewModel.uiState.collectAsStateWithLifecycle()
    // Create and remember the state for the LazyRow scroll position
    val lazyListState = rememberLazyListState()

    // Derived state recalculates efficiently only when dependent states change.
    // Determines if conditions are met to load more items.
    val shouldLoadMore by remember {
        derivedStateOf {
            lazyListState.isScrolledToEnd() && // Check if near the end of the list
                    uiState.canLoadMore &&       // Check if the ViewModel says more can be loaded
                    !uiState.isLoadingMore    // Check that we aren't already loading more
        }
    }

    // This triggers the loading of the next page of categories.
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            Log.d("CategoriesView", "Scrolled near end, triggering load more.")
            categoriesViewModel.loadMoreCategories()
        }
    }

    // FRONT END KEPT FROM PREVIOUS SPRINT
    Column(modifier = modifier.fillMaxWidth()) {
        // ========================================================
        // STRATEGY: CACHING THEN FALLING TO NETWORK
        // ========================================================
        // 1. Initial Loading State (only show if list is currently empty)
        if (uiState.isLoading && uiState.categories.isEmpty()) {
            // Display placeholders for the entire row during initial load
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                repeat(5) { CategoryButtonPlaceholder() } // Show 5 placeholders
            }
        }
        // 2. Initial Error State (only show if list is currently empty)
        else if (uiState.error != null && uiState.categories.isEmpty()) {
            // Display an error message if initial loading failed
            Text(
                text = uiState.error ?: "Failed to load categories.",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
        // 3. Loaded State (display categories and handle 'loading more')
        else {
            // Horizontal list of categories
            LazyRow(
                state = lazyListState, // Attach the LazyListState
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp), // Space between items
                contentPadding = PaddingValues(horizontal = 16.dp) // Padding at start/end
            ) {
                // Display the fetched categories
                itemsIndexed(
                    items = uiState.categories,
                    // Use category name as a stable key for item identity.
                    key = { _, category -> category.name }
                ) { index, category ->
                    val color = categoryColors[index % categoryColors.size]
                    CategoryButton(
                        category = category,
                        color = color,
                        onClick = { onCategoryClick(category) } // Trigger callback on click
                    )
                }

                // Display a loading indicator at the end if more items are being loaded
                if (uiState.isLoadingMore) {
                    item { // Add a final item for the loading indicator
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .height(45.dp) // Match button height for alignment
                                .padding(horizontal = 8.dp) // Some padding around indicator
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }

            // Optional: Display a small error message below the row if 'load more' failed
            if (uiState.error != null && uiState.categories.isNotEmpty() && !uiState.isLoadingMore) {
                Text(
                    text = "Couldn't load more: ${uiState.error}",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }
        }
    }
}

// --- Reusable Button Composable ---
@Composable
private fun CategoryButton(category: Category, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color), // Use passed color
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        modifier = Modifier
            .height(45.dp) // Fixed height
            .defaultMinSize(minWidth = 100.dp) // Ensure a minimum tappable width
    ) {
        Text(
            text = category.name,
            color = Color.White, // Text color for contrast on colored buttons
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis, // Handle long names
            maxLines = 1 // Ensure single line
        )
    }
}

// --- Placeholder for Loading State ---
@Composable
private fun CategoryButtonPlaceholder() {
    Box(
        modifier = Modifier
            .height(45.dp) // Match button height
            .width(120.dp) // Approximate button width
            .clip(RoundedCornerShape(16.dp)) // Match button shape
            .background(Color.LightGray.copy(alpha = 0.3f)) // Placeholder color
    )
}
