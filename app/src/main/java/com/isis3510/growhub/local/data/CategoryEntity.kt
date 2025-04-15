package com.isis3510.growhub.local.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.isis3510.growhub.model.objects.Category

@Entity(tableName = "categories")
data class CategoryEntity(
    // Use category name as primary key, assuming names are unique.
    @PrimaryKey val name: String,
    val order: Int // Store the original order from Firebase fetch
)

// Extension function to map from Entity to Domain model
fun CategoryEntity.toDomainModel(): Category {
    return Category(
        name = this.name
    )
}

// Extension function to map from Domain model to Entity
fun Category.toEntity(order: Int): CategoryEntity {
    return CategoryEntity(
        name = this.name,
        order = order
    )
}