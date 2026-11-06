package app.meridian.mobile
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

internal fun meridianTypography()=Typography(
 bodyLarge=TextStyle(fontSize=16.sp,lineHeight=24.sp),
 bodyMedium=TextStyle(fontSize=15.sp,lineHeight=22.sp),
 bodySmall=UiType.metadata,
 labelLarge=UiType.label.copy(fontWeight=FontWeight.Medium),
 labelMedium=TextStyle(fontSize=13.sp,lineHeight=19.sp,fontWeight=FontWeight.Medium),
 labelSmall=UiType.metadata,
 titleLarge=TextStyle(fontSize=20.sp,lineHeight=28.sp,fontWeight=FontWeight.SemiBold),
 titleMedium=TextStyle(fontSize=16.sp,lineHeight=23.sp,fontWeight=FontWeight.SemiBold),
 titleSmall=UiType.label.copy(fontWeight=FontWeight.SemiBold)
)
