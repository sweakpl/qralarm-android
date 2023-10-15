package com.sweak.qralarm.ui.screens.guide

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.TextDecoration
import com.sweak.qralarm.R
import com.sweak.qralarm.ui.theme.ButterflyBush
import com.sweak.qralarm.ui.theme.space

@Composable
fun GuidePageBackgroundWork() {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier
                .padding(horizontal = MaterialTheme.space.large)
                .wrapContentWidth(),
            text = stringResource(R.string.important),
            style = MaterialTheme.typography.displayMedium
        )

        Text(
            modifier = Modifier
                .padding(top = MaterialTheme.space.large)
                .wrapContentWidth(),
            text = stringResource(R.string.fix_background_work),
            style = MaterialTheme.typography.displayMedium
        )

        Icon(
            modifier = Modifier
                .padding(MaterialTheme.space.large)
                .wrapContentWidth(),
            painter = painterResource(id = R.drawable.ic_fix),
            contentDescription = "Fix icon",
            tint = MaterialTheme.colorScheme.tertiary
        )

        BackgroundWorkText(
            modifier = Modifier
                .padding(horizontal = MaterialTheme.space.large)
                .fillMaxWidth()
        )
    }
}

const val DONT_KILL_MY_APP_TAG_URL = "dontkillmyapp"

@Composable
fun BackgroundWorkText(
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current

    val link = stringResource(R.string.dontkillmyapp_verbose)
    val annotatedText = attachLink(
        stringResource(R.string.background_work_description),
        stringResource(R.string.dontkillmyapp_short),
        link
    )

    ClickableText(
        modifier = modifier,
        text = annotatedText,
        onClick = {
            annotatedText
                .getStringAnnotations(DONT_KILL_MY_APP_TAG_URL, it, it)
                .firstOrNull()
                ?.let { uriHandler.openUri(link) }
        },
        style = MaterialTheme.typography.bodyLarge
    )
}

fun attachLink(
    source: String,
    segment: String,
    link: String
): AnnotatedString {
    val builder = AnnotatedString.Builder()
    builder.append(source)

    val hyperlinkStyle = SpanStyle(
        color = ButterflyBush,
        textDecoration = TextDecoration.Underline
    )
    val clickableSegmentStart = source.indexOf(segment)
    val clickableSegmentEnd = clickableSegmentStart + segment.length


    builder.addStyle(hyperlinkStyle, clickableSegmentStart, clickableSegmentEnd)
    builder.addStringAnnotation(
        DONT_KILL_MY_APP_TAG_URL,
        link,
        clickableSegmentStart,
        clickableSegmentEnd
    )

    return builder.toAnnotatedString()
}