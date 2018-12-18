package kuokko.api.repository.database

import javax.inject.Singleton
import kuokko.api.model.Technique
import kuokko.api.repository.TechniqueRepository

@Singleton
class DatabaseTechniqueRepository: TechniqueRepository {
    companion object {
        var counter: Int = 0
    }

    override fun search(name: String) = (1 .. 10).map { Technique("$it", "Technique $it") }
    override fun get(id: String) = Technique(id, "Technique $id")
    override fun insert(technique: Technique) = technique.copy(id = counter++.toString())
}

