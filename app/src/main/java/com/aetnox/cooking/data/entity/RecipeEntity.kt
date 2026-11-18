package com.aetnox.cooking.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val prepTimeMinutes: Int = 0,
    val cookTimeMinutes: Int = 0,
    val servingsEnabled: Boolean = true,
    val baseServings: Double = 4.0,
    val yieldUnit: String = "порций",
    val status: String = STATUS_PLANNED,
    @ColumnInfo(name = "is_favorite", defaultValue = "0")
    val isFavorite: Boolean = false,
    val rating: Int = 0,
    val postCookNote: String = "",
    val chefHeatingType: String = "",
    val chefTemperature: String = "",
    val chefTiming: String = "",
    val chefReadinessSign: String = "",
    val notes: String = "",
    val rawNotes: String = "",
    val sourceUrl: String = "",
    val imageUrisJson: String = "[]",
    val calories: Double? = null,
    val protein: Double? = null,
    val fat: Double? = null,
    val carbs: Double? = null,
    val cloudId: String? = null,
    val cloudAccessCode: String? = null,
    @ColumnInfo(name = "is_in_cart", defaultValue = "0")
    val isInCart: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val STATUS_PLANNED = "PLANNED"         // В планах
        const val STATUS_VERIFIED = "VERIFIED"       // Проверено
        const val STATUS_NEEDS_WORK = "NEEDS_WORK"   // Работа над ошибками
        const val STATUS_ARCHIVED = "ARCHIVED"       // Архив
    }
}
