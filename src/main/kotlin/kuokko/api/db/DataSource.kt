package kuokko.api.db

import javax.annotation.PostConstruct
import io.micronaut.context.annotation.Context

@Context
class DataSource(
    val configuration: DatabaseConfiguration
) {
    init {
        println("${this.configuration}")
        Schema.createSchema(this.configuration)
    }
}
