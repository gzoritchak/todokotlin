package io.data2viz.play.web.model

import kotlinx.serialization.Serializable


@Serializable
data class ToDo(
    val text: String,
    val completed: Boolean = false
)

enum class VisibilityFilter {
    SHOW_COMPLETED,
    SHOW_ALL
}

@Serializable
data class State(
    val todos: List<ToDo> = listOf(),
    val visibilityFilter: VisibilityFilter = VisibilityFilter.SHOW_ALL
)

