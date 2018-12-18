package kuokko.api.repository

import kuokko.api.model.Technique

interface TechniqueRepository {
    fun search(name: String): List<Technique>
    fun get(id: String): Technique?
    fun insert(technique: Technique): Technique
}
