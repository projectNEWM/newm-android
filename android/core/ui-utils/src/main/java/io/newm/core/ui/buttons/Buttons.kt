package io.newm.core.ui.buttons

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Gray
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.newm.core.resources.R
import io.newm.core.theme.DarkViolet
import io.newm.core.theme.Pinkish
import io.newm.core.theme.Purple
import io.newm.core.theme.inter
import io.newm.core.ui.utils.iconGradient

private val enabledButtonGradient =
    iconGradient(DarkViolet, Pinkish)

private val disabledButtonGradient =
    iconGradient(DarkViolet.copy(alpha = 0.4f), Pinkish.copy(alpha = 0.4f))

@Composable
fun PrimaryButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    enabledIconRes: Int? = null,
) {
    Row(
        modifier = modifier
            .height(40.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(brush = if (enabled) enabledButtonGradient else disabledButtonGradient)
            .then(
                if (enabled) Modifier.clickable { onClick.invoke() }
                else Modifier
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        enabledIconRes?.takeIf { enabled }?.let {
            Icon(
                painter = painterResource(id = it),
                contentDescription = stringResource(R.string.check_icon_description),
                tint = White,
            )
            Spacer(modifier = Modifier.size(8.dp))
        }
        Text(
            text = text,
            color = if (enabled) MaterialTheme.colors.onPrimary else Gray,
            fontFamily = inter,
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
fun SecondaryButton(
    labelResId: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    backgroundBrush: Brush = defaultButtonGradient,
    textStyle: TextStyle = defaultButtonLabelStyle,
    enabled: Boolean = true,
    iconResId: Int? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundBrush)
            .fillMaxWidth()
            .height(40.dp),
        elevation = null,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            iconResId?.let {
                Icon(
                    painter = painterResource(id = it),
                    contentDescription = null,
                    tint = Purple
                )
                Spacer(modifier = Modifier.size(8.dp))
            }
            Text(
                text = stringResource(id = labelResId),
                style = textStyle
            )
        }
    }
}

private val defaultButtonGradient =
    iconGradient(DarkViolet.copy(alpha = 0.08f), Pinkish.copy(alpha = 0.08f))

private val defaultButtonLabelStyle = TextStyle(
    fontSize = 16.sp,
    fontFamily = inter,
    fontWeight = FontWeight.Medium,
    color = Purple
)