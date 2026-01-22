package org.piramalswasthya.cho.model

import androidx.navigation.NavDirections
import kotlinx.coroutines.flow.Flow

data class Icon(
    val icon: Int,
    val title: String,
    val count: Flow<Int>?,
    val navAction: NavDirections,
    var colorPrimary: Boolean = true,
    val allowRedBorder: Boolean = false
)
