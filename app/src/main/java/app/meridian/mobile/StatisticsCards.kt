package app.meridian.mobile

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.compose.ui.semantics.Role
import org.json.*

@Composable fun StatisticsCards(items:JSONArray,trend:JSONObject?,days:Int,scope:String="period",periodLabel:String="最近 $days 天"){
 var selected by remember{mutableStateOf<JSONObject?>(null)}
 val palette=LocalPalette.current
 val source=trend?.optJSONObject("drivingTrend")?:trend
 fun samples(item:JSONObject):List<ChartSample>{
  if(scope=="lifetime"||item.safeText("label").contains("累计"))return emptyList()
  return when(metricKind(item.safeText("label"))){
   MetricKind.Mileage->if(item.safeText("label").contains("里程"))chartSamples(source?.optJSONArray("mileageStats")?:JSONArray(),"mileage",.001)else emptyList()
   MetricKind.Energy->chartSamples(source?.optJSONArray("energyTrend")?:JSONArray(),"energyConsumption")
   else->emptyList()
  }
 }
 fun descriptions(item:JSONObject)=listOf("subtitle","description","hint","meta").mapNotNull{key->item.opt(key)?.takeIf{it is String}?.toString()?.takeIf{it.isNotBlank()&&it!=item.safeText("label")}}
 fun expandable(item:JSONObject)=samples(item).isNotEmpty()||descriptions(item).isNotEmpty()
 remember(items){items.objects().chunked(2)}.forEach{chunk->
  EqualCardRow(chunk.size,surface={column,modifier->
   val color=metricColor(chunk[column].safeText("label"),palette)
   Box(modifier.themeCardShadow(RoundedCornerShape(18.dp)).clip(RoundedCornerShape(18.dp)).background(Brush.linearGradient(listOf(color.copy(alpha=if(palette.dark).13f else .065f),glassWhite))).border(.7.dp,color.copy(alpha=.12f),RoundedCornerShape(18.dp)).then(if(expandable(chunk[column]))Modifier.cardClick{selected=chunk[column]}else Modifier))
  }){column->
   val item=chunk[column];val label=item.safeText("label");val color=metricColor(label,palette)
   Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(9.dp)){
    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(29.dp).background(color.copy(alpha=.12f),RoundedCornerShape(10.dp)),contentAlignment=Alignment.Center){Icon(metricIcon(label),null,tint=color,modifier=Modifier.size(17.dp))};Spacer(Modifier.weight(1f));if(expandable(item))Icon(Icons.Outlined.ChevronRight,null,tint=muted,modifier=Modifier.size(14.dp))}
    Text(label,fontSize=14.sp,lineHeight=21.sp,color=muted)
    Text(homeValue(item.opt("value")),fontSize=23.sp,lineHeight=30.sp,fontWeight=FontWeight.Medium,color=ink)
    Text(item.safeText("unit").ifBlank{if(scope=="lifetime"||label.contains("累计"))"累计表现"else periodLabel},fontSize=13.sp,lineHeight=19.sp,color=readableTint(color,glassWhite))
   }
  }
 }

 selected?.let{item->val label=item.safeText("label");FullscreenDataPage(label,{selected=null}){
  Panel{SectionTitle(label,metricIcon(label));Text(homeValue(item.opt("value"))+" "+item.safeText("unit"),fontSize=30.sp,fontWeight=FontWeight.SemiBold,color=ink);Text(if(scope=="lifetime"||label.contains("累计"))"车辆累计统计"else"统计范围：$periodLabel",fontSize=14.sp,color=muted);for(key in listOf("subtitle","description","hint","meta")){item.safeText(key).takeIf{item.opt(key) is String&&it.isNotBlank()}?.let{Text(it,fontSize=14.sp,color=muted)}}}
  val points=samples(item)
  if(points.isNotEmpty())InteractiveTrend("$periodLabel · "+if(metricKind(label)==MetricKind.Mileage)"每日里程"else"每日能耗",if(metricKind(label)==MetricKind.Mileage)"km"else"Wh/km",points)
 }}
}
