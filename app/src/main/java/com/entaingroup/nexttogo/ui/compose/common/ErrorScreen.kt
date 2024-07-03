package com.entaingroup.nexttogo.ui.compose.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.entaingroup.nexttogo.R
import com.entaingroup.nexttogo.ui.theme.NextToGoTheme

@Composable
fun ErrorScreen(
    message: String = stringResource(R.string.error_message_generic),
    buttonName: String = stringResource(R.string.error_button_default_text),
    buttonAction: (() -> Unit?)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.error),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            modifier = Modifier
                .size(50.dp),
            painter = painterResource(id = android.R.drawable.ic_dialog_alert),
            contentDescription = stringResource(id = R.string.error_image_description),
            contentScale = ContentScale.Fit
        )
        Text(
            modifier = Modifier
                .padding(10.dp),
            text = message,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onError,
        )
        buttonAction?.let {
            Button(
                onClick = { buttonAction.invoke() },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onError)
            ){
                Text(
                    text = buttonName,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
fun ErrorScreenPreview() {
    NextToGoTheme {
        ErrorScreen(buttonAction = {})
    }
}