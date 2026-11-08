package com.example.data.models

data class AppInfoData(
    val generalInfo: GeneralInfo,
    val poputka: PoputkaInfo,
    val updateDate: String = "Травень 2026"
)

data class GeneralInfo(
    val title: String,
    val busFares: List<InfoItem>,
    val privileges: List<InfoItem>
)

data class InfoItem(
    val icon: String,
    val text: String
)

data class PoputkaInfo(
    val title: String,
    val price: String,
    val routes: List<PoputkaRouteItem>,
    val links: List<PoputkaLinkItem>
)

data class PoputkaRouteItem(
    val city: String,
    val start: String,
    val end: String
)

data class PoputkaLinkItem(
    val name: String,
    val url: String,
    val iconType: String
)
