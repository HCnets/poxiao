package com.poxiao.app.ui

internal enum class HomeModule(
    val title: String,
) {
    Metrics("核心指标"),
    Rhythm("今天的节奏"),
    Learning("学习推进"),
    QuickPoints("快捷点位"),
    RecentPoints("最近访问"),
    Assistant("智能体"),
}

internal enum class HomeModuleSize(
    val title: String,
) {
    Compact("紧凑"),
    Standard("标准"),
    Hero("强调"),
}
