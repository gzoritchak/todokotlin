package io.data2viz.todo

import io.data2viz.play.todo.ToDo
import io.data2viz.play.todo.TodoAppState
import io.data2viz.play.todo.VisibilityFilter
import io.data2viz.todo.fwk.Middleware
import io.data2viz.todo.fwk.Next
import io.data2viz.todo.fwk.Store
import kotlinx.serialization.json.Json

//Actions
sealed class Action

data class ActionAddTodo(val text: String) : Action()
data class ActionUpdateTodos(val todos: List<ToDo>) : Action()
data class ActionCompleteTodo(val toDo: ToDo) : Action()
data class ActionRemoveTodo(val toDo: ToDo) : Action()
data class ActionSetVisibilityFilter(val filter: VisibilityFilter) : Action()
data class ActionAddError(val message: String): Action()
data class ActionRemoveError(val index: Int): Action()
object ActionClearCompleted : Action()

/**
 * Server state serialized in the page
 */
external val viewSharedState: String

object TodoAppStore :
    Store<TodoAppState, Action>(
        Json.parse(TodoAppState.serializer(), viewSharedState),
        listOf(LogMiddleware(), APIMiddleware())
    ) {

    override fun reducer(state: TodoAppState, action: Action): TodoAppState {
        return  when (action) {
            is ActionAddTodo -> state.copy(todos = state.todos + ToDo(action.text))
            is ActionRemoveTodo -> state.copy(todos = state.todos - action.toDo)
            is ActionUpdateTodos -> state.copy(todos = action.todos)
            is ActionCompleteTodo -> {
                val newTodoState = action.toDo.copy(completed = !action.toDo.completed)
                val newTodos = state.todos.toMutableList()
                    .apply { set(state.todos.indexOf(action.toDo), newTodoState) }
                state.copy(todos = newTodos)
            }
            is ActionClearCompleted -> state.copy(todos = state.todos.filter { !it.completed })
            is ActionSetVisibilityFilter -> state.copy(visibilityFilter = action.filter)
            is ActionAddError -> state.copy(messages = state.messages + action.message)
            is ActionRemoveError -> state.copy(messages =
                state
                    .messages
                    .toMutableList().apply { removeAt(action.index) }
                    .toList()
            )
        }
    }

}

class LogMiddleware : Middleware<TodoAppState, Action> {

    override fun applyMiddleware(
        store: Store<TodoAppState, Action>,
        action: Action,
        next: Next<TodoAppState, Action>
    ): Action {
        console.log(action)
        return next.next(store, action)
    }

}

