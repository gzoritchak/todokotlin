package io.data2viz.todo

import io.data2viz.play.todo.*
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.StatusPages
import io.ktor.html.respondHtml
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.files
import io.ktor.http.content.static
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.sessions.*
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.pipeline.PipelineContext
import kotlinx.html.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import org.slf4j.event.Level
import java.util.*


@KtorExperimentalAPI
fun main() {
    println("Starting server")
    embeddedServer(Netty, port = 8080, host = "127.0.0.1") {
        mainModule()
    }.start(wait = true)
}

var deleteCount = 0

@KtorExperimentalAPI
@Suppress("unused") //used by application.conf
fun Application.mainModule() {
    install(CallLogging) {
        level = Level.INFO
    }
    install(Sessions) {
        cookie<UserSession>("TodoAppState", SessionStorageMemory())
    }
    install(StatusPages)
    routing {

        get("/") {
            val userSession = call.sessions.getOrSet { TodoAppState(
                listOf(
                    ToDo("Share views", true, uuid()),
                    ToDo("Share state between server and client", true, uuid()),
                    ToDo("Action with remote call", false, uuid())
                )
            ).toSession() }

            call.respondHtml {
                pageHead()
                pageBody(userSession.toTodoAppState())
            }
        }

        route("todo") {
            post {
                val text = call.receive<String>()
                val newTodo = ToDo(text, false, uuid())
                val todoAppState = userTodos()
                val newTodos  = todoAppState.todos + newTodo
                val newState = todoAppState.copy(todos = newTodos)
                call.sessions.set(newState.toSession())
                call.respond(HttpStatusCode.OK, newState.toJson())
            }
            get("/{uuid}/complete"){
                val todoAppState = userTodos()
                val todo = todoAppState.todos.first { it.UUID == call.parameters["uuid"] }
                val newTodo = todo.copy(completed = !todo.completed)
                val todoIndex = todoAppState.todos.indexOf(todo)
                val newtodos = todoAppState.todos
                    .toMutableList()
                    .apply { set(todoIndex, newTodo) }
                    .toList()
                val newState = todoAppState.copy(todos = newtodos)
                call.sessions.set(newState.toSession())
                call.respond(HttpStatusCode.OK, "completed")
            }
            delete("/{uuid}"){
                deleteCount++
                if (deleteCount % 2 == 0) { //simulates an error every two calls.
                    call.respond(HttpStatusCode.InternalServerError, "Error")
                } else {
                    val todoAppState = userTodos()
                    val todo = todoAppState.todos.first { it.UUID == call.parameters["uuid"] }
                    val newTodos = todoAppState.todos - todo
                    val newState = todoAppState.copy(todos = newTodos)
                    call.sessions.set(newState.toSession())
                    call.respond(HttpStatusCode.OK, "deleted")
                }
            }
        }
        static("/") {
            files("data/public")
        }
    }

}

private fun PipelineContext<Unit, ApplicationCall>.userTodos(): TodoAppState =
    call.sessions.get<UserSession>()!!.toTodoAppState()

private fun uuid() = UUID.randomUUID().toString()

private fun HTML.pageBody(todoAppState: TodoAppState) {
    body {
        section("todoapp") {
            todoHeader(todoAppState)
            todoMain(todoAppState)
            todoFooter(todoAppState)
        }
        div("messages"){
            messages(todoAppState.messages)
        }
        val jsonData = todoAppState.toJson()

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

private fun TodoAppState.toJson(): String =
    Json(JsonConfiguration.Stable)
        .stringify(TodoAppState.serializer(), this)

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


data class UserSession(val jsonAppState: String) {
    fun toTodoAppState(): TodoAppState = Json.parse(TodoAppState.serializer(), jsonAppState)
}

fun TodoAppState.toSession(): UserSession = UserSession(toJson())

