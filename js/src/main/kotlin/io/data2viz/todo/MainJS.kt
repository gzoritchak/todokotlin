package io.data2viz.todo

import io.data2viz.play.todo.*
import kotlinx.html.js.section
import org.w3c.dom.*
import kotlin.browser.document


fun main() {
    println("starting client app")
    TodoApp.init()
}

const val ENTER_KEY_CODE = 13

object TodoApp: StateListener<TodoAppState> {

    private val container: Element = document.querySelector(".todoapp")!!

    fun init() {
        TodoAppStore.subscribe(this)
        bindEvents(TodoAppStore.state)
    }

    override fun applyState(state: TodoAppState) {
        renderFromState(state)
    }

    private fun renderFromState(state: TodoAppState) {
        container.removeChildren()
        container.appendChild(render.section { todoHeader(state)}.uniqueChild)
        container.appendChild(render.section { todoMain(state)}.uniqueChild)
        container.appendChild(render.section { todoFooter(state)}.uniqueChild)
        bindEvents(state)
    }

    fun bindEvents(state: TodoAppState) {
        val newTodo = container.querySelector("input.new-todo") as HTMLInputElement
        newTodo.onkeypress = { evt ->
            if (evt.keyCode == ENTER_KEY_CODE && newTodo.value.trim().isNotEmpty()) {
                TodoAppStore.dispatch(ActionAddTodo(newTodo.value.trim()))
            }
        }

        val clearCompleted = container.querySelector("button.clear-completed") as HTMLButtonElement
        clearCompleted.onclick = { TodoAppStore.dispatch(ActionClearCompleted)}

        addEvents("input.completeTodo", state.filteredTodos) { todo ->
            onclick = { TodoAppStore.dispatch(ActionCompleteTodo(todo)) }
        }
        addEvents("button.deleteTodo", state.filteredTodos) { todo ->
            onclick = { TodoAppStore.dispatch(ActionRemoveTodo(todo)) }
        }

        val visibilityLinks = container.querySelectorAll("a.visibility-filter").asList()
        visibilityLinks.forEach {
            console.log(it)
            val link = it as HTMLElement
            link.onclick = {
                val filter = VisibilityFilter.valueOf(link.id)
                println("$filter clicked")
                TodoAppStore.dispatch(ActionSetVisibilityFilter(filter))
            }
        }

        newTodo.focus()
    }

}
