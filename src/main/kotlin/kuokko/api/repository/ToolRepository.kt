package kuokko.api.repository

import kuokko.api.model.Tool

interface ToolRepository {
    fun search(name: String): List<Tool>
    fun get(id: String): Tool?
    fun insert(tool: Tool): Tool
}
