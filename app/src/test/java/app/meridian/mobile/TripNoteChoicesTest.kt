package app.meridian.mobile
import org.json.*
import org.junit.Assert.*
import org.junit.Test

class TripNoteChoicesTest{
 @Test fun exclusiveRoadChoiceReplacesEarlierSelectionAndCanBeCleared(){
  val first=toggleTripNoteChoice(emptyList(),"一路畅通")
  val second=toggleTripNoteChoice(first,"有些拥堵")
  assertEquals(listOf("有些拥堵"),second)
  assertTrue(toggleTripNoteChoice(second,"有些拥堵").isEmpty())
 }
 @Test fun experienceChoicesCanBeCombinedWithoutDuplicates(){
  assertEquals(listOf("风景不错","听歌放松"),normalizedTripNoteChoices(listOf("听歌放松","风景不错","听歌放松","未知选项")))
 }
 @Test fun oldFreeTextSurvivesEditingAndChoicesDoNotAccumulateInExtra(){
  val old=JSONObject().put("note","旧手记内容")
  assertEquals("旧手记内容",tripNoteExtra(old))
  val saved=mergeTripNoteArchive(JSONObject().put("1",old),"1",JSONObject().put("id",1),text=composeTripNote(listOf("一路畅通"),tripNoteExtra(old)),choices=listOf("一路畅通"),extra=tripNoteExtra(old))
  val note=saved.getJSONObject("1")
  assertEquals("旧手记内容",tripNoteExtra(note))
  assertEquals(note.getString("note"),composeTripNote(readTripNoteChoices(note),tripNoteExtra(note)))
 }
 @Test fun favoriteDoesNotEraseChoicesAndClearingSelectionsIsPersisted(){
  val old=JSONObject().put("1",JSONObject().put("noteChoices",JSONArray().put("听歌放松")).put("noteExtra","补充内容"))
  val favorite=mergeTripNoteArchive(old,"1",JSONObject(),favorite=true)
  assertEquals(listOf("听歌放松"),readTripNoteChoices(favorite.getJSONObject("1")))
  val cleared=mergeTripNoteArchive(favorite,"1",JSONObject(),text="",choices=emptyList(),extra="").getJSONObject("1")
  assertTrue(readTripNoteChoices(cleared).isEmpty());assertEquals("",tripNoteExtra(cleared));assertTrue(cleared.getBoolean("favorite"))
 }
}
