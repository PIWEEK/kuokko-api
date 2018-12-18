package kuokko.api.repository.mock

import javax.inject.Singleton
import kuokko.api.model.Technique
import kuokko.api.repository.database.DatabaseTechniqueRepository
import kuokko.api.repository.TechniqueRepository
import io.micronaut.context.annotation.Replaces

@Singleton
@Replaces(DatabaseTechniqueRepository::class)
class MockTechniqueRepository: TechniqueRepository {
    override fun search(name: String) = (1 .. 10).map { Technique("$it", "Technique $it") }
    override fun get(id: String) = Technique(id, "Technique $id")
    override fun insert(technique: Technique) = technique.copy(id = "1")
}

