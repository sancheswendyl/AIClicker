/*
 * Copyright (C) 2025 AIClicker
 */
package com.buzbuz.smartautoclicker.feature.smart.config.ui.event.variables

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.buzbuz.smartautoclicker.core.domain.model.Variable
import com.buzbuz.smartautoclicker.feature.smart.config.databinding.ItemVariableBinding

class VariablesAdapter : ListAdapter<Variable, VariablesAdapter.VariableViewHolder>(VariableDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VariableViewHolder {
        val binding = ItemVariableBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VariableViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VariableViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class VariableViewHolder(private val binding: ItemVariableBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(variable: Variable) {
            binding.apply {
                textVariableType.text = when (variable) {
                    is Variable.NumberVar -> "I"
                    is Variable.BooleanVar -> "B"
                    is Variable.TextVar -> "S"
                }
                textVariableName.text = variable.name
                textVariableValue.text = variable.getDisplayValue()
            }
        }
    }
}

class VariableDiffCallback : DiffUtil.ItemCallback<Variable>() {
    override fun areItemsTheSame(oldItem: Variable, newItem: Variable): Boolean =
        oldItem.name == newItem.name
    override fun areContentsTheSame(oldItem: Variable, newItem: Variable): Boolean =
        oldItem == newItem
}
