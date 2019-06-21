package io.data2viz.todo.fwk


interface StateListener<in S> {
	fun applyState(state: S)
}

abstract class Store<State, Action>(
	initialState: State,
	val middlewares:List<Middleware<State, Action>> = listOf()) {

	abstract fun reducer(state: State, action: Action): State

	private var currentState: State = initialState
	private val listeners = mutableListOf<StateListener<State>>()

	val state: State
		get() = currentState

	fun dispatch(action: Action) {
		val newAction = applyMiddleWare(action)
		currentState = reducer(currentState, newAction)
		listeners.forEach { listener ->
			listener.applyState(state)
		}
	}

	fun subscribe(listener: StateListener<State>): () -> Unit {
		listeners.add(listener)
		return { listeners.remove(listener) }
	}

	private fun applyMiddleWare(action: Action): Action {
		val chain = createNext(0)
		return chain.next(this, action)
	}

	private fun createNext(index: Int):Next<State, Action> {
		if (index == middlewares.size)
			return EndOfChain()
		return NextMiddleware(middlewares[index], createNext(index + 1))
	}
}


interface Middleware<State, Action> {
	fun applyMiddleware(store: Store<State,Action>, action: Action, next: Next<State,Action>): Action
}

interface Next<State, Action> {
	fun next(store: Store<State,Action>, action: Action): Action
}

class NextMiddleware<State, Action>(
	val middleware: Middleware<State, Action>,
	val next: Next<State, Action>): Next<State, Action> {

	override fun next(store: Store<State, Action>, action: Action): Action {
		return middleware.applyMiddleware(store, action, next)
	}
}

class EndOfChain<State,Action>: Next<State, Action> {

	override fun next(store: Store<State, Action>, action: Action): Action {
		return action
	}
}


/**
 * A [Subscriber] that notifies the [PartialSubscriber] if the [SubState] sub-state changed.
 */
class PartialSubscriber<in State, SubState>(private val subStateChangeListener: SubStateChangeListener<State, SubState>)
	: StateListener<State> {

	private var previousValue: SubState? = null

	override fun applyState(state: State) {
		val subState = subStateChangeListener.getSubState(state)
		if (previousValue == null || subState != previousValue) {
			subStateChangeListener.onSubStateChanged(subState)
			previousValue = subState
		}
	}

	interface SubStateChangeListener<in State, SubState> {

		/**
		 * Specify the sub-state you want to listen to.
		 */
		fun getSubState(state: State): SubState

		/**
		 * @param <T> The new sub-state
		 */
		fun onSubStateChanged(subState: SubState)
	}
}