/*
 * Copyright (C) 2025 AIClicker
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.buzbuz.smartautoclicker.feature.smart.config.ui.condition.trigger.timeofday

import android.app.DatePickerDialog
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.buzbuz.smartautoclicker.core.common.overlays.base.viewModels
import com.buzbuz.smartautoclicker.core.common.overlays.dialog.OverlayDialog
import com.buzbuz.smartautoclicker.core.ui.bindings.dialogs.DialogNavigationButton
import com.buzbuz.smartautoclicker.core.ui.bindings.dialogs.setButtonEnabledState
import com.buzbuz.smartautoclicker.core.ui.bindings.fields.setError
import com.buzbuz.smartautoclicker.core.ui.bindings.fields.setLabel
import com.buzbuz.smartautoclicker.core.ui.bindings.fields.setOnTextChangedListener
import com.buzbuz.smartautoclicker.core.ui.bindings.fields.setText
import com.buzbuz.smartautoclicker.feature.smart.config.R
import com.buzbuz.smartautoclicker.feature.smart.config.databinding.DialogConfigConditionTimeOfDayBinding
import com.buzbuz.smartautoclicker.feature.smart.config.di.ScenarioConfigViewModelsEntryPoint
import com.buzbuz.smartautoclicker.feature.smart.config.ui.common.dialogs.showCloseWithoutSavingDialog
import com.buzbuz.smartautoclicker.feature.smart.config.ui.condition.OnConditionConfigCompleteListener
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class TimeOfDayConditionDialog(
    private val listener: OnConditionConfigCompleteListener,
) : OverlayDialog(R.style.ScenarioConfigTheme) {

    private val viewModel: TimeOfDayConditionViewModel by viewModels(
        entryPoint = ScenarioConfigViewModelsEntryPoint::class.java,
        creator = { timeOfDayConditionViewModel() },
    )

    private lateinit var viewBinding: DialogConfigConditionTimeOfDayBinding

    override fun onCreateView(): ViewGroup {
        viewBinding = DialogConfigConditionTimeOfDayBinding.inflate(LayoutInflater.from(context)).apply {
            layoutTopBar.apply {
                dialogTitle.setText(R.string.item_time_of_day_title)
                buttonDismiss.setDebouncedOnClickListener { back() }
                buttonSave.apply {
                    visibility = View.VISIBLE
                    setDebouncedOnClickListener {
                        listener.onConfirmClicked()
                        super.back()
                    }
                }
                buttonDelete.apply {
                    visibility = View.VISIBLE
                    setDebouncedOnClickListener {
                        listener.onDeleteClicked()
                        super.back()
                    }
                }
            }

            fieldName.apply {
                setLabel(R.string.generic_name)
                setOnTextChangedListener { viewModel.setName(it.toString()) }
                textField.filters = arrayOf<InputFilter>(
                    InputFilter.LengthFilter(context.resources.getInteger(R.integer.name_max_length))
                )
            }
            hideSoftInputOnFocusLoss(fieldName.textField)

            // TimePicker para hora e minuto
            timePicker.apply {
                setIs24HourView(true)
                setOnTimeChangedListener { _, hour, minute ->
                    viewModel.setHour(hour)
                    viewModel.setMinute(minute)
                }
            }

            // Botão para selecionar data específica
            buttonSelectDate.setOnClickListener {
                val today = LocalDate.now()
                DatePickerDialog(
                    context,
                    { _, year, month, day ->
                        val selectedDate = LocalDate.of(year, month + 1, day)
                        viewModel.setSpecificDate(selectedDate)
                    },
                    today.year, today.monthValue - 1, today.dayOfMonth
                ).show()
            }

            // Chips dos dias da semana
            chipMon.setOnCheckedChangeListener { _, _ -> viewModel.toggleDayOfWeek(DayOfWeek.MONDAY) }
            chipTue.setOnCheckedChangeListener { _, _ -> viewModel.toggleDayOfWeek(DayOfWeek.TUESDAY) }
            chipWed.setOnCheckedChangeListener { _, _ -> viewModel.toggleDayOfWeek(DayOfWeek.WEDNESDAY) }
            chipThu.setOnCheckedChangeListener { _, _ -> viewModel.toggleDayOfWeek(DayOfWeek.THURSDAY) }
            chipFri.setOnCheckedChangeListener { _, _ -> viewModel.toggleDayOfWeek(DayOfWeek.FRIDAY) }
            chipSat.setOnCheckedChangeListener { _, _ -> viewModel.toggleDayOfWeek(DayOfWeek.SATURDAY) }
            chipSun.setOnCheckedChangeListener { _, _ -> viewModel.toggleDayOfWeek(DayOfWeek.SUNDAY) }
        }

        return viewBinding.root
    }

    override fun onDialogCreated(dialog: BottomSheetDialog) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                launch { viewModel.isEditingCondition.collect(::onConditionEditingStateChanged) }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.name.collect(viewBinding.fieldName::setText) }
                launch { viewModel.nameError.collect(viewBinding.fieldName::setError) }
                launch { viewModel.hour.collect(::updateTimePicker) }
                launch { viewModel.specificDate.collect(::updateSelectedDate) }
                launch { viewModel.daysOfWeek.collect(::updateDayChips) }
                launch { viewModel.conditionCanBeSaved.collect(::updateSaveButton) }
            }
        }
    }

    override fun back() {
        if (viewModel.hasUnsavedModifications()) {
            context.showCloseWithoutSavingDialog {
                listener.onDismissClicked()
                super.back()
            }
            return
        }
        listener.onDismissClicked()
        super.back()
    }

    private fun updateTimePicker(hour: Int) {
        viewBinding.timePicker.hour = hour
    }

    private fun updateSelectedDate(date: LocalDate?) {
        viewBinding.apply {
            if (date != null) {
                val formatted = date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
                textSelectedDate.text = formatted
                textSelectedDate.visibility = View.VISIBLE
                buttonSelectDate.text = formatted
            } else {
                textSelectedDate.visibility = View.GONE
                buttonSelectDate.setText(R.string.item_time_of_day_select_date)
            }
        }
    }

    private fun updateDayChips(days: Set<DayOfWeek>) {
        viewBinding.apply {
            chipMon.isChecked = DayOfWeek.MONDAY in days
            chipTue.isChecked = DayOfWeek.TUESDAY in days
            chipWed.isChecked = DayOfWeek.WEDNESDAY in days
            chipThu.isChecked = DayOfWeek.THURSDAY in days
            chipFri.isChecked = DayOfWeek.FRIDAY in days
            chipSat.isChecked = DayOfWeek.SATURDAY in days
            chipSun.isChecked = DayOfWeek.SUNDAY in days
        }
    }

    private fun updateSaveButton(canBeSaved: Boolean) {
        viewBinding.layoutTopBar.setButtonEnabledState(DialogNavigationButton.SAVE, canBeSaved)
    }

    private fun onConditionEditingStateChanged(isEditing: Boolean) {
        if (!isEditing) finish()
    }
}
