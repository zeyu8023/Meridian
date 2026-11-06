package app.meridian.mobile

import org.json.*
import org.junit.Assert.*
import org.junit.Test

class MetricScopeTest {
 private fun group(key:String,value:Int)=JSONObject().put("key",key).put("title",key).put("items",JSONArray().put(JSONObject().put("label",key).put("value",value)))
 private fun dashboard(vararg values:JSONObject)=JSONObject().put("metricGroups",JSONArray(values.toList()))
 @Test fun matchingGroupNamesRetainBothScopesAndTheirValues(){
  val data=JSONObject().put("periodDashboard",dashboard(group("overview",1),group("trip",2),group("energy",3),group("efficiency",4))).put("lifetimeDashboard",dashboard(group("overview",11),group("trip",12),group("energy",13),group("efficiency",14)))
  val result=statisticsMetricGroups(data);assertEquals(8,result.size)
  assertEquals(11,result.first{it.key=="overview"&&it.scope=="lifetime"}.items.getJSONObject(0).getInt("value"));assertEquals(12,result.first{it.key=="trip"&&it.scope=="lifetime"}.items.getJSONObject(0).getInt("value"))
  assertEquals(3,result.first{it.key=="energy"&&it.scope=="period"}.items.getJSONObject(0).getInt("value"));assertEquals(4,result.first{it.key=="efficiency"&&it.scope=="period"}.items.getJSONObject(0).getInt("value"))
  assertEquals(1,result.first{it.key=="overview"&&it.scope=="period"}.items.getJSONObject(0).getInt("value"))
  assertEquals(13,result.first{it.key=="energy"&&it.scope=="lifetime"}.items.getJSONObject(0).getInt("value"))
 }
 @Test fun oldServersWithOnlyPeriodGroupsStillShowTheirMetrics(){
  val groups=statisticsMetricGroups(JSONObject().put("periodDashboard",dashboard(group("overview",1),group("trip",2))))
  assertEquals(2,groups.size)
 }
 @Test fun independentDockReserveScalesWithPhysicalSize(){
  for(page in listOf(.75f,1f,1.25f))for(nav in listOf(.8f,1f,1.4f)){
   val reserved=navigationBottomSpaceDp(true,14f,page,nav,false)*page
   val dock=(navigationRailHeightDp(true,14f,nav)+8f+32f)*nav
   assertTrue(reserved>=dock)
  }
 }
}
