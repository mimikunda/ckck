package com.ckck.android.mainui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ckck.android.viewmodels.MainViewModel

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel = hiltViewModel(),
) {
    Scaffold(
        modifier = modifier,
    ) { innerPadding ->
        Box(
            modifier = Modifier.padding(innerPadding)
        ) {
            NavigationManager()
        }
    }
}

@Composable
fun NavigationManager() {
    Column {
        TextField(
            state = rememberTextFieldState(),
            label = { Text("From") }
        )
        TextField(
            state = rememberTextFieldState(),
            label = { Text("To") }
        )
        Button(
            onClick = { },
            content = { Text("Go") }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainPagePreview() {
    NavigationManager()
}