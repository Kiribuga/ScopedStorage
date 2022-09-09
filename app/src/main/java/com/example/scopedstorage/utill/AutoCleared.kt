package com.example.scopedstorage.utill

import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.observe
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Creates a new instance of the [AutoClearedDelegate] associated with this
 * fragment.
 */
fun <T : Any> Fragment.autoCleared(): ReadWriteProperty<Fragment, T> = AutoClearedDelegate(this)

/**
 * Creates a new instance of the [AutoClearedDelegate] associated with this
 * fragment that uses the specified initialization function [initializer].
 */
fun <T : Any> Fragment.autoCleared(
    initializer: (() -> T)
): ReadOnlyProperty<Fragment, T> = AutoClearedDelegate(this, initializer)

/**
 * A lazy property that gets cleaned up when the fragment's view is destroyed.
 *
 * Accessing this variable while the fragment's view is destroyed will throw [IllegalStateException].
 */
private class AutoClearedDelegate<T : Any>(
    private val fragment: Fragment,
    private  val initializer: (() -> T)? = null
) : ReadWriteProperty<Fragment, T>, ReadOnlyProperty<Fragment, T> {

    private var _value: T? = null

    private val fragmentLifecycleObserver = object : DefaultLifecycleObserver {
        override fun onCreate(owner: LifecycleOwner) {
            fragment.viewLifecycleOwnerLiveData.observe(fragment) { viewLifecycleOwner ->
                viewLifecycleOwner?.lifecycle?.addObserver(viewLifecycleObserver)
            }
        }
    }

    private val viewLifecycleObserver = object : DefaultLifecycleObserver {
        override fun onDestroy(owner: LifecycleOwner) {
            _value = null
        }
    }

    init {
        fragment.lifecycle.addObserver(fragmentLifecycleObserver)
    }

    override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
        checkNotNull(thisRef.view) {
            "Property '${property.name}' of fragment '${thisRef.javaClass.name}': view should be created before get."
        }
        if (_value == null && initializer != null) {
            _value = initializer.invoke()
        }
        return checkNotNull(_value) {
            "Property '${property.name}' of fragment '${thisRef.javaClass.name}': should be initialized before get."
        }
    }

    override fun setValue(thisRef: Fragment, property: KProperty<*>, value: T) {
        _value = value
    }
}