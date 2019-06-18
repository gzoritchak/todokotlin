package io.data2viz.todo



interface StateListener<in S> {
	fun applyState(state: S)
}


abstract class Store<State, in Action>(initialState: State) {

	abstract fun reducer(state: State, action: Action): State

	private var currentState: State = initialState
	private val listeners = mutableListOf<StateListener<State>>()

	val state: State
		get() = currentState

	fun dispatch(action: Action) {
		currentState = reducer(currentState, action)
		listeners.forEach { listener ->
			listener.applyState(state)
		}
	}

	fun subscribe(listener: StateListener<State>): () -> Unit {
		listeners.add(listener)
		return { listeners.remove(listener) }
	}
}
