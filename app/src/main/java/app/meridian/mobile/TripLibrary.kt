@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
package app.meridian.mobile
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import kotlinx.coroutines.*
import org.json.*
import java.time.LocalDate

internal fun tripDigest(j:JSONObject)=JSONObject().apply{for(key in listOf("id","tripId","carId","tripStartTime","tripEndTime","startLocation","endLocation","startAddress","endAddress","mileageKm","drivingMileage","durationMinutes","drivingDuration","avgSpeed","maxSpeed","tripEnergyConsumption","totalPowerConsumption","route"))if(j.has(key)&&!j.isNull(key))put(key,j.get(key))}
internal fun notebookIdentity(data:JSONObject,activeCar:String):Pair<String,String>?{
 fun valid(value:Any?)=value?.takeIf{it!=JSONObject.NULL}?.toString()?.trim()?.takeIf{it.isNotEmpty()&&it!="null"}
 val id=valid(data.opt("id"))?:valid(data.opt("tripId"))?:return null
 val car=valid(data.opt("carId"))?:valid(activeCar)?:return null
 return id to car
}
internal fun mergeTripNoteArchive(archive:JSONObject,id:String,trip:JSONObject,text:String?=null,tag:String?=null,favorite:Boolean?=null,choices:List<String>?=null,extra:String?=null):JSONObject{
 require(id.isNotBlank())
 val result=JSONObject(archive.toString());val note=JSONObject(result.optJSONObject(id)?.toString()?:"{}")
 if(text!=null)note.put("note",text.take(1000));if(tag!=null)note.put("tag",tag);if(favorite!=null)note.put("favorite",favorite)
 if(choices!=null)note.put("noteChoices",JSONArray(normalizedTripNoteChoices(choices)));if(extra!=null)note.put("noteExtra",extra.take(800))
 note.put("trip",tripDigest(trip));result.put(id,note);return result
}
@Composable internal fun TripNotebook(api:Api,data:JSONObject){
 val identity=notebookIdentity(data,api.activeCarId)?:return;val (id,carId)=identity
 val scope=api.localScope(carId);val key="notes:"+scope;val jobScope=rememberCoroutineScope()
 var note by remember(scope,id){mutableStateOf<JSONObject?>(null)};var error by remember(scope,id){mutableStateOf("")};var saving by remember(scope,id){mutableStateOf(false)}
 var open by rememberSaveable(scope,id){mutableStateOf(false)};var text by rememberSaveable(scope,id){mutableStateOf("")};var tag by rememberSaveable(scope,id){mutableStateOf("未分类")}
 var choices by rememberSaveable(scope,id,stateSaver=listSaver<List<String>,String>(save={it},restore={it.toList()})){mutableStateOf(emptyList<String>())}
 var initialized by rememberSaveable(scope,id){mutableStateOf(false)}
 val reveal=remember{BringIntoViewRequester()}
 LaunchedEffect(scope,id){try{note=withContext(Dispatchers.IO){DeviceData.read(key).optJSONObject(id)?:JSONObject()}}catch(e:CancellationException){throw e}catch(_:Exception){error="手记读取失败，请重新打开详情重试"}}
 LaunchedEffect(open,note){if(open&&!initialized&&note!=null){text=tripNoteExtra(note);tag=note?.optString("tag","未分类")?:"未分类";choices=readTripNoteChoices(note);initialized=true}}
 LaunchedEffect(open){if(open){withFrameNanos{};runCatching{reveal.bringIntoView()}}}
 fun save(favorite:Boolean?=null){if(saving||note==null)return;saving=true;error="";val extra=text;val selectedChoices=choices.toList();val draft=composeTripNote(selectedChoices,extra);val selectedTag=tag
  jobScope.launch{try{val saved=withContext(Dispatchers.IO){synchronized(DeviceData){val latest=DeviceData.read(key);val next=mergeTripNoteArchive(latest,id,data,if(favorite==null)draft else null,if(favorite==null)selectedTag else null,favorite,if(favorite==null)selectedChoices else null,if(favorite==null)extra else null);DeviceData.write(key,next);next.getJSONObject(id)}};note=saved;if(favorite==null)open=false}catch(e:CancellationException){throw e}catch(_:Exception){error="保存失败，编辑内容已保留，请重试"}finally{saving=false}}
 }
 Panel{
  Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Box(Modifier.weight(1f)){SectionTitle("我的行程手记",Icons.Outlined.BookmarkBorder)}
   ToolbarActionStrip(buildList{
    add(ToolbarAction(if(note?.optBoolean("favorite")==true)Icons.Outlined.Bookmark else Icons.Outlined.BookmarkBorder,"收藏行程",enabled=note!=null&&!saving,active=note?.optBoolean("favorite")==true){save(note?.optBoolean("favorite")!=true)})
    if(!open)add(ToolbarAction(Icons.Outlined.EditNote,"编辑行程手记",enabled=!saving){initialized=false;error="";open=true})
   })
  }
  if(open)Column(Modifier.fillMaxWidth().bringIntoViewRequester(reveal),verticalArrangement=Arrangement.spacedBy(12.dp)){
   if(!initialized)FlowLoading(label="正在准备手记…")else{
   Text("点选即可记录，所有选项都可留空",style=UiType.metadata,color=muted)
   SmallSelectionMenu(tag,tripNoteCategories.map{it to it}){if(!saving)tag=it}
   TripNoteQuickChoices(choices,!saving){choices=it}
   OutlinedTextField(text,{text=it.take(800)},Modifier.fillMaxWidth(),enabled=!saving,label={Text("补充一句 · 可选")},placeholder={Text("还有什么想记住的？")},supportingText={Text("${text.length}/800 · 仅保存在本机")},minLines=1,maxLines=4)
   }
   Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)){
    CinemaOutlineButton({open=false;error=""},Modifier.weight(1f),enabled=!saving){Text("取消")}
    CinemaButton({save()},Modifier.weight(1f),enabled=!saving&&initialized){Text(if(saving)"保存中…"else if(!initialized)"正在读取…"else"保存手记")}
   }
  }else if(note==null)Text("正在读取手记…",fontSize=13.sp,color=muted)else{
   Text(note!!.optString("tag","未分类"),fontSize=12.sp,color=accent)
   Text(privateProse(note!!.optString("note").ifBlank{"添加备注，留住这次出发的记忆"}),fontSize=13.sp,color=muted)
  }
  if(error.isNotBlank())Text(error,fontSize=13.sp,color=MaterialTheme.colorScheme.error)
 }
}
@Composable internal fun TripLibrary(api:Api,carId:Any,onClose:()->Unit){
 val revision=DeviceData.revision;val scope=api.localScope(carId);val notes=remember(scope,revision){DeviceData.read("notes:"+scope)};var tag by remember{mutableStateOf("全部")};var onlyFavorites by remember{mutableStateOf(true)};var selected by remember{mutableStateOf<JSONObject?>(null)};val action=rememberServerAction()
 val rows=notes.keys().asSequence().mapNotNull{notes.optJSONObject(it)}.filter{(!onlyFavorites||it.optBoolean("favorite"))&&(tag=="全部"||it.optString("tag")==tag)}.sortedByDescending{it.optJSONObject("trip")?.optString("tripStartTime")}.toList()
 FullscreenDataPage("行程收藏与手记",onClose){Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){SmallSelectionMenu(tag,(listOf("全部")+tripNoteCategories).map{it to it}){tag=it};Spacer(Modifier.weight(1f));CinemaChoice(onlyFavorites,{onlyFavorites=!onlyFavorites},{Text("仅收藏")})};Text("备注与收藏仅保存在当前设备",fontSize=12.sp,color=muted)
 if(rows.isEmpty())Panel{Text("还没有符合条件的行程");Text("打开任意行程详情，即可收藏或添加标签、备注。",fontSize=13.sp,color=muted)}
 rows.forEach{note->val trip=note.optJSONObject("trip")?:return@forEach;TripTimelineCard(trip,false){action.run{val id=trip.opt("id")?:trip.opt("tripId")?:throw Exception("缺少行程编号");selected=api.call("/car/trip/getDetail",JSONObject().put("tripId",id)).optJSONObject("data")?:throw Exception("暂无行程详情")}};note.optString("note").takeIf{it.isNotBlank()}?.let{Text(privateProse(it),fontSize=13.sp,color=muted)}};ServerNotice(action);selected?.let{TripDetailPage(api,it){selected=null}}
 }
}
internal fun commonRouteKey(j:JSONObject):String?{val pair=recentTripAddresses(j);return if(pair.first.contains("暂无数据")||pair.second.isBlank()||pair.second.contains("暂无数据"))null else pair.first.trim()+" → "+pair.second.trim()}
@Composable internal fun RouteComparisonPage(api:Api,carId:Any,onClose:()->Unit){
 val scope=api.localScope(carId);val revision=DeviceData.revision;val index=remember(scope,revision){DeviceData.read("trips:"+scope)};val groups=index.keys().asSequence().mapNotNull{index.optJSONObject(it)}.groupBy{commonRouteKey(it)}.filter{it.key!=null&&it.value.size>=2}.entries.sortedByDescending{it.value.size}
 val action=rememberServerAction();var group by remember{mutableStateOf<List<JSONObject>?>(null)};var detail by remember{mutableStateOf<JSONObject?>(null)}
 var syncTime by remember{mutableLongStateOf(0L)}
 val reload:()->Unit={action.run{var page=1;var more=true;var loaded=0;while(more&&page<=10){val end=LocalDate.now();val raw=api.call("/car/trip/get",JSONObject().put("carId",carId).put("startDate",end.minusDays(29).toString()).put("endDate",end.toString()).put("pageNum",page).put("pageSize",50).put("orderBy","trip_start_time desc").put("sortOrder","desc"));val d=raw.optJSONObject("data");val count=(d?.optJSONArray("list")?:d?.optJSONArray("records")?:raw.optJSONArray("data"))?.length()?:0;syncTime=responseSyncTime(raw);loaded+=count;more=hasMoreTripRecords(raw,count,loaded,50);page++}}}
 FullscreenDataPage("常用路线对比",onClose,refreshing=action.busy,onRefresh=reload,lastSynced=syncTime){
 Text("按相同起终点分组；基于本机已加载的行程，最多保留500条。下拉可补读近30天记录。",fontSize=12.sp,color=muted);ServerNotice(action)
 if(groups.isEmpty())Panel{Text("暂未找到重复路线");Text("先查看行程记录或补读近30天数据，两次以上的相同路线会出现在这里。",fontSize=13.sp,color=muted)}
 groups.forEach{entry->Panel{val places=entry.key!!.split(" → ",limit=2);RoutePoint(privateAddress(places[0]),accent);RoutePoint(privateAddress(places[1]),muted);val duration=entry.value.mapNotNull{tripDuration(it)};val energy=entry.value.mapNotNull{it.number("tripEnergyConsumption")};Text("${entry.value.size} 次 · 平均 "+numberText(duration.takeIf{it.isNotEmpty()}?.average())+" 分钟",fontSize=16.sp);if(energy.isNotEmpty())Text("平均能耗 "+numberText(energy.average())+" kWh/100km",fontSize=13.sp,color=muted);CinemaTonalButton({group=entry.value}){Text("比较每次表现")}}}
 group?.let{trips->FullscreenDataPage("路线表现对比",{group=null}){val sorted=trips.sortedBy{it.optString("tripStartTime")};InteractiveTrend("行程耗时","分钟",sorted.mapNotNull{j->tripDuration(j)?.let{ChartSample(j.optString("tripStartTime").take(16),it)}});InteractiveTrend("行程能耗","kWh/100km",sorted.mapNotNull{j->j.number("tripEnergyConsumption")?.let{ChartSample(j.optString("tripStartTime").take(16),it)}});sorted.reversed().forEach{trip->TripTimelineCard(trip,false){action.run{val id=trip.opt("id")?:trip.opt("tripId")?:throw Exception("缺少编号");detail=api.call("/car/trip/getDetail",JSONObject().put("tripId",id)).optJSONObject("data")}}};ServerNotice(action);detail?.let{TripDetailPage(api,it){detail=null}}}}
 }
}
