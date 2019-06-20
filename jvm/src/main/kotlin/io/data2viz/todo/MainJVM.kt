package io.data2viz.todo

import io.data2viz.play.todo.ToDo
import io.data2viz.play.todo.TodoAppState
import io.data2viz.play.todo.todoFooter
import io.data2viz.play.todo.todoHeader
import io.data2viz.play.todo.todoMain
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.StatusPages
import io.ktor.html.respondHtml
import io.ktor.http.content.files
import io.ktor.http.content.static
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.util.KtorExperimentalAPI
import kotlinx.html.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import org.slf4j.event.Level


@KtorExperimentalAPI
fun main() {
    println("Starting server")
    embeddedServer(Netty, port = 8080, host = "127.0.0.1") {
        mainModule()
    }.start(wait = true)
}

@KtorExperimentalAPI
@Suppress("unused") //used by application.conf
fun Application.mainModule() {
    install(CallLogging) {
        level = Level.INFO
    }
    install(StatusPages)
    routing {

        get("/") {
            call.respondHtml {
                pageHead()
                pageBody()
            }
        }
        static("/") {
            files("data/public")
        }
    }

}

val serverState = TodoAppState(
    listOf(
        ToDo("Share views", true),
        ToDo("Share state between server and client", true),
        ToDo("Set completed", true),
        ToDo("Add todo", true),
        ToDo("Action with remote call")
    )
)

private fun HTML.pageBody() {
    body {
        section("todoapp") {
            todoHeader(serverState)
            todoMain(serverState)
            todoFooter(serverState)
        }
        val json = Json(JsonConfiguration.Stable)
        val jsonData = json.stringify(TodoAppState.serializer(), serverState)

        script {
            unsafe {
                //language=JavaScript
                +"""
                // BODY.viewSharedState
                var viewSharedState = '$jsonData'
            """
            }
        }
        includeJs()
    }
}

private fun HTML.pageHead() {
    head {
        meta {
            charset = "utf-8"
        }
        meta {
            name = "viewport"
            content = "width=device-width,initial-scale=1,shrink-to-fit=no"
        }
        meta {
            name = "theme-color"
            content = "#000000"
        }
        link {
            rel = "stylesheet"
            href = "index.css"
        }
        link {
            rel = "stylesheet"
            href = "app.css"
        }
        title { +"Kotlin Todo" }

        script(src = "/require.min.js") {}
        script {
            unsafe {
                +"require.config({baseUrl: '/'});\n"
            }
        }
    }
}

fun FlowContent.includeJs() {
    val parentPackage = "js.io.data2viz.todo"
    script {
        unsafe {
            +"require(['/js.js'], function(js) { console.log($parentPackage); });\n"
        }
    }
}
