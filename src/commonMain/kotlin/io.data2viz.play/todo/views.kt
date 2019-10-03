package io.data2viz.play.todo

import kotlinx.html.*


fun SECTION.todoHeader(state: TodoAppState) {
    header("header") {
        h1 { +"todos" }
        input(classes = "new-todo") {
            placeholder = "What needs to be done?"
            value = state.newTodo
        }
    }
}

fun SECTION.todoMain(state: TodoAppState) {
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
            state.filteredTodos
                .forEach { toDo -> todo(toDo)
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


fun SECTION.todoFooter(state: TodoAppState) {
    val itemLeft = state.todos.count { !it.completed }
    val labelItemLeft = if (itemLeft > 1) " items left" else " item left"

    fun linkClasses(visibilityFilter: VisibilityFilter) =
        "visibility-filter" + if(state.visibilityFilter == visibilityFilter)
            " selected" else ""

    footer("footer") {
        style = "display: block;"
        span("todo-count") {
            strong { + "$itemLeft" }
            + labelItemLeft
        }
        ul("filters")  {
            li {
                a(classes = linkClasses(VisibilityFilter.ALL)) {
                    id = VisibilityFilter.ALL.name
                    +"All"
                }
            }
            li {
                a(classes = linkClasses(VisibilityFilter.ACTIVE)) {
                    id = VisibilityFilter.ACTIVE.name
                    +"Active"
                }
            }
            li {
                a(classes = linkClasses(VisibilityFilter.COMPLETED)) {
                    id = VisibilityFilter.COMPLETED.name
                    +"Completed"
                }
            }
        }
        button(classes = "clear-completed") {
            +"Clear completed"
        }
    }
}


fun DIV.messages(messages:List<String>){
    div { //we need a unique child
        messages.forEach {
            div("message") {
                + it
            }
        }
    }
}