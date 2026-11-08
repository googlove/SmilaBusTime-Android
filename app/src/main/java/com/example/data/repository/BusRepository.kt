package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.models.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

class BusRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("smilabus_prefs", Context.MODE_PRIVATE)

    private val cityRouteIds = setOf(
        "3", "4", "5", "17", "30", "32", "34", "39", "40", "41", "48", "49"
    )

    private val suburbanPrices = mapOf(
        "309" to Pair("30 ₴", null),
        "155" to Pair("30 ₴", null),
        "154" to Pair("від 40 ₴", null),
        "153" to Pair("від 47,5 ₴", "Перевізник: МЕРКУЛОВ В.Л. ФОП"),
        "152" to Pair("від 25 ₴", "Ціна по Смілі: 20 грн"),
        "151" to Pair("від 20 ₴", null),
        "150" to Pair("від 25-30 ₴", "До кінцевої: 30 грн. По Смілі: 20 грн."),
        "141" to Pair("від 25 ₴", null),
        "140" to Pair("від 45 ₴", null),
        "139" to Pair("від 25 ₴", "ч/з с. Холоднянське, М. Смілянка, Тернівка, Попівка. До с. Санжариха: 25 грн. По місту: 20 грн."),
        "137" to Pair("від 30 ₴", null),
        "136" to Pair("від 45 ₴", null),
        "129" to Pair("від 50 ₴", null),
        "126" to Pair("від 82 ₴", "ч/з с. Ротмістрівка, с. Ташлик, с. Самгородок"),
        "123" to Pair("від 50 ₴", "ч/з с. Ротмістрівка"),
        "302" to Pair("до Черкас: 120 ₴", "По Смілі: 20 грн | По Черкасам: 33 грн")
    )

    private var cachedRoutes: List<BusRoute>? = null
    private var cachedInfo: AppInfoData? = null

    fun getBusRoutes(): List<BusRoute> {
        cachedRoutes?.let { return it }
        val routes = mutableListOf<BusRoute>()
        try {
            val jsonString = context.assets.open("data.json").bufferedReader().use { it.readText() }
            val array = JSONArray(jsonString)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val id = obj.optString("id", "")
                val number = obj.optString("number", id)
                val title = obj.optString("title", "")
                val color = obj.optString("color", "#007DD6")
                val mapIframeSrc = obj.optString("mapIframeSrc").takeIf { it.isNotEmpty() }

                val routesList = mutableListOf<RouteDirection>()
                val routesArray = obj.optJSONArray("routes")
                if (routesArray != null) {
                    for (r in 0 until routesArray.length()) {
                        val routeObj = routesArray.getJSONObject(r)
                        val dir = routeObj.optString("direction", "")
                        val wd = routeObj.optString("workDays").takeIf { it.isNotEmpty() }

                        val stopsList = mutableListOf<StopTimes>()
                        val stopsArray = routeObj.optJSONArray("stops")
                        if (stopsArray != null) {
                            for (s in 0 until stopsArray.length()) {
                                val sObj = stopsArray.getJSONObject(s)
                                val sName = sObj.optString("name", "")
                                val timesList = mutableListOf<String>()
                                val timesArr = sObj.optJSONArray("times")
                                if (timesArr != null) {
                                    for (t in 0 until timesArr.length()) {
                                        timesList.add(timesArr.getString(t))
                                    }
                                }
                                stopsList.add(StopTimes(sName, timesList))
                            }
                        }
                        routesList.add(RouteDirection(dir, wd, stopsList))
                    }
                }

                var dirRoutesData: DirectionRoutesData? = null
                val dirObj = obj.optJSONObject("directionRoutes")
                if (dirObj != null) {
                    val fObj = dirObj.optJSONObject("forward")
                    val bObj = dirObj.optJSONObject("backward")
                    dirRoutesData = DirectionRoutesData(
                        forward = parseDirectionData(fObj),
                        backward = parseDirectionData(bObj)
                    )
                }

                val isUrban = cityRouteIds.contains(number)
                val (fare, fareNote) = if (isUrban) {
                    Pair("20 ₴", null)
                } else {
                    suburbanPrices[number] ?: Pair("Приміський", null)
                }

                routes.add(
                    BusRoute(
                        id = id,
                        number = number,
                        title = title,
                        color = color,
                        mapIframeSrc = mapIframeSrc,
                        routes = routesList,
                        directionRoutes = dirRoutesData,
                        isUrban = isUrban,
                        fare = fare,
                        fareNote = fareNote
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        cachedRoutes = routes
        return routes
    }

    private fun parseDirectionData(obj: JSONObject?): DirectionData? {
        if (obj == null) return null
        val name = obj.optString("name", "")
        val wd = obj.optString("workDays").takeIf { it.isNotEmpty() }
        val depList = mutableListOf<String>()
        val depArr = obj.optJSONArray("departureTimes")
        if (depArr != null) {
            for (i in 0 until depArr.length()) {
                depList.add(depArr.getString(i))
            }
        }
        val stopsList = mutableListOf<DirectionStop>()
        val stopsArr = obj.optJSONArray("stops")
        if (stopsArr != null) {
            for (i in 0 until stopsArr.length()) {
                val sObj = stopsArr.getJSONObject(i)
                stopsList.add(
                    DirectionStop(
                        name = sObj.optString("name", ""),
                        offsetMin = sObj.optInt("offsetMin", 0)
                    )
                )
            }
        }
        return DirectionData(name, wd, depList, stopsList)
    }

    fun getAppInfo(): AppInfoData {
        cachedInfo?.let { return it }
        try {
            val jsonString = context.assets.open("info.json").bufferedReader().use { it.readText() }
            val root = JSONObject(jsonString)

            val gObj = root.getJSONObject("generalInfo")
            val gTitle = gObj.optString("title", "Загальна інформація")
            val faresList = mutableListOf<InfoItem>()
            val faresArr = gObj.optJSONArray("busFares")
            if (faresArr != null) {
                for (i in 0 until faresArr.length()) {
                    val item = faresArr.getJSONObject(i)
                    faresList.add(InfoItem(item.optString("icon", "💰"), item.optString("text", "")))
                }
            }
            val privList = mutableListOf<InfoItem>()
            val privArr = gObj.optJSONArray("privileges")
            if (privArr != null) {
                for (i in 0 until privArr.length()) {
                    val item = privArr.getJSONObject(i)
                    privList.add(InfoItem(item.optString("icon", "🎖️"), item.optString("text", "")))
                }
            }
            val generalInfo = GeneralInfo(gTitle, faresList, privList)

            val pObj = root.getJSONObject("poputka")
            val pTitle = pObj.optString("title", "Попутка")
            val pPrice = pObj.optString("price", "100 грн")
            val routesList = mutableListOf<PoputkaRouteItem>()
            val rArr = pObj.optJSONArray("routes")
            if (rArr != null) {
                for (i in 0 until rArr.length()) {
                    val item = rArr.getJSONObject(i)
                    routesList.add(
                        PoputkaRouteItem(
                            city = item.optString("city", ""),
                            start = item.optString("start", ""),
                            end = item.optString("end", "")
                        )
                    )
                }
            }
            val linksList = mutableListOf<PoputkaLinkItem>()
            val lArr = pObj.optJSONArray("links")
            if (lArr != null) {
                for (i in 0 until lArr.length()) {
                    val item = lArr.getJSONObject(i)
                    linksList.add(
                        PoputkaLinkItem(
                            name = item.optString("name", ""),
                            url = item.optString("url", ""),
                            iconType = if (item.optString("icon").contains("viber")) "viber" else "telegram"
                        )
                    )
                }
            }
            val poputka = PoputkaInfo(pTitle, pPrice, routesList, linksList)
            val info = AppInfoData(generalInfo, poputka)
            cachedInfo = info
            return info
        } catch (e: Exception) {
            e.printStackTrace()
            return AppInfoData(
                GeneralInfo("Загальна інформація", emptyList(), emptyList()),
                PoputkaInfo("Попутка", "100 грн", emptyList(), emptyList())
            )
        }
    }

    // --- Favorites ---
    fun getFavorites(): Set<String> {
        return prefs.getStringSet("favorites", emptySet()) ?: emptySet()
    }

    fun isFavorite(busNumber: String): Boolean {
        return getFavorites().contains(busNumber)
    }

    fun toggleFavorite(busNumber: String): Boolean {
        val current = getFavorites().toMutableSet()
        val willBeFav = !current.contains(busNumber)
        if (willBeFav) current.add(busNumber) else current.remove(busNumber)
        prefs.edit().putStringSet("favorites", current).apply()
        return willBeFav
    }

    // --- Time helpers ---
    fun getCurrentMinutes(): Int {
        val c = Calendar.getInstance()
        return c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE)
    }

    fun parseTimeStr(timeStr: String?): Int? {
        if (timeStr.isNullOrBlank()) return null
        val clean = timeStr.trim().split(" ")[0]
        val parts = clean.split(":")
        if (parts.size < 2) return null
        val h = parts[0].toIntOrNull() ?: return null
        val m = parts[1].toIntOrNull() ?: return null
        return h * 60 + m
    }

    fun extractDayNote(timeStr: String): String? {
        val s = timeStr.trim()
        val openParen = s.indexOf('(')
        val closeParen = s.indexOf(')')
        if (openParen != -1 && closeParen != -1 && closeParen > openParen) {
            return s.substring(openParen + 1, closeParen).trim()
        }
        return null
    }

    fun noteMatchesToday(note: String?): Boolean {
        if (note == null) return true
        val low = note.lowercase()
        val c = Calendar.getInstance()
        // Day of week: Sunday = 1, Monday = 2, Tuesday = 3, ... Saturday = 7
        val dow = c.get(Calendar.DAY_OF_WEEK)
        val isWeekend = (dow == Calendar.SATURDAY || dow == Calendar.SUNDAY)
        val isWeekday = !isWeekend

        return when {
            low.contains("пн-пт") || low.contains("буд") -> isWeekday
            low.contains("сб-нд") || low.contains("вих") -> isWeekend
            low.contains("вт-пт") -> dow in Calendar.TUESDAY..Calendar.FRIDAY
            low.contains("сб") && !low.contains("нд") -> dow == Calendar.SATURDAY
            low.contains("нд") && !low.contains("сб") -> dow == Calendar.SUNDAY
            else -> true
        }
    }

    fun formatDiffMinutes(diff: Int): String {
        return when {
            diff <= 0 -> "зараз"
            diff < 60 -> "через $diff хв"
            else -> {
                val h = diff / 60
                val m = diff % 60
                if (m == 0) "через $h год" else "через $h год $m хв"
            }
        }
    }

    fun formatRide(min: Int): String {
        val h = min / 60
        val m = min % 60
        return when {
            h == 0 -> "$m хв"
            m == 0 -> "$h год"
            else -> "$h год $m хв"
        }
    }

    fun minToHHMM(minutes: Int): String {
        val m = ((minutes % 1440) + 1440) % 1440
        val h = m / 60
        val min = m % 60
        return "%02d:%02d".format(h, min)
    }

    fun findNextDeparture(bus: BusRoute): NextDepartureInfo? {
        val cur = getCurrentMinutes()
        var bestMinutes: Int? = null
        var bestTimeStr: String = ""
        var bestStop: String = ""
        var bestNote: String? = null

        for (route in bus.routes) {
            for (stop in route.stops) {
                for (timeStr in stop.times) {
                    val t = parseTimeStr(timeStr) ?: continue
                    val note = extractDayNote(timeStr)
                    if (!noteMatchesToday(note)) continue
                    if (t >= cur && (bestMinutes == null || t < bestMinutes)) {
                        bestMinutes = t
                        bestTimeStr = timeStr.split(" ")[0]
                        bestStop = stop.name
                        bestNote = note
                    }
                }
            }
        }

        return bestMinutes?.let {
            NextDepartureInfo(
                minutes = it,
                timeFormatted = bestTimeStr,
                stopName = bestStop,
                diffMinutes = it - cur,
                note = bestNote
            )
        }
    }

    fun getAllStopNames(): List<String> {
        val stops = linkedSetOf<String>()
        for (bus in getBusRoutes()) {
            bus.directionRoutes?.forward?.stops?.forEach { stops.add(it.name) }
            bus.directionRoutes?.backward?.stops?.forEach { stops.add(it.name) }
            bus.routes.forEach { r ->
                r.stops.forEach { s -> stops.add(s.name) }
            }
        }
        return stops.sorted()
    }

    fun normalizeStop(name: String): String {
        return name.lowercase()
            .replace("вул.", "")
            .replace("вулиця", "")
            .replace("зуп.", "")
            .replace("зупинка", "")
            .replace("«", "")
            .replace("»", "")
            .replace("\"", "")
            .trim()
    }

    fun planTrip(fromName: String, toName: String): List<TripPlanResult> {
        val nf = normalizeStop(fromName)
        val nt = normalizeStop(toName)
        val now = getCurrentMinutes()
        val results = mutableListOf<TripPlanResult>()
        val seen = mutableSetOf<String>()

        for (bus in getBusRoutes()) {
            val urban = bus.isUrban
            val rideCap = if (urban) 65 else 180

            // 1. directionRoutes
            bus.directionRoutes?.let { dr ->
                listOfNotNull(dr.forward, dr.backward).forEach { d ->
                    val names = d.stops.map { normalizeStop(it.name) }
                    val fi = names.indexOfFirst { it.contains(nf) || nf.contains(it) }
                    val ti = names.indexOfFirst { it.contains(nt) || nt.contains(it) }
                    if (fi != -1 && ti != -1 && fi < ti) {
                        val fOff = d.stops[fi].offsetMin
                        val tOff = d.stops[ti].offsetMin
                        val ride = tOff - fOff
                        for (depStr in d.departureTimes) {
                            val depMin = parseTimeStr(depStr) ?: continue
                            val note = extractDayNote(depStr)
                            val boardMin = depMin + fOff
                            val arriveMin = depMin + tOff
                            val key = "${bus.number}|$boardMin|$fi|$ti"
                            if (seen.add(key)) {
                                results.add(
                                    TripPlanResult(
                                        busNumber = bus.number,
                                        busTitle = bus.title,
                                        color = bus.color,
                                        dirName = d.name,
                                        urban = urban,
                                        fromName = d.stops[fi].name,
                                        toName = d.stops[ti].name,
                                        boardMin = boardMin,
                                        arriveMin = arriveMin,
                                        rideMin = ride,
                                        note = note,
                                        diff = boardMin - now,
                                        today = noteMatchesToday(note),
                                        bus = bus
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // 2. routes fallback
            for (route in bus.routes) {
                val names = route.stops.map { normalizeStop(it.name) }
                val fi = names.indexOfFirst { it.contains(nf) || nf.contains(it) }
                val ti = names.indexOfFirst { it.contains(nt) || nt.contains(it) }
                if (fi != -1 && ti != -1 && fi < ti) {
                    val fRaw = route.stops[fi].times
                    val tParsed = route.stops[ti].times.mapNotNull {
                        val m = parseTimeStr(it) ?: return@mapNotNull null
                        Pair(m, extractDayNote(it))
                    }.sortedBy { it.first }

                    val equalLen = fRaw.size == route.stops[ti].times.size
                    fRaw.forEachIndexed { idx, ft ->
                        val bMin = parseTimeStr(ft) ?: return@forEachIndexed
                        val note = extractDayNote(ft)
                        var aMin: Int? = null
                        var ride: Int? = null

                        if (equalLen && idx < route.stops[ti].times.size) {
                            val cand = parseTimeStr(route.stops[ti].times[idx])
                            if (cand != null && cand >= bMin) {
                                aMin = cand
                                ride = cand - bMin
                            }
                        }
                        if (aMin == null) {
                            val cand = tParsed.firstOrNull { it.first >= bMin }
                            if (cand != null && (cand.first - bMin) <= rideCap) {
                                aMin = cand.first
                                ride = cand.first - bMin
                            }
                        }

                        val key = "${bus.number}|$bMin|${route.stops[fi].name}|${route.stops[ti].name}"
                        if (seen.add(key)) {
                            results.add(
                                TripPlanResult(
                                    busNumber = bus.number,
                                    busTitle = bus.title,
                                    color = bus.color,
                                    dirName = route.direction,
                                    urban = urban,
                                    fromName = route.stops[fi].name,
                                    toName = route.stops[ti].name,
                                    boardMin = bMin,
                                    arriveMin = aMin,
                                    rideMin = ride,
                                    note = note,
                                    diff = bMin - now,
                                    today = noteMatchesToday(note),
                                    bus = bus
                                )
                            )
                        }
                    }
                }
            }
        }

        // Sort: first upcoming today, then past today, then other days
        results.sortWith { a, b ->
            val aUpcomingToday = a.today && a.diff >= 0
            val bUpcomingToday = b.today && b.diff >= 0
            when {
                aUpcomingToday && !bUpcomingToday -> -1
                !aUpcomingToday && bUpcomingToday -> 1
                aUpcomingToday && bUpcomingToday -> a.diff.compareTo(b.diff)
                a.today && !b.today -> -1
                !a.today && b.today -> 1
                else -> a.boardMin.compareTo(b.boardMin)
            }
        }

        return results
    }
}
