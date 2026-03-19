/*
 * Copyright (C) 2025 AIClicker
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.buzbuz.smartautoclicker.core.domain.model

/**
 * Represents a variable that can be used in procedures.
 * Variables are identified by name and can hold different types of values.
 */
sealed class Variable {

    abstract val name: String

    /** A numeric integer variable. */
    data class NumberVar(
        override val name: String,
        val value: Int = 0,
    ) : Variable()

    /** A boolean variable (true/false). */
    data class BooleanVar(
        override val name: String,
        val value: Boolean = false,
    ) : Variable()

    /** A text string variable. */
    data class TextVar(
        override val name: String,
        val value: String = "",
    ) : Variable()

    /** Get the value as a string for display purposes. */
    fun getDisplayValue(): String = when (this) {
        is NumberVar -> value.toString()
        is BooleanVar -> if (value) "Verdadeiro" else "Falso"
        is TextVar -> value
    }

    /** Get the type name for display purposes. */
    fun getTypeName(): String = when (this) {
        is NumberVar -> "Número"
        is BooleanVar -> "Booleano"
        is TextVar -> "Texto"
    }
}

/** Type of variable for database storage. */
enum class VariableType {
    NUMBER,
    BOOLEAN,
    TEXT,
}

/** Operation to apply to a variable. */
enum class VariableOperation {
    /** Set the variable to a specific value. */
    SET,
    /** Add to the current value (NumberVar only). */
    ADD,
    /** Subtract from the current value (NumberVar only). */
    MINUS,
    /** Toggle the boolean value (BooleanVar only). */
    TOGGLE,
    /** Append text to the current value (TextVar only). */
    APPEND,
}

/** Comparison operation for variable conditions. */
enum class VariableComparisonOperation {
    EQUALS,
    NOT_EQUALS,
    GREATER,
    GREATER_OR_EQUALS,
    LOWER,
    LOWER_OR_EQUALS,
    CONTAINS,
    NOT_CONTAINS,
}
