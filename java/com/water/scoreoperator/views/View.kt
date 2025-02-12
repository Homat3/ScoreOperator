package com.water.scoreoperator.views

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.serialization.Serializable

@Serializable
abstract class View {

    @Composable
    abstract fun OnCreate(modifier: Modifier)

    abstract fun getRoute() : String
}

enum class ViewPage{
    SettingView, ControlView;
}