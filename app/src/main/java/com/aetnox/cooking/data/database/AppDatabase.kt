package com.aetnox.cooking.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.aetnox.cooking.data.dao.RecipeDao
import com.aetnox.cooking.data.entity.IngredientEntity
import com.aetnox.cooking.data.entity.RecipeEntity
import com.aetnox.cooking.data.entity.RecipeTagCrossRef
import com.aetnox.cooking.data.entity.StepEntity
import com.aetnox.cooking.data.entity.TagEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import androidx.room.migration.Migration

@Database(
    entities = [
        RecipeEntity::class,
        IngredientEntity::class,
        StepEntity::class,
        TagEntity::class,
        RecipeTagCrossRef::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun recipeDao(): RecipeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE recipes ADD COLUMN is_favorite INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE recipes ADD COLUMN cloudId TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE recipes ADD COLUMN cloudAccessCode TEXT DEFAULT NULL")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE recipes ADD COLUMN is_in_cart INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cooking_recipes.db"
                )
                    .addMigrations(MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                val database = getInstance(context)
                                val sample = SampleData.createSampleRecipeData()
                                database.recipeDao().saveFullRecipe(
                                    recipe = sample.recipe,
                                    ingredients = sample.ingredients,
                                    steps = sample.steps,
                                    tags = sample.tags
                                )
                            }
                        }

                        override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                            super.onDestructiveMigration(db)
                            // Страховка: если схема изменится без миграции, сначала
                            // сохраняем всю БД в JSON-файл, и только потом Room её стирает.
                            runCatching { dumpAllTablesToFile(context, db) }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        /**
         * Схемо-независимый дамп всех таблиц (SELECT * с именами колонок).
         * Работает даже тогда, когда entity-классы уже не соответствуют старой схеме.
         */
        private fun dumpAllTablesToFile(context: Context, db: SupportSQLiteDatabase) {
            val root = org.json.JSONObject()
            db.query(
                "SELECT name FROM sqlite_master WHERE type = 'table' " +
                    "AND name NOT LIKE 'sqlite_%' AND name != 'android_metadata'"
            ).use { tables ->
                while (tables.moveToNext()) {
                    val tableName = tables.getString(0)
                    val rows = org.json.JSONArray()
                    db.query("SELECT * FROM \"$tableName\"").use { cursor ->
                        while (cursor.moveToNext()) {
                            val row = org.json.JSONObject()
                            for (i in 0 until cursor.columnCount) {
                                row.put(cursor.getColumnName(i), cursor.getString(i))
                            }
                            rows.put(row)
                        }
                    }
                    root.put(tableName, rows)
                }
            }
            val file = java.io.File(context.filesDir, "destructive_migration_backup.json")
            file.writeText(
                org.json.JSONObject()
                    .put("dumpedAt", System.currentTimeMillis())
                    .put("warning", "База была пересоздана после изменения схемы. Импортируйте данные вручную.")
                    .put("tables", root)
                    .toString()
            )
            android.util.Log.w("AppDatabase", "Деструктивная миграция: данные сохранены в ${file.absolutePath}")
        }
    }
}
