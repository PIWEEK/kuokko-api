package kuokko.api.repository.mock

import javax.inject.Singleton
import kuokko.api.model.Tool
import kuokko.api.repository.database.DatabaseToolRepository
import kuokko.api.repository.ToolRepository
import io.micronaut.context.annotation.Replaces

@Singleton
@Replaces(DatabaseToolRepository::class)
class MockToolRepository: ToolRepository {
    override fun search(name: String) = (1 .. 10).map { Tool("$it", "Tool $it") }
    override fun get(id: String) = Tool(id, "Tool $id")
    override fun insert(tool: Tool) = tool.copy(id = "1")
}

