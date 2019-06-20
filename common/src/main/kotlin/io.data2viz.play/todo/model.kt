package io.data2viz.play.todo

import kotlinx.serialization.Serializable


@Serializable
data class TodoAppState(
    val todos: List<ToDo> = listOf(),
    val visibilityFilter: VisibilityFilter = VisibilityFilter.ALL,
    val newTodo: String = ""
)


@Serializable
data class ToDo(
    val text: String,
    val completed: Boolean = false
)

enum class VisibilityFilter {
    ALL, COMPLETED, ACTIVE
}


val TodoAppState.filteredTodos: List<ToDo>
    get() = todos.filter {
        when (visibilityFilter) {
            VisibilityFilter.COMPLETED -> it.completed
            VisibilityFilter.ACTIVE -> !it.completed
            VisibilityFilter.ALL -> true
        }
    }


