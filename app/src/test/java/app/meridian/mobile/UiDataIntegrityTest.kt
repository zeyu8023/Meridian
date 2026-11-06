package app.meridian.mobile
import org.json.*
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

class UiDataIntegrityTest{
 @Test fun dailyTripsRequestNeverFallsBackToAllDates(){
  val date=LocalDate.of(2026,11,4)
  val spec=recordReadSpec(42,false,3,"desc","","all","",-1,date to date)
  assertEquals("/car/trip/get",spec.path)
  assertEquals(date.toString(),spec.body.getString("startDate"))
  assertEquals(date.toString(),spec.body.getString("endDate"))
  assertEquals(3,spec.body.getInt("pageNum"))
 }
 @Test fun nullPrimaryTripIdFallsBackToTripId(){
  assertEquals("123" to "42",notebookIdentity(JSONObject().put("id",JSONObject.NULL).put("tripId",123).put("carId",JSONObject.NULL),"42"))
 }
 @Test fun emptyIdentifiersDoNotCreateUnreachableNotes(){
  assertNull(notebookIdentity(JSONObject().put("id"," "),"42"))
  assertNull(notebookIdentity(JSONObject().put("id",1),""))
 }
 @Test fun specificMetricMeaningWinsOverGenericDrivingAndMileage(){
  assertEquals(MetricKind.Assistance,metricKind("辅助驾驶里程"))
  assertEquals(MetricKind.Time,metricKind("驾驶时长"))
  assertEquals(MetricKind.Charge,metricKind("累计充电时长"))
  assertEquals(MetricKind.Cost,metricKind("充电费用"))
  assertEquals(MetricKind.Time,metricKind("行程时长"))
 }
 @Test fun brightChartColorsRemainReadableAsTextInBothModes(){
  for(background in listOf(Color.White,Color(0xFF202125)))for(tint in listOf(Color.Yellow,Color(0xFFFF6A00),Color(0xFF168BFF))){
   val foreground=readableTint(tint,background)
   val contrast=(maxOf(foreground.luminance(),background.luminance())+.05f)/(minOf(foreground.luminance(),background.luminance())+.05f)
   assertTrue(contrast>=4.5f)
  }
 }
}
