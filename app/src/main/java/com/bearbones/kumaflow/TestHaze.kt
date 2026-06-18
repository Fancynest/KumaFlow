package com.bearbones.kumaflow

import androidx.compose.ui.Modifier
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import androidx.compose.runtime.Composable

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun test(modifier: Modifier, state: HazeState): Modifier {
    return modifier.hazeChild(state = state, style = HazeMaterials.regular())
}
