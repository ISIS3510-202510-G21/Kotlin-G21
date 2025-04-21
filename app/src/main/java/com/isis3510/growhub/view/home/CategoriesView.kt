package com.isis3510.growhub.view.home

import android.os.Build
import android.util.Log // Import Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.runtime.livedata.observeAsState
import com.isis3510.growhub.model.objects.Category
import com.isis3510.growhub.viewmodel.CategoriesViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

// Predefined colors for buttons
private val categoryColors = listOf(
    Color(0xffef635a), Color(0xfff59762), Color(0xff29d697),
    Color(0xff3b5998), Color(0xff8e44ad), Color(0xff2c3e50),
    Color(0xff16a085), Color(0xfff39c12)
)

// LazyListState extension
fun LazyListState.isScrolledNearEnd(buffer: Int = 1): Boolean {
    val visibleItems = layoutInfo.visibleItemsInfo
    if (visibleItems.isEmpty() || layoutInfo.totalItemsCount == 0) return false
    val lastVisibleIndex = visibleItems.last().index
    val totalItemCount = layoutInfo.totalItemsCount
    // Adjusted logic: check if last visible is the second to last item or later
    // when buffer is 1. This gives a little more room to trigger loading.
    return lastVisibleIndex >= totalItemCount - 1 - buffer
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CategoriesView(
    modifier: Modifier = Modifier,
    categoriesViewModel: CategoriesViewModel,
    onCategoryClick: (Category) -> Unit
) {
    val categories by categoriesViewModel.categories.observeAsState(emptyList())
    val isLoadingMore by categoriesViewModel.isLoadingMore.observeAsState(false)
    val hasReachedEnd by categoriesViewModel.hasReachedEnd.observeAsState(false)
    val listState = rememberLazyListState()

    // State for the temporary message visibility
    var showEndMessage by remember { mutableStateOf(false) }

    // Logging the state value during recompositions
    Log.d("CategoriesView", "Recomposition: showEndMessage = $showEndMessage, isLoadingMore = $isLoadingMore, hasReachedEnd = $hasReachedEnd")

    // --- Effect for observing the LiveData event ---
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val scope = rememberCoroutineScope() // Scope tied to the composable lifecycle

    DisposableEffect(lifecycleOwner, categoriesViewModel) { // Effect runs when keys change or on dispose
        val observer = androidx.lifecycle.Observer<Unit?> { _ ->
            if (!showEndMessage) { // Prevent starting a new timer if one is already running
                scope.launch {
                    Log.d("CategoriesView", "Coroutine launched: Setting showEndMessage = true")
                    showEndMessage = true
                    delay(3000L) // Keep message visible for 3 seconds
                    Log.d("CategoriesView", "Coroutine finished delay: Setting showEndMessage = false")
                    showEndMessage = false
                }
            } else {
                Log.d("CategoriesView", "SingleLiveEvent observed, but message already showing.")
            }
        }

        // Start observing
        categoriesViewModel.showNoMoreCategoriesMessage.observe(lifecycleOwner, observer)

        // Cleanup: Remove observer when the effect leaves composition
        onDispose {
            categoriesViewModel.showNoMoreCategoriesMessage.removeObserver(observer)
        }
    }


    // Effect to trigger loading when scrolled near the end (check logic)
    LaunchedEffect(listState, categories, isLoadingMore, hasReachedEnd) {
        snapshotFlow { listState.isScrolledNearEnd() }
            .distinctUntilChanged()
            .filter { isNearEnd -> isNearEnd } // Filter for true (near end)
            .collect {
                // Check conditions *inside* collect
                if (!isLoadingMore && !hasReachedEnd) {
                    Log.d("CategoriesView", "ScrollNearEnd detected & conditions met. Calling loadCategories()")
                    categoriesViewModel.loadCategories()
                } else {
                    Log.d("CategoriesView", "ScrollNearEnd detected BUT conditions NOT met (isLoadingMore=$isLoadingMore, hasReachedEnd=$hasReachedEnd)")
                }
            }
    }


    Box(modifier = modifier.fillMaxSize()) {

        Column(modifier = Modifier.fillMaxWidth()) {
            // Placeholder logic
            if (categories.isEmpty() && !isLoadingMore && !hasReachedEnd && categoriesViewModel.categories.value == null) { // Initial empty state
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    repeat(5) { CategoryButtonPlaceholder() }
                }
                Log.d("CategoriesView", "Displaying Placeholders")
            } else if (categories.isEmpty() && hasReachedEnd) { // Empty and truly nothing more
                Text(
                    "No categories available.",
                    modifier = Modifier.padding(16.dp).align(Alignment.CenterHorizontally)
                )
                Log.d("CategoriesView", "Displaying 'No categories available'")
            } else {
                // LazyRow for categories
                LazyRow(
                    state = listState,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
                ) {
                    items(categories, key = { category -> category.name }) { category ->
                        val index = categories.indexOf(category).coerceAtLeast(0) // Safe index access
                        val color = categoryColors[index % categoryColors.size]
                        CategoryButton(category = category, color = color, onClick = { onCategoryClick(category) })
                    }

                    // Loading indicator
                    if (isLoadingMore) {
                        item {
                            Box(
                                modifier = Modifier
                                    .height(45.dp)
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                                Log.d("CategoriesView", "Displaying Loading Indicator")
                            }
                        }
                    }
                }
            }
        }

        // --- Animated message overlay at the bottom ---
        // Add logging around AnimatedVisibility as well
        val isMessageActuallyVisible = showEndMessage
        Log.d("CategoriesView", "AnimatedVisibility wrapper: showEndMessage = $isMessageActuallyVisible")

        AnimatedVisibility(
            visible = isMessageActuallyVisible, // Use the state variable directly
            enter = fadeIn(animationSpec = tween(300)) + slideInVertically(initialOffsetY = { it / 2 }, animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300)) + slideOutVertically(targetOffsetY = { it / 2 }, animationSpec = tween(300)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        ) {
            // This block renders when visible = true
            Log.d("CategoriesView", "AnimatedVisibility content rendering!")
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color.DarkGray.copy(alpha = 0.85f),
                tonalElevation = 4.dp,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "There are no more categories",
                    color = Color.White,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

// CategoryButton and CategoryButtonPlaceholder remain the same
@Composable
private fun CategoryButton(category: Category, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        modifier = Modifier
            .height(45.dp)
            .defaultMinSize(minWidth = 100.dp)
    ) {
        Text(
            text = category.name,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
    }
}

@Composable
private fun CategoryButtonPlaceholder() {
    Box(
        modifier = Modifier
            .height(45.dp)
            .width(120.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.LightGray.copy(alpha = 0.3f))
    )
}