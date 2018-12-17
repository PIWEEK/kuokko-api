package kuokko.api.db

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.dao.UUIDTable
import java.util.UUID

fun Table.uuidPK() = uuid("id").primaryKey().clientDefault { UUID.randomUUID() }

object RecipeDB: Table("recipes") {
    val id = uuidPK()
    val title = text("name")
    val cookTime = varchar("cook_time", 255).nullable()
    val preparationTime = varchar("preparation_time", 255).nullable()
    val totalTime = varchar("total_time", 255).nullable()
    val dificulty = varchar("dificulty", 255).nullable()
    val language = varchar("language", 255).nullable()
    val photoUrl = text("photo_url").nullable()
    val recipeUrl = text("recipe_url").nullable()
}

object IngredientDB: Table("ingredients") {
    val id = uuidPK()
    val name = varchar("name", 255)
}

object ToolDB: Table("tools") {
    val id = uuidPK()
    val name = varchar("name", 255)
    val description = text("description").nullable()
    val photo = text("photo").nullable()
}

object TechniqueDB: Table("techniques") {
    val id = uuidPK()
    val name = varchar("name", 255)
    val description = text("description").nullable()
    val video = text("video").nullable()
}

object RecipeWithIngredients: Table("recipes_ingredients") {
    val recipeId = uuid("recipe_id").references(RecipeDB.id).primaryKey(0)
    val ingredientId = uuid("ingredient_id").references(IngredientDB.id).primaryKey(1)
    val quantity = varchar("quantity", 255).nullable()
    val unit = varchar("unit", 255).nullable()
    val preparation = text("preparation").nullable()
}

object RecipeWithTools: Table("recipes_tools") {
    val recipeId = uuid("recipe_id").references(RecipeDB.id).primaryKey(0)
    val toolId = uuid("ingredient_id").references(ToolDB.id).primaryKey(1)
}

object RecipeWithTechniques: Table("recipes_techniques") {
    val recipeId = uuid("recipe_id").references(RecipeDB.id).primaryKey(0)
    val techniqueId = uuid("ingredient_id").references(TechniqueDB.id).primaryKey(1)
}

object RecipeEntry: Table("recipe_entry") {
    val id = uuidPK()
    val recipeId = uuid("recipe_id").references(RecipeDB.id)
    val description = text("description").nullable()
}

object RecipeStep: Table("recipe_step") {
    val id = uuidPK()
    val recipeEntryId = uuid("recipe_entry_id").references(RecipeEntry.id)
    val action = varchar("action", 255)
    val ingredient = varchar("ingredient", 255).nullable()
    val portion = varchar("portion", 255).nullable()
    val description = varchar("description", 255).nullable()
    val time = varchar("time", 255).nullable()
    val note = varchar("note", 255).nullable()
}

object Schema {
    fun createSchema(config: DatabaseConfiguration) {
        val host = config.host ?: "localhost"
        val port = config.port ?: 5432
        val database = config.database ?: "postgres"
        val jdbcUrl = "jdbc:postgresql://${host}:${port}/${database}"

        Database.connect(jdbcUrl, driver = "org.postgresql.Driver", user = config.user ?: "", password = config.password ?: "")
        transaction {
            addLogger(StdOutSqlLogger)
            SchemaUtils.createMissingTablesAndColumns (
                RecipeDB,
                IngredientDB,
                ToolDB,
                TechniqueDB,
                RecipeWithIngredients,
                RecipeWithTools,
                RecipeWithTechniques,
                RecipeEntry,
                RecipeStep
            )
        }
    }

}
