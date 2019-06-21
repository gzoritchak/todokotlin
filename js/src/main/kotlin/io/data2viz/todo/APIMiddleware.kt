package io.data2viz.todo

import io.data2viz.play.todo.TodoAppState
import io.data2viz.todo.fwk.Middleware
import io.data2viz.todo.fwk.Next
import io.data2viz.todo.fwk.Store
import io.ktor.client.HttpClient
import io.ktor.client.features.defaultRequest
import io.ktor.client.request.delete
import io.ktor.client.request.host
import io.ktor.client.request.port
import io.ktor.client.response.HttpResponse
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class APIMiddleware : Middleware<TodoAppState, Action> {

    private val client = HttpClient {
        defaultRequest {
            host = "127.0.0.1"
            port = 8080
        }
    }

    override fun applyMiddleware(
        store: Store<TodoAppState, Action>,
        action: Action,
        next: Next<TodoAppState, Action>
    ): Action {

        return when (action){
            is ActionRemoveTodo -> {
                GlobalScope.launch {
                    val response: HttpResponse = client.delete("todo/${action.toDo.UUID}")
                    if (response.status != HttpStatusCode.OK) {
                        store.dispatch(
                            ActionAddError(
                                "Failure during remote execution. You should probably reload the page"))
                    }
                }
                next.next(store, action)
            }
            is ActionAddTodo -> next.next(store, action)
            is ActionCompleteTodo -> next.next(store, action)
            is ActionClearCompleted -> next.next(store, action)
            else -> next.next(store, action)
        }
    }

}