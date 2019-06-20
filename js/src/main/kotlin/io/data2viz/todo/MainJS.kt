package io.data2viz.todo

import io.data2viz.play.web.model.State
import io.data2viz.play.web.model.ToDo
import io.data2viz.play.web.views.todoFooter
import io.data2viz.play.web.views.todoHeader
import io.data2viz.play.web.views.todoMain
import kotlinx.coroutines.withTimeout
import kotlinx.html.TagConsumer
import kotlinx.html.dom.JSDOMBuilder
import kotlinx.html.dom.append
import kotlinx.html.dom.create
import kotlinx.html.js.section
import kotlinx.serialization.json.Json
import org.w3c.dom.*
import kotlin.browser.document

external val viewSharedState: String

fun main() {
    println("starting client app")
    val state = Json.parse(State.serializer(), viewSharedState)
    bindEvents(state)
    TodoApp.removeChildrenNode()
    state.todos = state.todos + ToDo("Client side")
    TodoApp.renderFromState(state)
}

data class CompleteTodoAction(val toDo: ToDo)

object TodoApp {
    val container = document.querySelector(".todoapp")!!

    fun removeChildrenNode() {
        while (container.firstElementChild != null) {
            container.removeChild(container.firstElementChild!!)
        }
    }

    fun renderFromState(state: State) {
        container.appendChild(new.section { todoHeader(state)}.uniqueChild)
        container.appendChild(new.section { todoMain(state)}.uniqueChild)
        container.appendChild(new.section { todoFooter(state)}.uniqueChild)
    }

}




fun bindEvents(state: State) {

    addEvents("input.completeTodo", state.todos) { todo ->
        onclick = {
            println("todo completed:: $todo")
            CompleteTodoAction(todo)
        }
    }
    addEvents("button.deleteTodo", state.todos) { todo ->
        onclick = {
            println("delete todo:: $todo")
            CompleteTodoAction(todo)
        }
    }

}

fun <D> addEvents(selector: String, data: List<D>, apply: HTMLElement.(D) -> Unit) {
    val elements = document.querySelectorAll(selector).asList()
    if (elements.size != data.size)
        error("There should be as much selected element as data")

    val zip = elements.zip(data)

    zip.forEach {
        val htmlElement = it.first as HTMLElement
        apply(htmlElement, it.second)
    }

}

private fun <T> String.selectElement(): T {
    return document.querySelector(".$this") as T
}

val new : TagConsumer<HTMLElement>
    get() = JSDOMBuilder(document)


private val HTMLElement.uniqueChild: Node
    get() {
        return childNodes[0]!!
    }
