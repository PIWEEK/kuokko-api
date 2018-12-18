package kuokko.api.repository.database

import javax.inject.Singleton
import kuokko.api.model.Tool
import kuokko.api.repository.ToolRepository

@Singleton
class DatabaseToolRepository: ToolRepository {
    companion object {
        var counter: Int = 0
    }

    override fun search(name: String) = (1 .. 10).map { Tool("$it", "Tool $it") }
    override fun get(id: String) = Tool(id, "Tool $id")
    override fun insert(tool: Tool) = tool.copy(id = counter++.toString())
}

