package sc.util

import javafx.beans.binding.Bindings
import javafx.beans.binding.BooleanBinding
import javafx.beans.binding.ObjectBinding
import javafx.beans.value.ObservableValue
import javafx.beans.value.WritableValue

fun WritableValue<Boolean>.toggle() {
    value = !value
}

fun <T> ObservableValue<T>.listenImmediately(listener: (newValue: T) -> Unit) {
    listener(this.value)
    addListener { _, _, new -> listener(new) }
}

fun <T> Array<ObservableValue<T>>.booleanBinding(listener: (values: List<T>) -> Boolean): BooleanBinding =
        Bindings.createBooleanBinding({ listener(map { it.value }) }, *this)

fun <T, U> Array<ObservableValue<T>>.binding(listener: (values: List<T>) -> U): ObjectBinding<U> =
    Bindings.createObjectBinding({ listener(map { it.value }) }, *this)

fun <T> Array<ObservableValue<T>>.listen(listener: (values: List<T>) -> Unit) {
    forEach { observable ->
        observable.addListener { _, _, _ ->
            listener(map { it.value })
        }
    }
}