/*
 * Copyright (C) 2023 iamr0s and InstallerX Revived Contributors
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Source: https://github.com/wxxsfxyzm/InstallerX-Revived/
 */

package com.ckck.android.mainui

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ckck.android.R

@Composable
fun withHaptic(type: HapticFeedbackType, onClick: () -> Unit): () -> Unit {
    val haptic = LocalHapticFeedback.current
    return {
        haptic.performHapticFeedback(type)
        onClick()
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CommonDialog(
    onDismissRequest: () -> Unit,
    title: String,
    text: String,
    confirmButtonText: String,
    onConfirm: () -> Unit,
    dismissButtonText: String = stringResource(R.string.action_cancel),
    onDismiss: () -> Unit = onDismissRequest,
    isDestructive: Boolean = false,
) {
    val interactionSources = remember { List(2) { MutableInteractionSource() } }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(dismissOnClickOutside = true)
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 8.dp,
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .widthIn(min = 280.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                ButtonGroup(
                    overflowIndicator = { menuState ->
                        ButtonGroupDefaults.OverflowIndicator(menuState = menuState)
                    },
                ) {
                    val scope = this
                    customItem(
                        buttonGroupContent = {
                            OutlinedButton(
                                onClick = withHaptic(HapticFeedbackType.Reject) {
                                    onDismiss()
                                },
                                shapes = ButtonDefaults.shapes(),
                                modifier = with(scope) {
                                    Modifier
                                        .weight(1f)
                                        .animateWidth(interactionSources[0])
                                },
                                interactionSource = interactionSources[0],
                            ) {
                                Text(
                                    text = dismissButtonText,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        },
                        menuContent = { menuState ->
                            androidx.compose.material3.DropdownMenuItem(
                                text = { Text(dismissButtonText) },
                                onClick = {
                                    onDismiss()
                                    menuState.dismiss()
                                }
                            )
                        }
                    )
                    customItem(
                        buttonGroupContent = {
                            Button(
                                onClick = withHaptic(HapticFeedbackType.Confirm) {
                                    onConfirm()
                                    onDismiss()
                                },
                                colors = if (isDestructive) {
                                    ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error,
                                        contentColor = MaterialTheme.colorScheme.onError
                                    )
                                } else {
                                    ButtonDefaults.buttonColors()
                                },
                                modifier = with(scope) {
                                    Modifier
                                        .weight(1f)
                                        .animateWidth(interactionSources[1])
                                },
                                interactionSource = interactionSources[1],
                                shapes = ButtonDefaults.shapes(),
                            ) {
                                Text(
                                    text = confirmButtonText,
                                )
                            }
                        },
                        menuContent = { menuState ->
                            androidx.compose.material3.DropdownMenuItem(
                                text = { Text(confirmButtonText) },
                                onClick = {
                                    onConfirm()
                                    onDismiss()
                                    menuState.dismiss()
                                }
                            )
                        }
                    )
                }
            }
        }
    }
}