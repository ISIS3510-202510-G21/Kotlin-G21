package com.isis3510.growhub.view.home

import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
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
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.setValue
import com.isis3510.growhub.model.objects.Category
import com.isis3510.growhub.viewmodel.CategoriesViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

// Predefined colors for buttons
private val categoryColors = listOf(
    Color(0xffef635a), Color(0xfff59762), Color(0xff29d697),
    Color(0xff3b5998), Color(0xff8e44ad), Color(0xff2c3e50),
    Color(0xff16a085), Color(0xfff39c12)
)

// Función más estricta para detectar solo cuando estamos realmente cerca del final
fun LazyListState.isScrolledNearEnd(): Boolean {
    val visibleItems = layoutInfo.visibleItemsInfo
    if (visibleItems.isEmpty() || layoutInfo.totalItemsCount == 0) return false

    // Solo detectamos que estamos cerca del final cuando el último elemento visible
    // es el penúltimo o último de la lista
    val lastVisibleIndex = visibleItems.last().index
    val totalItemCount = layoutInfo.totalItemsCount

    // Solo consideramos "cerca del final" cuando estamos viendo el último elemento
    return lastVisibleIndex >= totalItemCount - 1
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
    val context = LocalContext.current

    // State for the temporary message visibility
    var showEndMessage by remember { mutableStateOf(false) }

    // Estado para controlar la visibilidad del indicador de carga con retardo
    var showLoadingIndicatorWithDelay by remember { mutableStateOf(false) }

    // LaunchedEffect para controlar la visibilidad del indicador con retardo
    LaunchedEffect(isLoadingMore) {
        if (isLoadingMore) {
            showLoadingIndicatorWithDelay = true
            // Espera 2.5 segundos
            delay(2500)
            showLoadingIndicatorWithDelay = false
        }
    }

    // Logging para debug
    Log.d("CategoriesView", "Recomposition: categories = ${categories.size}, isLoadingMore = $isLoadingMore, hasReachedEnd = $hasReachedEnd")

    // --- Effect for observing the LiveData event ---
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    DisposableEffect(lifecycleOwner, categoriesViewModel) {
        val observer = androidx.lifecycle.Observer<Unit?> { _ ->
            if (!showEndMessage) {
                scope.launch {
                    Log.d("CategoriesView", "Coroutine launched: Setting showEndMessage = true")
                    showEndMessage = true
                    delay(3000L)
                    Log.d("CategoriesView", "Coroutine finished delay: Setting showEndMessage = false")
                    showEndMessage = false
                }
            } else {
                Log.d("CategoriesView", "SingleLiveEvent observed, but message already showing.")
            }
        }

        categoriesViewModel.showNoMoreCategoriesMessage.observe(lifecycleOwner, observer)

        onDispose {
            categoriesViewModel.showNoMoreCategoriesMessage.removeObserver(observer)
        }
    }

    // Effect para cargar más categorías solo cuando el usuario ha llegado al final
    LaunchedEffect(listState) {
        snapshotFlow {
            // Combinamos la detección del final con el estado de desplazamiento
            val isAtEnd = listState.isScrolledNearEnd()
            val isScrollingFromUser = listState.isScrollInProgress

            // Solo consideramos que estamos al final si realmente el usuario está desplazando
            // y hemos llegado al último elemento
            isAtEnd && isScrollingFromUser
        }
            .distinctUntilChanged()
            .collect { shouldLoadMore ->
                if (shouldLoadMore) {
                    Log.d("CategoriesView", "User scroll reached end with isLoadingMore=$isLoadingMore, hasReachedEnd=$hasReachedEnd")
                    if (!isLoadingMore && !hasReachedEnd) {
                        Log.d("CategoriesView", "Calling loadCategories() after user reached end")
                        categoriesViewModel.loadCategories()
                    }
                }
            }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Manejo de estados de UI
            if (categories.isEmpty() && !isLoadingMore && !hasReachedEnd && categoriesViewModel.categories.value == null) {
                // Estado inicial de carga
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    repeat(5) { CategoryButtonPlaceholder() }
                }
                Log.d("CategoriesView", "Displaying Placeholders")
            } else if (categories.isEmpty() && hasReachedEnd) {
                // No hay categorías disponibles
                Text(
                    "No categories are available.",
                    modifier = Modifier.padding(16.dp).align(Alignment.CenterHorizontally)
                )
                Log.d("CategoriesView", "Displaying 'No categories available'")
            } else {
                // LazyRow para mostrar las categorías
                LazyRow(
                    state = listState,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
                ) {
                    items(categories, key = { category -> category.name }) { category ->
                        val index = categories.indexOf(category).coerceAtLeast(0)
                        val color = categoryColors[index % categoryColors.size]
                        CategoryButton(category = category, color = color, onClick = { onCategoryClick(category) })
                    }

                    // Indicador de carga al final
                    // Hacemos que siempre esté visible por 2.5 segundos
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
    }

    // Mostrar Toast cuando sea necesario
    LaunchedEffect(showEndMessage) {
        if (showEndMessage) {
            Toast.makeText(
                context,
                categoriesViewModel.categoriesEventualConnectivity(),
                Toast.LENGTH_SHORT
            ).show()
            Log.d("CategoriesView", "Toast displayed")
        }
    }
}

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