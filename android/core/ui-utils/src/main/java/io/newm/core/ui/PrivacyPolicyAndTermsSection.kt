package io.newm.core.ui

import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp

@Composable
fun PrivacyPolicyAndTermsSection(
    modifier: Modifier = Modifier,
    onPrivacyPolicyClicked: () -> Unit,
    onTermsOfServiceClicked: () -> Unit
) {
    val annotatedText = buildAnnotatedString {
        append("By continuing, I agree to NEWM's\n")

        pushStringAnnotation(tag = "privacy", annotation = "privacy_policy")
        withStyle(
            style = SpanStyle(
                color = MaterialTheme.colors.primary,
                textDecoration = TextDecoration.Underline
            )
        ) {
            append("Privacy Policy")
        }
        pop()

        append(" and ")

        pushStringAnnotation(tag = "terms", annotation = "terms_of_service")
        withStyle(
            style = SpanStyle(
                color = MaterialTheme.colors.primary,
                textDecoration = TextDecoration.Underline
            )
        ) {
            append("Terms of Service")
        }
        pop()
    }

    ClickableText(
        text = annotatedText,
        style = MaterialTheme.typography.body2.copy(
            fontSize = 12.sp,
            color = MaterialTheme.colors.onBackground
        ),
        modifier = modifier,
        onClick = { offset ->
            annotatedText.getStringAnnotations(tag = "privacy", start = offset, end = offset)
                .firstOrNull()?.let {
                    onPrivacyPolicyClicked()
                }

            annotatedText.getStringAnnotations(tag = "terms", start = offset, end = offset)
                .firstOrNull()?.let {
                    onTermsOfServiceClicked()
                }
        }
    )
}
