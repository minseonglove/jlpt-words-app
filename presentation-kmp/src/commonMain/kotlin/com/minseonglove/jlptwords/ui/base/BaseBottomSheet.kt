package com.minseonglove.jlptwords.ui.base

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetDefaults
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.minseonglove.jlptwords.ui.theme.MolluTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseBottomSheet(
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
    properties: ModalBottomSheetProperties = ModalBottomSheetDefaults.properties,
    shape: Shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    onDismissRequest: () -> Unit,
    content: @Composable (ColumnScope.() -> Unit),
) {
    ModalBottomSheet(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        shape = shape,
        dragHandle = {
            BaseDragHandle(
                modifier =
                    Modifier.padding(
                        vertical = 12.dp,
                    ),
            )
        },
        properties = properties,
        containerColor = MolluTheme.colorScheme.backgroundNormal,
        content = content,
    )
}
