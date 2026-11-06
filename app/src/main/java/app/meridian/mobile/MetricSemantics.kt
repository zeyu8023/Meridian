package app.meridian.mobile

import androidx.compose.ui.graphics.Color

internal enum class MetricKind { Energy,Mileage,Charge,Time,Assistance,Safety,Cost,Other }
internal fun metricKind(label:String):MetricKind=when{
 label.contains("费用")||label.contains("支出")||label.contains("账单")->MetricKind.Cost
 label.contains("辅助")||label.contains("智驾")||label.contains("领航")->MetricKind.Assistance
 label.contains("安全")||label.contains("车况")->MetricKind.Safety
 label.contains("充电")||label.contains("补能")->MetricKind.Charge
 label.contains("能耗")||label.contains("电耗")||label.contains("耗电")->MetricKind.Energy
 label.contains("驾驶")||label.contains("时长")||label.contains("时间")||label.contains("天")->MetricKind.Time
 label.contains("里程")||label.contains("续航")||label.contains("行程")->MetricKind.Mileage
 else->MetricKind.Other
}
internal fun metricColor(label:String,palette:MeridianPalette):Color{
 val colors=themeChartColors(palette)
 val slot=when(metricKind(label)){MetricKind.Energy->0;MetricKind.Mileage->1;MetricKind.Charge->2;MetricKind.Time->3;MetricKind.Assistance->4;MetricKind.Safety->2;MetricKind.Cost->0;MetricKind.Other->4}
 return colors[slot%colors.size]
}
