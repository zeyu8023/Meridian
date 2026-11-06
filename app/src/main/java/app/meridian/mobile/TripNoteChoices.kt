package app.meridian.mobile

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.*
import org.json.JSONObject

internal val tripNoteCategories=listOf("未分类","通勤","旅行","工作","接送","办事","其他")
internal data class TripNoteChoiceGroup(val title:String,val options:List<String>,val exclusive:Boolean=false)
internal val tripNoteChoiceGroups=listOf(
 TripNoteChoiceGroup("路况",listOf("一路畅通","路况一般","有些拥堵"),true),
 TripNoteChoiceGroup("天气",listOf("晴天出行","阴天出行","雨天出行","雪天出行"),true),
 TripNoteChoiceGroup("同行",listOf("独自出行","家人同行","朋友同行","同事同行"),true),
 TripNoteChoiceGroup("行程感受",listOf("驾驶轻松","风景不错","停车方便","停车较难","途中充电","听歌放松"))
)
internal fun normalizedTripNoteChoices(values:List<String>)=tripNoteChoiceGroups.flatMap{group->
 val selected=group.options.filter{it in values}
 if(group.exclusive)selected.take(1)else selected
}
internal fun toggleTripNoteChoice(values:List<String>,option:String):List<String>{
 val group=tripNoteChoiceGroups.firstOrNull{option in it.options}?:return values
 val next=if(option in values)values-option else (if(group.exclusive)values-group.options.toSet()else values)+option
 return normalizedTripNoteChoices(next)
}
internal fun readTripNoteChoices(note:JSONObject?)=normalizedTripNoteChoices(note?.optJSONArray("noteChoices")?.let{array->(0 until array.length()).map{array.optString(it)}}.orEmpty())
internal fun tripNoteExtra(note:JSONObject?)=if(note?.has("noteExtra")==true)note.optString("noteExtra")else note?.optString("note").orEmpty()
internal fun composeTripNote(choices:List<String>,extra:String)=listOf(normalizedTripNoteChoices(choices).joinToString(" · "),extra.trim().take(800)).filter{it.isNotBlank()}.joinToString("\n")

@OptIn(ExperimentalLayoutApi::class)
@Composable internal fun TripNoteQuickChoices(choices:List<String>,enabled:Boolean,onChange:(List<String>)->Unit){
 tripNoteChoiceGroups.forEach{group->
  Column(verticalArrangement=Arrangement.spacedBy(6.dp)){
   Text(group.title+if(group.exclusive)" · 单选，可取消"else" · 可多选",style=UiType.metadata,color=muted)
   FlowRow(horizontalArrangement=Arrangement.spacedBy(7.dp),verticalArrangement=Arrangement.spacedBy(6.dp)){
    group.options.forEach{option->NoteChoiceChip(option,option in choices,enabled){onChange(toggleTripNoteChoice(choices,option))}}
   }
  }
 }
}
@Composable internal fun NoteChoiceChip(label:String,selected:Boolean,enabled:Boolean=true,onClick:()->Unit){
 val shape=RoundedCornerShape(13.dp);val source=remember{MutableInteractionSource()}
 val tint=if(selected)readableTint(accent,glassWhite)else ink
 Box(Modifier.heightIn(min=44.dp).motionPress(source).clip(shape).toggleable(selected,enabled=enabled,role=Role.Checkbox,interactionSource=source,indication=null,onValueChange={onClick()}).then(if(!enabled)Modifier.background(softWash)else Modifier)){
  SharedGlassControl(shape,Modifier.matchParentSize(),if(selected)accent.copy(alpha=.16f)else androidx.compose.ui.graphics.Color.Transparent)
  Row(Modifier.border(if(selected)1.dp else .6.dp,if(selected)accent.copy(alpha=.5f)else glassEdge,shape).padding(horizontal=11.dp,vertical=11.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(5.dp)){
   if(selected)Icon(Icons.Outlined.Check,null,Modifier.size(14.dp),tint=tint)
   Text(label,style=UiType.metadata,color=tint.copy(alpha=if(enabled)1f else .4f))
  }
 }
}
