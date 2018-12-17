package kuokko.api

import io.micronaut.context.ApplicationContext
import io.micronaut.runtime.server.EmbeddedServer
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlin.test.*

object RecipeControllerSpec: Spek({
    describe("Recipe controller") {
        val server : EmbeddedServer = ApplicationContext.run(EmbeddedServer::class.java)
        val controller = server.applicationContext.getBean(RecipeController::class.java)

        it("List recipes") {
            val recipes = controller.listRecipes()
            assertEquals(10, recipes.size)
        }

        it("Retrieve recipes by id") {
            val recipe = controller.retrieveById("1")
            assertEquals("Recipe 1", recipe.title)
            assertEquals(0, recipe.tools.size)
        }

        afterGroup {
            server.close()
        }
    }
})
