package app.meridian.mobile
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
@Composable internal fun Modifier.themeCardShadow(shape:Shape):Modifier{
 val palette=LocalPalette.current;val material=palette.cardMaterial
 return if(!palette.dark&&material.elevationDp>0f)shadow(material.elevationDp.dp,shape,clip=false,ambientColor=palette.text.copy(alpha=material.shadowAlpha),spotColor=palette.text.copy(alpha=material.shadowAlpha))else this
}
@Composable internal fun Modifier.themeCardEdge(shape:Shape,alpha:Float=1f):Modifier=
 border(LocalPalette.current.cardMaterial.edgeWidthDp.dp,glassEdge.copy(alpha=glassEdge.alpha*alpha),shape)
