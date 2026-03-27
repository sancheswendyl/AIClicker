/*
 * Copyright (C) 2024 Kevin Buzeau
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.buzbuz.smartautoclicker.feature.smart.config.ui.scenario

import android.util.Log
import android.view.View
import android.view.ViewGroup

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle

import com.buzbuz.smartautoclicker.core.ui.bindings.dialogs.DialogNavigationButton
import com.buzbuz.smartautoclicker.core.ui.bindings.dialogs.setButtonEnabledState
import com.buzbuz.smartautoclicker.core.common.overlays.base.viewModels
import com.buzbuz.smartautoclicker.core.common.overlays.dialog.implementation.navbar.NavBarDialog
import com.buzbuz.smartautoclicker.core.common.overlays.dialog.implementation.navbar.NavBarDialogContent
import com.buzbuz.smartautoclicker.core.ui.bindings.dialogs.setButtonVisibility
import com.buzbuz.smartautoclicker.feature.smart.config.R
import com.buzbuz.smartautoclicker.feature.smart.config.di.ScenarioConfigViewModelsEntryPoint
import com.buzbuz.smartautoclicker.feature.smart.config.ui.common.dialogs.showCloseWithoutSavingDialog
import com.buzbuz.smartautoclicker.feature.smart.config.ui.scenario.config.ScenarioConfigContent
import com.buzbuz.smartautoclicker.feature.smart.config.ui.scenario.imageevents.ImageEventListContent
import com.buzbuz.smartautoclicker.feature.smart.config.ui.scenario.more.MoreContent
import com.buzbuz.smartautoclicker.feature.smart.config.ui.scenario.triggerevents.TriggerEventListContent

import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.buzbuz.smartautoclicker.feature.smart.config.ui.event.variables.VariablesManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.navigation.NavigationBarView

import kotlinx.coroutines.launch

class ScenarioDialog(
    private val onConfigSaved: () -> Unit,
    private val onConfigDiscarded: () -> Unit,
) : NavBarDialog(R.style.ScenarioConfigTheme) {

    private var variablesManager: VariablesManager? = null

    /** The view model for this dialog. */
    private val viewModel: ScenarioDialogViewModel by viewModels(
        entryPoint = ScenarioConfigViewModelsEntryPoint::class.java,
        creator = { scenarioDialogViewModel() },
    )

    override fun onCreateView(): ViewGroup {
        return super.onCreateView().also {
            topBarBinding.setButtonVisibility(DialogNavigationButton.SAVE, View.VISIBLE)
            topBarBinding.dialogTitle.setText(R.string.dialog_title_scenario_config)
        }
    }

    private fun setupVariablesTab(dialog: BottomSheetDialog) {
        val btnTab = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            setPadding(48, 0, 48, 0)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 120
            )
            setBackgroundColor(context.getColor(android.R.color.transparent))
            isClickable = true
            isFocusable = true
        }

        val tabText = TextView(context).apply {
            text = context.getString(R.string.variables_tab_title)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val btnAdd = android.widget.ImageButton(context).apply {
            setImageResource(R.drawable.ic_add)
            background = null
            layoutParams = LinearLayout.LayoutParams(80, 80).apply { marginEnd = 16 }
            setOnClickListener { variablesManager?.addVariable() }
        }

        val iconTab = ImageView(context).apply {
            setImageResource(R.drawable.ic_chevron_up)
            layoutParams = LinearLayout.LayoutParams(60, 60)
        }

        btnTab.addView(tabText)
        btnTab.addView(btnAdd)
        btnTab.addView(iconTab)

        val recyclerView = RecyclerView(context).apply {
            visibility = View.GONE
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(16, 8, 16, 8)
        }

        val tabContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFF1E1E2E.toInt())
            addView(btnTab)
            addView(recyclerView)
        }

        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            android.view.Gravity.BOTTOM
        )
        tabContainer.layoutParams = params
        val navBarHeight = context.resources.getDimensionPixelSize(
            com.buzbuz.smartautoclicker.core.common.overlays.R.dimen.android_bottom_navigation_height
        )
        val tabParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            android.view.Gravity.BOTTOM
        )
        tabParams.bottomMargin = navBarHeight
        tabContainer.layoutParams = tabParams
        (dialog.window?.decorView?.findViewById<FrameLayout>(com.google.android.material.R.id.container))
            ?.addView(tabContainer)

        // Empurra o FAB acima da aba de variáveis
        val density = context.resources.displayMetrics.density
        val tabHeightPx = (56 * density).toInt()
        val fabParams = createCopyButtons.root.layoutParams as? CoordinatorLayout.LayoutParams
        if (fabParams != null) {
            fabParams.bottomMargin = navBarHeight + tabHeightPx + (8 * density).toInt()
            createCopyButtons.root.layoutParams = fabParams
        }

        variablesManager = VariablesManager(
            context = context,
            overlayManager = overlayManager,
            btnTab = btnTab,
            iconTab = iconTab,
            recyclerView = recyclerView,
            onVariablesChanged = { viewModel.updateVariables(it) },
        )
    }

    override fun inflateMenu(navBarView: NavigationBarView) {
        navBarView.inflateMenu(R.menu.menu_scenario_config)
    }

    override fun onCreateContent(navItemId: Int): NavBarDialogContent = when (navItemId) {
        R.id.page_image_events -> ImageEventListContent(context.applicationContext)
        R.id.page_trigger_events -> TriggerEventListContent(context.applicationContext)
        R.id.page_config -> ScenarioConfigContent(context.applicationContext)
        R.id.page_more -> MoreContent(context.applicationContext)
        else -> throw IllegalArgumentException("Unknown menu id $navItemId")
    }

    override fun onDialogCreated(dialog: BottomSheetDialog) {
        super.onDialogCreated(dialog)
        setupVariablesTab(dialog)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                launch { viewModel.isEditingScenario.collect(::onScenarioEditingStateChanged) }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.navItemsValidity.collect(::updateContentsValidity) }
                launch { viewModel.scenarioCanBeSaved.collect(::updateSaveButtonState) }
                launch { viewModel.scenarioVariables.collect { variablesManager?.updateVariables(it) } }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.monitorViews(
            createEventButton = createCopyButtons.buttonNew,
            saveButton = topBarBinding.buttonSave,
        )
    }

    override fun onPause() {
        super.onPause()
        viewModel.stopViewMonitoring()
    }

    override fun onDialogButtonPressed(buttonType: DialogNavigationButton) {
        when (buttonType) {
            DialogNavigationButton.SAVE -> {
                onConfigSaved()
                super.back()
            }

            DialogNavigationButton.DISMISS -> {
                back()
                return
            }

            DialogNavigationButton.DELETE -> Unit
        }
    }

    override fun back() {
        if (viewModel.hasUnsavedModifications()) {
            context.showCloseWithoutSavingDialog {
                onConfigDiscarded()
                super.back()
            }
            return
        }

        onConfigDiscarded()
        super.back()
    }

    private fun updateContentsValidity(itemsValidity: Map<Int, Boolean>) {
        itemsValidity.forEach { (itemId, isValid) ->
            setMissingInputBadge(itemId, !isValid)
        }
    }

    private fun updateSaveButtonState(isEnabled: Boolean) {
        topBarBinding.setButtonEnabledState(DialogNavigationButton.SAVE, isEnabled)
    }

    private fun onScenarioEditingStateChanged(isEditingScenario: Boolean) {
        if (!isEditingScenario) {
            Log.e(TAG, "Closing ScenarioDialog because there is no scenario edited")
            finish()
        }
    }
}

private const val TAG = "ScenarioDialog"