package com.aetnox.cooking.ui.util

import android.content.Context
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class CustomCartItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val isChecked: Boolean = false
)

object CartManager {
    private const val PREFS_NAME = "cooking_cart_prefs"
    private const val KEY_CHECKED_KEYS = "cart_checked_keys"
    private const val KEY_CUSTOM_ITEMS = "cart_custom_items"
    private const val KEY_RECIPE_SERVINGS = "cart_recipe_servings"

    private val json = Json { ignoreUnknownKeys = true }

    // Observable Compose state
    val checkedKeys = mutableStateMapOf<String, Boolean>()
    val customItems = mutableStateOf<List<CustomCartItem>>(emptyList())
    val recipeServings = mutableStateMapOf<Long, Double>()

    private var initialized = false

    fun init(context: Context) {
        if (initialized) return
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Load checked keys
        val savedChecked = prefs.getStringSet(KEY_CHECKED_KEYS, emptySet()) ?: emptySet()
        checkedKeys.clear()
        savedChecked.forEach { checkedKeys[it] = true }

        // Load custom items
        val customJson = prefs.getString(KEY_CUSTOM_ITEMS, "[]") ?: "[]"
        val items = runCatching {
            json.decodeFromString<List<CustomCartItem>>(customJson)
        }.getOrDefault(emptyList())
        customItems.value = items

        // Load recipe servings map
        val servingsJson = prefs.getString(KEY_RECIPE_SERVINGS, "{}") ?: "{}"
        val servingsMap = runCatching {
            json.decodeFromString<Map<Long, Double>>(servingsJson)
        }.getOrDefault(emptyMap())
        recipeServings.clear()
        recipeServings.putAll(servingsMap)

        initialized = true
    }

    fun toggleCheckedKey(context: Context, key: String) {
        val current = checkedKeys[key] == true
        if (current) {
            checkedKeys.remove(key)
        } else {
            checkedKeys[key] = true
        }
        saveCheckedKeys(context)
    }

    fun isKeyChecked(key: String): Boolean {
        return checkedKeys[key] == true
    }

    fun addCustomItem(context: Context, name: String) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return
        val newItem = CustomCartItem(name = trimmed)
        val updated = customItems.value + newItem
        customItems.value = updated
        saveCustomItems(context)
    }

    fun toggleCustomItem(context: Context, id: String) {
        val updated = customItems.value.map {
            if (it.id == id) it.copy(isChecked = !it.isChecked) else it
        }
        customItems.value = updated
        saveCustomItems(context)
    }

    fun removeCustomItem(context: Context, id: String) {
        val updated = customItems.value.filter { it.id != id }
        customItems.value = updated
        saveCustomItems(context)
    }

    fun setRecipeServings(context: Context, recipeId: Long, servings: Double) {
        if (servings <= 0.5) return
        recipeServings[recipeId] = servings
        saveRecipeServings(context)
    }

    fun getRecipeServings(recipeId: Long, defaultServings: Double): Double {
        return recipeServings[recipeId] ?: defaultServings
    }

    fun clearCheckedKeys(context: Context) {
        checkedKeys.clear()
        saveCheckedKeys(context)
    }

    fun clearCustomItems(context: Context) {
        customItems.value = emptyList()
        saveCustomItems(context)
    }

    fun clearAll(context: Context) {
        checkedKeys.clear()
        customItems.value = emptyList()
        recipeServings.clear()
        saveCheckedKeys(context)
        saveCustomItems(context)
        saveRecipeServings(context)
    }

    private fun saveCheckedKeys(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putStringSet(KEY_CHECKED_KEYS, checkedKeys.keys.toSet())
            .apply()
    }

    private fun saveCustomItems(context: Context) {
        val jsonStr = json.encodeToString(customItems.value)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_CUSTOM_ITEMS, jsonStr)
            .apply()
    }

    private fun saveRecipeServings(context: Context) {
        val jsonStr = json.encodeToString(recipeServings.toMap())
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_RECIPE_SERVINGS, jsonStr)
            .apply()
    }
}
