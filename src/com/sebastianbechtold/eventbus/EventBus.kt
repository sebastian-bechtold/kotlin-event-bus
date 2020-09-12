package com.sebastianbechtold.eventbus

import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

// A default EventBus instance:
var deb = EventBus()

class EventBus {

	inner class HandlerEntry<T>(val handler : (T) -> Unit, val priority : Int = 0) {
	}

	var _eventHandlers: HashMap<Any, Any> = HashMap()

	inline fun <reified T> addHandler(noinline listener: (T) -> Unit, priority : Int = 0) {

		var handlers = _eventHandlers.get(T::class) as ArrayList<HandlerEntry<T>>?

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

		_eventHandlers.put(T::class, handlersClone)
	}


	inline fun <reified T> removeHandler(noinline listener: (T) -> Unit) {

		var handlers = _eventHandlers.get(T::class) as ArrayList<HandlerEntry<T>>?

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
			_eventHandlers.put(T::class, handlersClone)
		}
	}


	inline fun <reified T> fire(event: T) {

		var handlers = _eventHandlers.get(T::class) as ArrayList<HandlerEntry<T>>?

		if (handlers == null) {
			return;
		}

		for(handlerEntry in handlers) {
			handlerEntry.handler(event)
		}
	}
}
