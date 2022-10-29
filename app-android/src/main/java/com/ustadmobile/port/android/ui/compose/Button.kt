package com.example.ustadmobile.ui

import android.graphics.BitmapFactory
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Icon
import android.media.Image
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.DefaultShadowColor
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import com.toughra.ustadmobile.R
import com.ustadmobile.core.generated.locale.MessageID.content
import com.ustadmobile.port.android.ui.theme.ui.theme.primary

@Composable
fun ButtonWithRoundCorners(text: String, onClick: () -> Unit) {
    val drawableId = R.drawable.pre_lollipop_btn_selector_bg_onboarding
    Button(
        onClick = {onClick()},
        contentPadding = PaddingValues(0.dp),
        shape = RoundedCornerShape(5.dp),
        elevation = null,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = primary,
            contentColor = Color.White,
            disabledBackgroundColor = Color.Transparent,
            disabledContentColor = primary.copy(alpha = ContentAlpha.disabled),
        ),
        modifier = Modifier.height(40.dp)
            .width(120.dp)
            .shadow(AppBarDefaults.BottomAppBarElevation)
            .zIndex(1f)
    ) {
        Box(
            contentAlignment = Alignment.Center,
        ) {
            TileAndroidImage(
                drawableId = drawableId,
                contentDescription = "...",
                modifier = Modifier.matchParentSize()
            )
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(8.dp),){
                Text(
                    text = text,
                    style = TextStyle(color = Color.White)
                )
            }
        }
    }
}

@Composable
private fun TileAndroidImage(
    @DrawableRes drawableId: Int,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val drawable = remember(drawableId) {
        BitmapDrawable(
            context.resources,
            BitmapFactory.decodeResource(
                context.resources,
                drawableId
            )
        ).apply {
            tileModeX = Shader.TileMode.REPEAT
            tileModeY = Shader.TileMode.REPEAT
        }
    }
    AndroidView(
        factory = {
            ImageView(it)
        },
        update = { imageView ->
            imageView.background = drawable
        },
        modifier = modifier
            .semantics {
                this.contentDescription = contentDescription
                role = Role.Image
            }
    )
}