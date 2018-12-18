package kuokko.api.repository.database

import java.util.UUID
import javax.inject.Singleton
import kuokko.api.model.Ingredient
import kuokko.api.repository.IngredientRepository

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

import kuokko.api.db.IngredientDB

typealias QueryOp = SqlExpressionBuilder.()->Op<Boolean>

@Singleton
class DatabaseIngredientRepository: IngredientRepository {
    fun hydrate(row: ResultRow) = Ingredient(
        id = row[IngredientDB.id].toString(),
        name = row[IngredientDB.name]
    )

    fun byId(id: String): QueryOp = { IngredientDB.id eq UUID.fromString(id) }
    fun byName(ingredient: Ingredient): QueryOp = { IngredientDB.name eq ingredient.name }

    override fun search(name: String) = IngredientDB.selectAll().map(::hydrate)

    override fun get(id: String) = IngredientDB.select(byId(id)).firstOrNull()?.let(::hydrate)

    override fun insert(ingredient: Ingredient) = transaction {
        val previous = IngredientDB.select(byName(ingredient)).firstOrNull()?.let(::hydrate)

        previous ?: IngredientDB.insertAndGetId {
            it[name] = ingredient.name
        }.let {
            ingredient.copy(id = it.value.toString())
        }
    }
}

