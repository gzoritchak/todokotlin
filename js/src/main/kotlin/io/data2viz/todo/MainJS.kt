package io.data2viz.todo

import io.data2viz.play.todo.*
import io.data2viz.todo.fwk.*
import kotlinx.html.js.div
import kotlinx.html.js.section
import org.w3c.dom.*
import kotlin.browser.document

fun main() {
    println("starting client app")
    TodoApp.init()
    MessageApp.init()

}

const val ENTER_KEY_CODE = 13


object MessageApp: PartialSubscriber.SubStateChangeListener<TodoAppState, List<String>> {

    private val container: Element = document.querySelector(".messages")!!

    override fun getSubState(state: TodoAppState): List<String>  = state.messages

    override fun onSubStateChanged(messages: List<String>) {
        container.removeChildren()
        container.appendChild(render.div { messages(messages)}.uniqueChild)
    }

    fun init() {
        TodoAppStore.subscribe(PartialSubscriber(this))
    }

}

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

    private fun bindEvents(state: TodoAppState) {
        val newTodo = container.querySelector("input.new-todo") as HTMLInputElement

        fun addNewTodoOnEnter() {
            newTodo.onkeypress = { evt ->
                if (evt.keyCode == ENTER_KEY_CODE && newTodo.value.trim().isNotEmpty()) {
                    TodoAppStore.dispatch(ActionAddTodo(newTodo.value.trim()))
                }
            }
        }

        fun clearCompletedClicked() {
            val clearCompleted = container.querySelector("button.clear-completed") as HTMLButtonElement
            clearCompleted.onclick = { TodoAppStore.dispatch(ActionClearCompleted) }
        }

        fun completeTodoClicked() {
            addEvents("input.completeTodo", state.filteredTodos) { todo ->
                onclick = { TodoAppStore.dispatch(ActionCompleteTodo(todo)) }
            }
        }

        fun deleteTodoClicked() {
            addEvents("button.deleteTodo", state.filteredTodos) { todo ->
                onclick = { TodoAppStore.dispatch(ActionRemoveTodo(todo)) }
            }
        }

        fun selectVisibility() {
            val visibilityLinks = container.querySelectorAll("a.visibility-filter").asList()
            visibilityLinks.forEach {
                val link = it as HTMLElement
                link.onclick = {
                    val filter = VisibilityFilter.valueOf(link.id)
                    TodoAppStore.dispatch(ActionSetVisibilityFilter(filter))
                }
            }
        }

        addNewTodoOnEnter()
        clearCompletedClicked()
        completeTodoClicked()
        deleteTodoClicked()
        selectVisibility()
        newTodo.focus()
    }

}
