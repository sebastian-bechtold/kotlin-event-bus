

package com.sebastianbechtold.eventbus

import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

var deb = EventBus()

class EventBus {

	inner class HandlerEntry<T>(val handler : (T) -> Unit, val priority : Int = 0) {
	}

	var _eventHandlers: HashMap<Any, Any> = HashMap()

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

		handlers.add(HandlerEntry(listener, priority))

		Collections.sort(handlers, object : Comparator<HandlerEntry<T>> {
			override fun compare(p0: HandlerEntry<T>, p1: HandlerEntry<T>): Int {

				if (p0.priority > p1.priority) {
					return -1
				}
				else if (p0.priority < p1.priority) {
					return 1
				}

				return 0
			}
		})
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
	}


	inline fun <reified T> fire(event: T) {
 
		var handlers = _eventHandlers.get(T::class);

		if (handlers == null) {
			return;
		}

		// NOTE: We create a copy of the handlers list here and use the copy to fire the events
		// in order to avoid ConcurrentModificationExceptions if the code which is executed by
		// a handlers adds or removes event handlers from this event bus.
		val handlersCopy = ArrayList<HandlerEntry<T>>()
		handlersCopy.addAll(handlers  as ArrayList<HandlerEntry<T>>)

		for(handlerEntry in handlersCopy) {
			handlerEntry.handler(event)
		}
	}
}
