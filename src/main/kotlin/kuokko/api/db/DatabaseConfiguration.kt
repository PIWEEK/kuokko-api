package kuokko.api.db

import io.micronaut.context.annotation.ConfigurationProperties

@ConfigurationProperties("database")
class DatabaseConfiguration {
    var host: String? = null
    var port: Int? = null
    var database: String? = null
    var user: String? = null
    var password: String? = null

    override fun toString() = """
    Database Config
    ---------------
     - host: ${this.host}
     - port: ${this.port}
     - database: ${this.database}
     - user: ${this.user}
     - password: ${this.password?.map { '*' }?.joinToString("")}
    """.trimIndent()

}
