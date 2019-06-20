package io.data2viz.play.web.views

import io.data2viz.play.web.model.State
import io.data2viz.play.web.model.ToDo
import kotlinx.html.*


fun SECTION.todoHeader(state: State) {
    header("header") {
        h1 { +"todos" }
        input(classes = "new-todo") {
            placeholder = "What needs to be done?"
        }
    }
}

fun SECTION.todoMain(state: State) {
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
            state.todos.forEach { toDo ->
                todo(toDo)
            }
        }
    }
}

private fun UL.todo(toDo: ToDo) {
    val completedClass = if (toDo.completed) "completed" else ""
    li(completedClass) {
        div("view") {
            input(classes = "toggle completeTodo") {
                type = InputType.checkBox
                checked = toDo.completed
            }
            label { + toDo.text }
            button(classes = "destroy deleteTodo") {}
        }
        input(classes = "edit") {
            value = toDo.text
        }
    }
}


fun SECTION.todoFooter(state: State) {
    footer {
        classes = setOf("footer")
        style = "display: block;"
        span {
            classes = setOf("todo-count")
            strong { + "${state.todos.count { !it.completed }}" }
            +"item left"
        }
        ul  {
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
