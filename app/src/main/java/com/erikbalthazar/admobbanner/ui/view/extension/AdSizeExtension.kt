package com.erikbalthazar.admobbanner.ui.view.extension

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.erikbalthazar.admobbanner.ui.theme.Dimens
import com.google.android.gms.ads.AdSize

/**
 * Returns the height of the [AdSize] in [Dp].
 */
fun AdSize.getHeightInDp(): Dp {
    return this.height.takeIf { it > 0 }?.dp ?: Dimens.BannerHeight
}
