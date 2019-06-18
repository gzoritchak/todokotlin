package io.data2viz.todo

import com.fasterxml.jackson.databind.SerializationFeature
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

private fun HTML.pageBody() {
    body {
        todoApp()
        bindJs()
    }
}

private fun BODY.todoApp() {
    section("todoapp") {
        todoHeader()
        todoMain()
        todoFooter()
    }
}

private fun SECTION.todoHeader() {
    header("header") {
        h1 { +"todos" }
        input(classes = "new-todo") {
            placeholder = "What needs to be done?"
        }
    }
}


private fun SECTION.todoMain() {
    section("main") {
        style = "display: block;"
        input(classes = "toggle-all") {
            id = "toggle-all"
            type = InputType.checkBox
        }
        label {
            attributes["for"] = "toggle-all"
            +"Mark all as complete"
        }
        ul("todo-list") {
            li {
                classes = setOf("completed")
                div("view") {
                    input(classes = "toggle") {
                        type = InputType.checkBox
                        checked = true
                    }
                    label { +"narusite" }
                    button {
                        classes = setOf("destroy")
                    }
                }
                input(classes = "edit") {
                    value = "narusite"
                }
            }
            li {
                div {
                    classes = setOf("view")
                    input(classes = "toggle") {
                        type = InputType.checkBox
                    }
                    label { +"nrausite au aunr" }
                    button(classes = "destroy") {}
                }
                input(classes = "edit") {
                    value = "nrausite au aunr"
                }
            }
        }
    }
}

private fun SECTION.todoFooter() {
    footer {
        classes = setOf("footer")
        style = "display: block;"
        span {
            classes = setOf("todo-count")
            strong { +"1" }
            +"item left"
        }
        ul {
            classes = setOf("filters")
            li {
                a(classes = "selected") {
                    href = "#/"
                    +"All"
                }
            }
            li {
                a {
                    href = "#/active"
                    +"Active"
                }
            }
            li {
                a {
                    href = "#/completed"
                    +"Completed"
                }
            }
        }
        button(classes = "clear-completed") {
            +"Clear completed"
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


fun FlowContent.bindJs() {
    val parentPackage = "js.io.data2viz.todo"

    script {
        +"require(['/js.js'], function(js) { console.log($parentPackage); });\n"
    }
}
