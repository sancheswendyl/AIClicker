/*
 * Copyright (C) 2025 AIClicker
 */
package com.buzbuz.smartautoclicker.feature.smart.config.ui.condition.trigger.timeofday

import android.app.DatePickerDialog
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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

            timePicker.apply {
                setIs24HourView(true)
                setOnTimeChangedListener { _, hour, minute ->
                    viewModel.setHour(hour)
                    viewModel.setMinute(minute)
                }
            }

            buttonSelectDate.setOnClickListener {
                showDatePickerInline(root)
            }

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

    private fun showDatePickerInline(root: android.view.View) {
        val today = LocalDate.now()
        val yearPicker = android.widget.NumberPicker(context).apply {
            minValue = today.year
            maxValue = today.year + 5
            value = today.year
        }
        val monthPicker = android.widget.NumberPicker(context).apply {
            minValue = 1
            maxValue = 12
            value = today.monthValue
            displayedValues = arrayOf("Jan","Fev","Mar","Abr","Mai","Jun","Jul","Ago","Set","Out","Nov","Dez")
        }
        val dayPicker = android.widget.NumberPicker(context).apply {
            minValue = 1
            maxValue = 31
            value = today.dayOfMonth
        }

        val layout = android.widget.LinearLayout(context).apply {
            orientation = android.widget.LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER
            addView(dayPicker)
            addView(monthPicker)
            addView(yearPicker)
        }

        val wm = context.getSystemService(android.content.Context.WINDOW_SERVICE) as android.view.WindowManager
        val params = android.view.WindowManager.LayoutParams(
            android.view.WindowManager.LayoutParams.WRAP_CONTENT,
            android.view.WindowManager.LayoutParams.WRAP_CONTENT,
            android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            android.graphics.PixelFormat.TRANSLUCENT
        )

        android.app.AlertDialog.Builder(context, android.R.style.Theme_DeviceDefault_Dialog_Alert)
            .setTitle(R.string.item_time_of_day_title)
            .setView(layout)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                viewModel.setSpecificDate(LocalDate.of(yearPicker.value, monthPicker.value, dayPicker.value))
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
            .apply {
                window?.setType(android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
                window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
                window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
            }
            .show()
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
