package com.isis3510.growhub.model.objects

data class Category (
    val name: String
)

// Extension to convert between model entity and data entity
fun com.isis3510.growhub.local.data.CategoryEntity.toCategory(): Category {
    return Category(
        name = this.name
    )
}

fun Category.toEntity(): com.isis3510.growhub.local.data.CategoryEntity {
    return com.isis3510.growhub.local.data.CategoryEntity(
        name = this.name
    )
}
