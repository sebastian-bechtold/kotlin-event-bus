package com.sebastianbechtold.eventbus

import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class EventBus {

	inner class HandlerEntry<T>(val handler : (T) -> Unit, val priority : Int = 0) {}

	var _eventHandlers: HashMap<Pair<Any,Any>, Any> = HashMap()

	inline fun <reified T> addHandler(noinline listener: (T) -> Unit, source : Any = "all", priority : Int = 0) {

		var handlers = _eventHandlers.get(Pair(T::class, source)) as ArrayList<HandlerEntry<T>>?

		if (handlers == null) {
			handlers = ArrayList()
		}
		else {

			// Check if handler is already registered. If yes, do nothing:
			for(handlerEntry in handlers) {
				if (handlerEntry.handler == listener) {
					return
				}
			}
		}

		// ATTENTION: Here, we clone the handlers list to prevent a ConcurrentModificationException
		// that would otherwise happen if a handlers list is changed by one of the handlers in it:
		var handlersClone = handlers.clone() as ArrayList<HandlerEntry<T>>

		// Add the new handler:
		handlersClone.add(HandlerEntry(listener, priority))

		// ... and reorder the list of handlers by their priorities:
		Collections.sort(handlersClone, object : Comparator<HandlerEntry<T>> {
			override fun compare(p0: HandlerEntry<T>, p1: HandlerEntry<T>): Int {
				return p0.priority - p1.priority
			}
		})

		_eventHandlers.put(Pair(T::class, source), handlersClone)
	}


	inline fun <reified T> removeHandler(noinline listener: (T) -> Unit, source: Any = "all") {

		var handlers = _eventHandlers.get(Pair(T::class, source)) as ArrayList<HandlerEntry<T>>?

		if (handlers == null) {
			return;
		}

		// ATTENTION: Here, we clone the handlers list to prevent a ConcurrentModificationException
		// that would otherwise happen if a handlers list is changed by one of the handlers in it:
		var handlersClone = handlers.clone() as ArrayList<HandlerEntry<T>>

		var index = -1

		for(handlerEntry in handlersClone.withIndex()) {

			if (handlerEntry.value.handler == listener) {
				index = handlerEntry.index
				break
			}
		}

		if (index >= 0) {
			handlersClone.removeAt(index)
			_eventHandlers.put(Pair(T::class, source), handlersClone)
		}
	}


	inline fun <reified T> fire(event: T, source : Any? = null) {

		if (source != null) {

			var sourceHandlers = _eventHandlers.get(Pair(T::class, source)) as ArrayList<HandlerEntry<T>>?

			if (sourceHandlers != null) {

				for (handlerEntry in sourceHandlers) {
					handlerEntry.handler(event)
				}

			}
		}


		var allHandlers = _eventHandlers.get(Pair(T::class, "all")) as ArrayList<HandlerEntry<T>>?

		if (allHandlers != null) {

			for(handlerEntry in allHandlers) {
				handlerEntry.handler(event)
			}
		}




	}
}
