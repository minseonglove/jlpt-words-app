package com.minseonglove.jlptwords.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.container

abstract class BaseViewModel<STATE : Any, SIDE_EFFECT : Any> :
    ViewModel(),
    ContainerHost<STATE, SIDE_EFFECT> {
    protected fun container(
        initialState: STATE,
        onCreate: (suspend org.orbitmvi.orbit.syntax.Syntax<STATE, SIDE_EFFECT>.() -> Unit)? = null,
    ): Container<STATE, SIDE_EFFECT> = viewModelScope.container(initialState, onCreate = onCreate)
}
