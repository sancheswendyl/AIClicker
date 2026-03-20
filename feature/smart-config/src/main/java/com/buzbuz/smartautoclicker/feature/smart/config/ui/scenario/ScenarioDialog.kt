/*
 * Copyright (C) 2024 Kevin Buzeau
 */
package com.buzbuz.smartautoclicker.feature.smart.config.ui.scenario

import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.buzbuz.smartautoclicker.core.common.overlays.base.viewModels
import com.buzbuz.smartautoclicker.core.common.overlays.dialog.implementation.navbar.NavBarDialog
import com.buzbuz.smartautoclicker.core.common.overlays.dialog.implementation.navbar.NavBarDialogContent
import com.buzbuz.smartautoclicker.core.ui.bindings.dialogs.DialogNavigationButton
import com.buzbuz.smartautoclicker.core.ui.bindings.dialogs.setButtonEnabledState
import com.buzbuz.smartautoclicker.core.ui.bindings.dialogs.setButtonVisibility
import com.buzbuz.smartautoclicker.feature.smart.config.R
import com.buzbuz.smartautoclicker.feature.smart.config.di.ScenarioConfigViewModelsEntryPoint
import com.buzbuz.smartautoclicker.feature.smart.config.ui.common.dialogs.showCloseWithoutSavingDialog
import com.buzbuz.smartautoclicker.feature.smart.config.ui.event.variables.VariablesManager
import com.buzbuz.smartautoclicker.feature.smart.config.ui.scenario.config.ScenarioConfigContent
import com.buzbuz.smartautoclicker.feature.smart.config.ui.scenario.imageevents.ImageEventListContent
import com.buzbuz.smartautoclicker.feature.smart.config.ui.scenario.more.MoreContent
import com.buzbuz.smartautoclicker.feature.smart.config.ui.scenario.triggerevents.TriggerEventListContent
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.navigation.NavigationBarView
import kotlinx.coroutines.launch

class ScenarioDialog(
    private val onConfigSaved: () -> Unit,
    private val onConfigDiscarded: () -> Unit,
) : NavBarDialog(R.style.ScenarioConfigTheme) {

    private var variablesManager: VariablesManager? = null

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

    private fun setupVariablesTab(dialog: BottomSheetDialog) {
        // Encontrar o BottomSheet e adicionar margem inferior
        val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            ?: return

        // Levantar o bottomSheet para dar espaço à aba
        val sheetParams = bottomSheet.layoutParams as? android.widget.FrameLayout.LayoutParams
        if (sheetParams != null) {
            sheetParams.bottomMargin = 150
            bottomSheet.layoutParams = sheetParams
        }

        // Criar a aba de variáveis
        val iconTab = ImageView(context).apply {
            setImageResource(R.drawable.ic_chevron_up)
            layoutParams = LinearLayout.LayoutParams(60, 60)
        }

        val btnAdd = android.widget.ImageButton(context).apply {
            setImageResource(R.drawable.ic_add)
            background = null
            layoutParams = LinearLayout.LayoutParams(80, 80).apply { marginEnd = 16 }
            setOnClickListener { variablesManager?.addVariable() }
        }

        val tabText = TextView(context).apply {
            text = context.getString(R.string.variables_tab_title)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val btnTab = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            setPadding(48, 0, 48, 0)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 150
            )
            isClickable = true
            isFocusable = true
            addView(tabText)
            addView(btnAdd)
            addView(iconTab)
        }

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
            val params = CoordinatorLayout.LayoutParams(
                CoordinatorLayout.LayoutParams.MATCH_PARENT,
                CoordinatorLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = android.view.Gravity.BOTTOM
            }
            layoutParams = params
            addView(btnTab)
            addView(recyclerView)
        }

        variablesManager = VariablesManager(
            context = context,
            overlayManager = overlayManager,
            btnTab = btnTab,
            iconTab = iconTab,
            recyclerView = recyclerView,
            onVariablesChanged = { viewModel.updateVariables(it) },
        )

        // Adicionar a aba no CoordinatorLayout pai do BottomSheet
        (bottomSheet.parent as? CoordinatorLayout)?.addView(tabContainer)
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
