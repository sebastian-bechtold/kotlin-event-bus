package com.sebastianbechtold.eventbus

var eb = EventBus()

class EventBus {

	var mListeners: HashMap<Any, Any> = HashMap()

	inline fun <reified T> addListener(noinline listener: (T) -> Unit) {

		var list = mListeners.get(T::class);

		if (list == null) {
			list = HashSet<(T) -> Unit>();
			mListeners.put(T::class, list)
		}

		(list as HashSet<(T) -> Unit>).add(listener)
	}

	inline fun <reified T> removeListener(noinline listener: (T) -> Unit) {

		var list = mListeners.get(T::class);

		if (list == null) {
			return;
		}

		(list as HashSet<(T) -> Unit>).remove(listener)
	}

	inline fun <reified T> fire(event: T) {
 
		var list = mListeners.get(T::class);

		if (list == null) {
			return;
		}

		for (listener in list as HashSet<(T) -> Unit>) {
			listener(event)
		}
	}
}
