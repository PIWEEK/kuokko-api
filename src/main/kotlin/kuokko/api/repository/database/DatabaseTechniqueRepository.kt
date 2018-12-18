package kuokko.api.repository.database

import java.util.UUID
import javax.inject.Singleton
import kuokko.api.model.Technique
import kuokko.api.repository.TechniqueRepository

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

import kuokko.api.db.TechniqueDB

@Singleton
class DatabaseTechniqueRepository: TechniqueRepository {
    fun hydrate(row: ResultRow) = Technique(
        id = row[TechniqueDB.id].toString(),
        name = row[TechniqueDB.name]
    )

    fun byId(id: String): QueryOp = { TechniqueDB.id eq UUID.fromString(id) }
    fun byName(technique: Technique): QueryOp = { TechniqueDB.name eq technique.name }

    override fun search(name: String) = TechniqueDB.selectAll().map(::hydrate)

    override fun get(id: String) = TechniqueDB.select(byId(id)).firstOrNull()?.let(::hydrate)

    override fun insert(technique: Technique) = transaction {
        val previous = TechniqueDB.select(byName(technique)).firstOrNull()?.let(::hydrate)

        previous ?: TechniqueDB.insertAndGetId {
            it[name] = technique.name
        }.let {
            technique.copy(id = it.value.toString())
        }
    }
}

