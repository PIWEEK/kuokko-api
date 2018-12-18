package kuokko.api.repository.database

import java.util.UUID
import javax.inject.Singleton
import kuokko.api.model.Tool
import kuokko.api.repository.ToolRepository

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

import kuokko.api.db.ToolDB

@Singleton
class DatabaseToolRepository: ToolRepository {
    fun hydrate(row: ResultRow) = Tool(
        id = row[ToolDB.id].toString(),
        name = row[ToolDB.name]
    )

    fun byId(id: String): QueryOp = { ToolDB.id eq UUID.fromString(id) }
    fun byName(tool: Tool): QueryOp = { ToolDB.name eq tool.name }

    override fun search(name: String) = ToolDB.selectAll().map(::hydrate)

    override fun get(id: String) = ToolDB.select(byId(id)).firstOrNull()?.let(::hydrate)

    override fun insert(tool: Tool) = transaction {
        val previous = ToolDB.select(byName(tool)).firstOrNull()?.let(::hydrate)

        previous ?: ToolDB.insertAndGetId {
            it[name] = tool.name
        }.let {
            tool.copy(id = it.value.toString())
        }
    }
}

