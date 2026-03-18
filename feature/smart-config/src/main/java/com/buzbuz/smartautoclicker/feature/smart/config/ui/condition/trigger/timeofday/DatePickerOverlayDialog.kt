/*
 * Copyright (C) 2025 AIClicker
 */
package com.buzbuz.smartautoclicker.feature.smart.config.ui.condition.trigger.timeofday

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import android.widget.LinearLayout
import com.buzbuz.smartautoclicker.core.common.overlays.dialog.OverlayDialog
import com.buzbuz.smartautoclicker.feature.smart.config.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.time.LocalDate

class DatePickerOverlayDialog(
    private val initialDate: LocalDate = LocalDate.now(),
    private val onDateSelected: (LocalDate) -> Unit,
) : OverlayDialog(R.style.ScenarioConfigTheme) {

    private lateinit var dayPicker: NumberPicker
    private lateinit var monthPicker: NumberPicker
    private lateinit var yearPicker: NumberPicker

    override fun onCreateView(): ViewGroup {
        val today = initialDate

        dayPicker = NumberPicker(context).apply {
            minValue = 1
            maxValue = 31
            value = today.dayOfMonth
        }
        monthPicker = NumberPicker(context).apply {
            minValue = 1
            maxValue = 12
            value = today.monthValue
            displayedValues = arrayOf("Jan","Fev","Mar","Abr","Mai","Jun","Jul","Ago","Set","Out","Nov","Dez")
        }
        yearPicker = NumberPicker(context).apply {
            minValue = today.year
            maxValue = today.year + 5
            value = today.year
        }

        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)

            val title = android.widget.TextView(context).apply {
                text = context.getString(R.string.item_time_of_day_title)
                textSize = 18f
                setPadding(0, 0, 0, 32)
            }
            addView(title)

            val pickersRow = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER
                addView(dayPicker)
                addView(monthPicker)
                addView(yearPicker)
            }
            addView(pickersRow)

            val buttonsRow = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.END
                setPadding(0, 32, 0, 0)

                val cancelBtn = android.widget.Button(context).apply {
                    text = context.getString(android.R.string.cancel)
                    setOnClickListener { back() }
                }
                val okBtn = android.widget.Button(context).apply {
                    text = context.getString(android.R.string.ok)
                    setOnClickListener {
                        onDateSelected(LocalDate.of(yearPicker.value, monthPicker.value, dayPicker.value))
                        back()
                    }
                }
                addView(cancelBtn)
                addView(okBtn)
            }
            addView(buttonsRow)
        }

        return root as ViewGroup
    }

    override fun onDialogCreated(dialog: BottomSheetDialog) {}
}
