package io.data2viz.todo

import com.fasterxml.jackson.databind.SerializationFeature
import io.data2viz.play.web.model.State
import io.data2viz.play.web.model.ToDo
import io.data2viz.play.web.views.todoFooter
import io.data2viz.play.web.views.todoHeader
import io.data2viz.play.web.views.todoMain
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.StatusPages
import io.ktor.html.respondHtml
import io.ktor.http.content.files
import io.ktor.http.content.static
import io.ktor.jackson.jackson
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.sessions.Sessions
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
    install(Sessions)
    install(ContentNegotiation) {
        jackson {
            configure(SerializationFeature.INDENT_OUTPUT, true)
        }
    }
    routing {

        get("/") {
            call.respondHtml {
                pageHead()
                pageBody()
            }
        }
        static("/") {
            files("public")
        }
    }

}

val state = State(
    listOf(
        ToDo("Share views", true),
        ToDo("Set completed"),
        ToDo("Add todo"),
        ToDo("Share state between server and client")
    )
)


private fun HTML.pageBody() {
    body {
        section("todoapp") {
            todoHeader(state)
            todoMain(state)
            todoFooter(state)
        }
        includeJs()

        val json = Json(JsonConfiguration.Stable)
        val jsonData = json.stringify(State.serializer(), state).replace("<", "\\\\u003c")

        script {
            unsafe {
                //language=JavaScript
                +"""
                // BODY.viewSharedState
                var viewSharedState = '$jsonData'
            """
            }
        }

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
            rel = "shortcut icon"
            href = "favicon.ico"
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
            +"require.config({baseUrl: '/'});\n"
        }

    }
}


fun FlowContent.includeJs() {
    val parentPackage = "js.io.data2viz.todo"

    script {
        +"require(['/js.js'], function(js) { console.log($parentPackage); });\n"
    }
}
