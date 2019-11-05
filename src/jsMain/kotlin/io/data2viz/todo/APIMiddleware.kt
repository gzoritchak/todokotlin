package io.data2viz.todo

import axios.*
import io.data2viz.play.todo.TodoAppState
import io.data2viz.todo.fwk.Middleware
import io.data2viz.todo.fwk.Next
import io.data2viz.todo.fwk.Store
import kotlinx.serialization.json.Json

class APIMiddleware : Middleware<TodoAppState, Action> {

	init {
		//https://github.com/axios/axios/issues/907#issuecomment-373988087
	    Axios.defaults.transformResponse = undefined
	}

	override fun applyMiddleware(
		store: Store<TodoAppState, Action>,
		action: Action,
		next: Next<TodoAppState, Action>
	): Action {

		return when (action) {
			is ActionRemoveTodo -> {
				Axios.delete("todo/${action.toDo.UUID}")
					.catchAxiosError { err ->
						console.log("HTTP Status code :: ${err.response?.status}")
						remoteCallError(store)
					}
				next.next(store, action)
			}
			is ActionAddTodo -> {
				val config: dynamic = jsObject()
				val headers = jsObject()
				headers["Content-Type"] = "text/plain;charset=UTF-8"
				config.headers = headers
				Axios.post<String>("todo/", data = action.text, config = config)
					.then { e: AxiosResponse<String> ->
						val json = e.data
						val todos =	Json.parse(TodoAppState.serializer(), json).todos
						store.dispatch(ActionUpdateTodos(todos))
					}
					.catch { remoteCallError(store) }
				next.next(store, action)
			}
			is ActionCompleteTodo -> {
				Axios.get<String>("todo/${action.toDo.UUID}/complete")
					.catch { remoteCallError(store) }
				next.next(store, action)
			}
			is ActionClearCompleted -> {
				val completedTodos = store.state.todos
					.filter { it.completed }
				if (completedTodos.isNotEmpty())
					Axios.patch<String>("todo/clearCompleted", completedTodos.mapNotNull { it.UUID }.joinToString("+"))
						.then { response ->
							val json = response.data
							val todos = Json.parse(TodoAppState.serializer(), json).todos
							store.dispatch(ActionUpdateTodos(todos))
						}
						.catch { remoteCallError(store) }
				next.next(store, action)

			}
			else -> next.next(store, action)
		}
	}


	private fun remoteCallError(
		store: Store<TodoAppState, Action>,
		e: AxiosError? = null
	) {
		if (e != null) {
			console.log(e)
		}
		store.dispatch(
			ActionAddError(
				"Failure during remote execution. You should probably reload the page"
			)
		)
	}

	fun jsObject(init: (dynamic) -> Unit = {}): dynamic = js("{}")

}


