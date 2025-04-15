package com.isis3510.growhub.view.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.isis3510.growhub.model.objects.Category
import com.isis3510.growhub.viewmodel.CategoriesViewModel

// Predefined list of colors for categories
private val categoryColors = listOf(
    Color(0xffef635a), Color(0xfff59762), Color(0xff29d697),
    Color(0xff3b5998), Color(0xff8e44ad), Color(0xff2c3e50),
    Color(0xff16a085), Color(0xfff39c12)
)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CategoriesView(
    modifier: Modifier = Modifier,
    categoriesViewModel: CategoriesViewModel,
    onCategoryClick: (Category) -> Unit // Callback for category click
) {
    val categories = categoriesViewModel.categories // Observer Pattern

    Column(modifier = modifier.fillMaxWidth()) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            if (categories.isEmpty()) {
                // Show placeholders if categories are empty (loading state)
                items(5) {
                    CategoryButtonPlaceholder()
                }
            } else {
                // Use category.name as the key.
                itemsIndexed(
                    items = categories,
                    key = { _, category -> category.name }
                ) { index, category ->
                    val color = categoryColors[index % categoryColors.size] // Use index for color
                    CategoryButton(
                        category = category,
                        color = color,
                        onClick = { onCategoryClick(category) }
                    )
                }
            }
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