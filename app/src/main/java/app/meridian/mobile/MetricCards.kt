package app.meridian.mobile

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import org.json.*

/** Shared line slots keep peers aligned without clipping larger accessibility fonts. */
@Composable fun MetricDataCard(label:String,value:String,unit:String,icon:ImageVector,modifier:Modifier=Modifier,tint:Color=accent,footer:String="") {
 Column(modifier.background(softWash,RoundedCornerShape(16.dp)).themeCardEdge(RoundedCornerShape(16.dp)).padding(12.dp),verticalArrangement=Arrangement.spacedBy(6.dp)) {
  Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(6.dp)){
   Icon(icon,null,tint=tint,modifier=Modifier.size(17.dp))
   Text(label,fontSize=13.sp,lineHeight=18.sp,color=muted,modifier=Modifier.weight(1f))
  }
  Text(value,fontSize=20.sp,lineHeight=24.sp,fontWeight=FontWeight.SemiBold,color=ink)
  if(unit.isNotBlank())Text(unit,fontSize=13.sp,lineHeight=19.sp,color=muted)
  if(footer.isNotBlank())Text(footer,fontSize=13.sp,lineHeight=19.sp,color=readableTint(tint,softWash))
 }
}

data class StatisticsMetricGroup(val title:String,val items:JSONArray,val key:String="",val scope:String="period")
/** Scope is part of identity: equal period and lifetime values are not duplicates. */
fun statisticsMetricGroups(root:JSONObject):List<StatisticsMetricGroup> {
 val seen=mutableSetOf<String>();val merged=linkedMapOf<String,StatisticsMetricGroup>()
 for((source,scope) in listOf("periodDashboard" to "period","lifetimeDashboard" to "lifetime")){
  val groups=root.optJSONObject(source)?.optJSONArray("metricGroups")?:continue
  for(group in groups.objects()){
   val groupKey=group.safeText("key");val title=group.safeText("title").ifBlank{groupKey}
   for(item in group.optJSONArray("items")?.objects().orEmpty()){
    val label=item.safeText("label").trim();val key=item.safeText("key").ifBlank{item.safeText("code")}.ifBlank{label}
    val declared=item.safeText("scope")
    val itemScope=when{declared in listOf("period","lifetime")->declared;label.contains("累计")->"lifetime";label.contains("统计期")->"period";else->scope}
    val identity=itemScope+"|"+groupKey.ifBlank{title}
    val raw=item.opt("value");val canonical=when(raw){null,JSONObject.NULL->"missing";is Number->raw.toDouble().toString();else->raw.toString().trim()}
    val metricId=identity+"|"+key+"|"+item.safeText("unit")+"|"+canonical
    if(key.isBlank()||seen.add(metricId)){
     val target=merged.getOrPut(identity){StatisticsMetricGroup(title,JSONArray(),groupKey,itemScope)}
     target.items.put(item)
    }
   }
  }
 }
 val order=listOf("overview","trip","energy","efficiency","charge")
 return merged.values.sortedWith(compareBy<StatisticsMetricGroup>{order.indexOf(it.key).let{n->if(n<0)99 else n}}.thenBy{it.scope=="lifetime"})
}
