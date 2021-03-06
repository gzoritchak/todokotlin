package io.data2viz.todo

import io.data2viz.play.todo.TodoAppState
import io.data2viz.todo.fwk.Middleware
import io.data2viz.todo.fwk.Next
import io.data2viz.todo.fwk.Store
import io.ktor.client.HttpClient
import io.ktor.client.call.receive
import io.ktor.client.features.cookies.HttpCookies
import io.ktor.client.features.defaultRequest
import io.ktor.client.request.*
import io.ktor.client.response.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLProtocol
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlin.browser.document
import kotlin.browser.window

class APIMiddleware : Middleware<TodoAppState, Action> {

	private val client = HttpClient {
		install(HttpCookies)
		defaultRequest {
			host = window.location.host
		}
	}


	override fun applyMiddleware(
        store: Store<TodoAppState, Action>,
        action: Action,
        next: Next<TodoAppState, Action>
    ): Action {

        return when (action) {
            is ActionRemoveTodo -> {
                GlobalScope.launch {
                    client.delete<HttpResponse>("todo/${action.toDo.UUID}")
                        .displayEventualRemoteCallError(store)
                }
                next.next(store, action)
            }
            is ActionAddTodo -> {
                GlobalScope.launch {
                    val response = client.post<HttpResponse>("todo/") {
                        body = action.text
                    }.displayEventualRemoteCallError(store)
                    val json = response.receive<String>()
                    val todos = Json.parse(TodoAppState.serializer(), json).todos
                    store.dispatch(ActionUpdateTodos(todos))
                }

                next.next(store, action)
            }
            is ActionCompleteTodo -> {
                GlobalScope.launch {
                    client.get<HttpResponse>("todo/${action.toDo.UUID}/complete")
                        .displayEventualRemoteCallError(store)
                }
                next.next(store, action)
            }
            is ActionClearCompleted -> {
                val completedTodos = store.state.todos
                    .filter { it.completed }
                if (completedTodos.isNotEmpty())
                    GlobalScope.launch {
                        val response = client.patch<HttpResponse>("todo/clearCompleted") {
                            body = completedTodos.mapNotNull { it.UUID }
                                    .joinToString("+")
                        }.displayEventualRemoteCallError(store)
                        val json = response.receive<String>()
                        val todos = Json.parse(TodoAppState.serializer(), json).todos
                        store.dispatch(ActionUpdateTodos(todos))
                    }
                next.next(store, action)

            }
            else -> next.next(store, action)
        }
    }

    private fun HttpResponse.displayEventualRemoteCallError(
        store: Store<TodoAppState, Action>
    ): HttpResponse {
        if (status != HttpStatusCode.OK) {
            store.dispatch(
                ActionAddError(
                    "Failure during remote execution. You should probably reload the page"
                )
            )
        }
        return this
    }

}
