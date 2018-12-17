package kuokko.api

import io.micronaut.runtime.Micronaut
import io.swagger.v3.oas.annotations.*
import io.swagger.v3.oas.annotations.info.*

@OpenAPIDefinition(
    info = Info(
        title = "Kuokko Api",
        version = "0.1"
    )
)
object Application {
    @JvmStatic
    fun main(args: Array<String>) {
        Micronaut.build()
                .packages("kuokko.api")
                .mainClass(Application.javaClass)
                .start()
    }
}
