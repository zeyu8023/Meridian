package app.meridian.mobile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.json.*
import java.time.LocalDate

/** Reads the same date range as records, independently of the home summary. */
@Composable internal fun DailyTripsPage(api:Api,carId:Any,date:LocalDate,metrics:JSONArray,onClose:()->Unit){
 val scope=rememberCoroutineScope()
 val trips=remember(carId,date){mutableStateListOf<JSONObject>()}
 var page by remember(carId,date){mutableIntStateOf(1)}
 var busy by remember(carId,date){mutableStateOf(false)}
 var more by remember(carId,date){mutableStateOf(true)}
 var loaded by remember(carId,date){mutableStateOf(false)}
 var error by remember(carId,date){mutableStateOf("")}
 var selected by remember(carId,date){mutableStateOf<JSONObject?>(null)}
 var detailBusy by remember{mutableStateOf(false)}
 var detailError by remember{mutableStateOf("")}
 suspend fun load(){
  if(busy)return
  busy=true;error=""
  try{
   val spec=recordReadSpec(carId,false,page,"desc","","all","",-1,date to date)
   val response=api.call(spec.path,spec.body)
   val batch=recordRows(response)?.objects()?:throw IllegalStateException()
   val payload=response.optJSONObject("data")?:response
   val total=payload.number("total")?:payload.number("totalCount")?:response.number("total")
   trips.addAll(batch);loaded=true;page++
   more=batch.isNotEmpty()&&(if(total!=null)trips.size<total else batch.size>=20)
  }catch(e:CancellationException){throw e}catch(_:Exception){error="当天行程读取失败，请重试"}finally{busy=false}
 }
 LaunchedEffect(carId,date){load()}
 FullscreenDataPage("${date.monthValue}月${date.dayOfMonth}日 · 全部行程",onClose){
  Panel{SectionTitle("当日概况",Icons.Outlined.Today);HomeMetricTiles(metrics.objects().map{dailyBriefMetric(it,date==LocalDate.now())})}
  if(trips.isNotEmpty()){
   val chronological=trips.reversed()
   val distance=chronological.mapNotNull{trip->tripDistance(trip)?.let{ChartSample(shortTime(trip.safeText("tripStartTime")),it)}}
   if(distance.size>=2)InteractiveTrend("单次行程里程","km",distance)
  }
  Text("行程明细 · 已加载 ${trips.size} 次",fontSize=14.sp,color=muted)
  trips.forEach{trip->TripTimelineCard(trip,false){
   if(!detailBusy)scope.launch{
    detailBusy=true;detailError=""
    try{
     val id=trip.opt("id")?:trip.opt("tripId")?:throw IllegalStateException()
     selected=api.call("/car/trip/getDetail",JSONObject().put("tripId",id)).optJSONObject("data")?:throw IllegalStateException()
    }catch(e:CancellationException){throw e}catch(_:Exception){detailError="行程详情读取失败，请再次点击该行程重试"}finally{detailBusy=false}
   }
  }}
  if(busy||detailBusy)FlowLoading(label=if(detailBusy)"正在读取行程详情…"else"正在读取当天行程…")
  if(detailError.isNotBlank())Text(detailError,color=MaterialTheme.colorScheme.error)
  if(error.isNotBlank()){Text(error,color=MaterialTheme.colorScheme.error);CinemaOutlineButton({scope.launch{load()}},enabled=!busy){Text("重试读取行程")}}
  if(loaded&&trips.isEmpty()&&!busy)Panel{Text("当天暂无行程记录",color=muted)}
  if(loaded&&more&&!busy&&error.isBlank())CinemaTonalButton({scope.launch{load()}},Modifier.fillMaxWidth()){Text("加载更多行程")}
 }
 selected?.let{TripDetailPage(api,it){selected=null}}
}
