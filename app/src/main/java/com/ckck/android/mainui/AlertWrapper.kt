package com.ckck.android.mainui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertDialogWrapper(
    visible: Boolean,
    onDismiss: () -> Unit,
    title: String,
    content: @Composable () -> Unit,
    actions: @Composable RowScope.() -> Unit = {},
    leftArrangedActions: @Composable RowScope.() -> Unit = {}
) {
    if (!visible) return
    BasicAlertDialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.large,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                if (title.isNotEmpty()) {
                    Text(text = title, style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(12.dp))
                }
                Column(modifier = Modifier.heightIn(max = 550.dp)) {
                    content()
                }
                Spacer(Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    content = {
                        Row(
                            horizontalArrangement = Arrangement.Start,
                            content = leftArrangedActions
                        )
                        Spacer(Modifier.width(12.dp))
                        Row(
                            horizontalArrangement = Arrangement.End,
                            content = actions
                        )
                    }
                )
            }
        }
    }
}