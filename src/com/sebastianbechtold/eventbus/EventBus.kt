

package com.sebastianbechtold.eventbus

import java.util.*
import java.util.logging.Handler
import kotlin.Comparator
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

var deb = EventBus()

class EventBus {

	inner class HandlerEntry<T>(val handler : (T) -> Unit, val priority : Int = 0) {
	}

	var _eventHandlers: HashMap<Any, Any> = HashMap()
	var _eventHandlersToRun : HashMap<Any, Any> = HashMap()


	inline fun <reified T> addHandler(noinline listener: (T) -> Unit, priority : Int = 0) {

		var handlers = _eventHandlers.get(T::class);

		if (handlers == null) {
			handlers = ArrayList<HandlerEntry<T>>()
			_eventHandlers.put(T::class, handlers)
		}
		else {

			// Check if handler is already registered. If yes, do nothing:
			for(handlerEntry in handlers as ArrayList<HandlerEntry<T>>) {
				if (handlerEntry.handler == listener) {
					return
				}
			}
		}

		// Otherwise, add handler...
		handlers.add(HandlerEntry(listener, priority))

		// ... and reorder the list of handlers by their priorities:
		Collections.sort(handlers as ArrayList<HandlerEntry<T>>, object : Comparator<HandlerEntry<T>> {
			override fun compare(p0: HandlerEntry<T>, p1: HandlerEntry<T>): Int {

				return p0.priority - p1.priority
				/*
				if (p0.priority > p1.priority) {
					return -1
				}
				else if (p0.priority < p1.priority) {
					return 1
				}

				return 0
				 */
			}
		})

		// Clone handlers list for actual use to avoid ConcurrentModificationExceptions:
		cloneHandlersList()
	}


	fun cloneHandlersList() {

		// NOTE: We create a copy of the handlers list here.
		// In the fire() method, we use that copy to fire the events in
		// order to avoid ConcurrentModificationExceptions
		// if the code which is executed by a handler adds or removes event
		// handlers from this event bus.

		for(key in _eventHandlers.keys) {

			var originalList = _eventHandlers.get(key)!! as ArrayList<HandlerEntry<Any>>

			_eventHandlersToRun.put(key, originalList.clone() as ArrayList<HandlerEntry<Any>>)
		}
	}


	inline fun <reified T> removeHandler(noinline listener: (T) -> Unit) {

		var handlers = _eventHandlers.get(T::class);

		if (handlers == null) {
			return;
		}

		var index = -1

		for(handlerEntry in (handlers as ArrayList<HandlerEntry<T>>).withIndex()) {

			if (handlerEntry.value.handler == listener) {
				index = handlerEntry.index
				break
			}
		}

		if (index >= 0) {
			handlers.removeAt(index)
		}

		// Clone handlers list for actual use to avoid ConcurrentModificationExceptions:
		cloneHandlersList()
	}


	inline fun <reified T> fire(event: T) {

		// NOTE how we use the cloned handlers list here instead of the original one in order
		// to avoid ConcurrentModificationExceptions:
		var handlers = _eventHandlersToRun.get(T::class) as ArrayList<HandlerEntry<T>>?

		if (handlers == null) {
			return;
		}

		for(handlerEntry in handlers) {
			handlerEntry.handler(event)
		}
	}
}
