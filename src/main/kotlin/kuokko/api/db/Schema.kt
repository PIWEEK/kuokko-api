package kuokko.api.db

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.dao.UUIDTable
import java.util.UUID

fun Table.uuidPK() = uuid("id").primaryKey().clientDefault { UUID.randomUUID() }

object RecipeDB: UUIDTable("recipes") {
    val title = text("name").uniqueIndex()
    val author = text("author").nullable()
    val cookTime = varchar("cook_time", 255).nullable()
    val preparationTime = varchar("preparation_time", 255).nullable()
    val totalTime = varchar("total_time", 255).nullable()
    val dificulty = varchar("dificulty", 255).nullable()
    val language = varchar("language", 255).nullable()
    val photoUrl = text("photo_url").nullable()
    val recipeUrl = text("recipe_url").nullable()
}

object IngredientDB: UUIDTable("ingredients") {
    val name = varchar("name", 255).uniqueIndex()
}

object ToolDB: UUIDTable("tools") {
    val name = varchar("name", 255).uniqueIndex()
    val description = text("description").nullable()
    val photo = text("photo").nullable()
}

object TechniqueDB: UUIDTable("techniques") {
    val name = varchar("name", 255).uniqueIndex()
    val description = text("description").nullable()
    val video = text("video").nullable()
}

object RecipeWithIngredients: Table("recipes_ingredients") {
    val recipeId = reference("recipe_id", RecipeDB).primaryKey(0)
    val ingredientId = reference("ingredient_id", IngredientDB).primaryKey(1)
    val quantity = varchar("quantity", 255).nullable()
    val unit = varchar("unit", 255).nullable()
    val preparation = text("preparation").nullable()
}

object RecipeWithTools: Table("recipes_tools") {
    val recipeId = reference("recipe_id", RecipeDB).primaryKey(0)
    val toolId = reference("tool_id", ToolDB).primaryKey(1)
}

object RecipeWithTechniques: Table("recipes_techniques") {
    val recipeId = reference("recipe_id", RecipeDB).primaryKey(0)
    val techniqueId = reference("technique_id", TechniqueDB).primaryKey(1)
}

object RecipeEntry: UUIDTable("recipe_entry") {
    val recipeId = reference("recipe_id", RecipeDB)
    val description = text("description").nullable()
}

object RecipeStep: UUIDTable("recipe_step") {
    val recipeEntryId = reference("recipe_entry_id", RecipeEntry)
    val action = varchar("action", 255)
    val ingredient = reference("ingredient_id", IngredientDB).nullable()
    val technique = reference("technique_id", TechniqueDB).nullable()
    val portion = float("portion").nullable()
    val description = varchar("description", 255).nullable()
    val time = varchar("time", 255).nullable()
    val note = varchar("note", 255).nullable()
    val order = integer("order")
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
            SchemaUtils.drop (
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
