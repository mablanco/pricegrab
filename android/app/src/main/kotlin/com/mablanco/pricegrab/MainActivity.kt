package com.mablanco.pricegrab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.mablanco.pricegrab.ui.compare.CompareViewModel

class MainActivity : ComponentActivity() {

    /**
     * The same [CompareViewModel] the Compose tree resolves via
     * `viewModel()` — both share this activity as their ViewModelStore
     * owner.
     */
    private val compareViewModel: CompareViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // FR-008.2: backgrounding the app dismisses the Undo affordance.
        // Hooking into `ON_STOP` (rather than `ON_PAUSE`) gives the user
        // a brief window to return from a fingerprint sheet or system
        // dialog without losing the Undo, while guaranteeing the
        // affordance is gone by the time the activity is fully stopped.
        lifecycle.addObserver(
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_STOP) {
                    compareViewModel.dismissUndo()
                }
            },
        )

        setContent {
            PriceGrabApp()
        }
    }
}
