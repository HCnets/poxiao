
package com.poxiao.app.schedule

import android.util.Base64
import com.poxiao.app.data.FeedCard
import java.io.IOException
import java.math.BigInteger
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.RSAPublicKeySpec
import java.security.spec.X509EncodedKeySpec
import java.time.LocalDate
import java.util.concurrent.ConcurrentHashMap
import javax.crypto.Cipher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

private class HitaCookieJar : CookieJar {
    private val store = ConcurrentHashMap<String, MutableList<Cookie>>()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val bucket = store.getOrPut(url.host) { mutableListOf() }
        cookies.forEach { incoming ->
            bucket.removeAll { it.name == incoming.name && it.matches(url) }
            bucket.add(incoming)
        }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return store[url.host]?.filter { it.matches(url) }.orEmpty()
    }

    fun currentValue(name: String, host: String): String {
        return store[host]?.firstOrNull { it.name == name }?.value.orEmpty()
    }
}

class HitaScheduleGatewayImpl : HitaScheduleGateway {
    private val cookieJar = HitaCookieJar()
    private val client = OkHttpClient.Builder().cookieJar(cookieJar).build()
    private val jsonType = "application/json; charset=utf-8".toMediaType()
    private var lastRsaBody: String = ""

    private fun buildRequest(
        path: String,
        authorization: String,
        rolecode: String,
        body: RequestBody,
        contentType: String? = null,
    ): Request {
        val builder = Request.Builder()
            .url(HitaApiGuide.BaseUrl + path)
            .post(body)
            .header("authorization", authorization)
            .header("rolecode", rolecode)
            .header("_lang", "cn")
            .header("Accept", "*/*")
            .header("Connection", "keep-alive")
            .header(
                "User-Agent",
                "Mozilla/5.0 (Linux; Android 15) AppleWebKit/537.36 Chrome/144.0 Mobile Safari/537.36",
            )
        if (contentType != null) {
            builder.header("Content-Type", contentType)
        }
        return builder.build()
    }

    private suspend fun execute(request: Request): String = withContext(Dispatchers.IO) {
        client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful && body.isBlank()) {
                throw IOException("HTTP ${response.code}")
            }
            body
        }
    }

    override suspend fun initializeRoute() {
        val request = buildRequest(
            path = HitaApiGuide.InitRoute,
            authorization = "Basic aW5jb246MTIzNDU=",
            rolecode = "01",
            body = FormBody.Builder().build(),
            contentType = "application/x-www-form-urlencoded",
        )
        execute(request)
    }

    override suspend fun fetchRsaKey() {
        val request = buildRequest(
            path = HitaApiGuide.FetchRsa,
            authorization = "Basic aW5jb246MTIzNDU=",
            rolecode = "06",
            body = FormBody.Builder().build(),
            contentType = "application/x-www-form-urlencoded",
        )
        lastRsaBody = execute(request)
    }

    override suspend fun login(studentId: String, password: String): HitaAuthSession {
        initializeRoute()
        fetchRsaKey()

        var lastError = "登录失败，请检查账号或密码。"
        val encryptedCandidates = parseRsaPublicKeys(lastRsaBody).mapNotNull { encryptPassword(password, it) }
        for (candidate in listOf(password) + encryptedCandidates) {
            for (rolecode in listOf("01", "06")) {
                val body = FormBody.Builder()
                    .add("username", studentId)
                    .add("password", candidate)
                    .build()
                val request = buildRequest(
                    path = HitaApiGuide.LdapLogin,
                    authorization = "Basic aW5jb246MTIzNDU=",
                    rolecode = rolecode,
                    body = body,
                    contentType = "application/x-www-form-urlencoded",
                )
                val response = execute(request)
                val json = JSONObject(response)
                val token = json.optString("access_token")
                if (token.isBlank()) {
                    lastError = json.optString("msg").ifBlank { lastError }
                    continue
                }
                val info = json.optJSONObject("info")
                val data = json.optJSONObject("data")
                return HitaAuthSession(
                    accessToken = token,
                    refreshToken = json.optString("refresh_token"),
                    routeCookie = cookieJar.currentValue("route", "mjw.hitsz.edu.cn"),
                    jsessionId = cookieJar.currentValue("JSESSIONID", "mjw.hitsz.edu.cn"),
                    studentId = info?.optString("yhdm").orEmpty().ifBlank { studentId },
                    studentName = data?.optString("yhxm").orEmpty(),
                    studentType = data?.optString("pylx").orEmpty().ifBlank { "1" },
                )
            }
        }
        throw IOException(lastError)
    }

    override suspend fun fetchTerms(session: HitaAuthSession): List<HitaTerm> {
        val request = buildRequest(
            path = HitaApiGuide.QueryTerms,
            authorization = "bearer ${session.accessToken}",
            rolecode = "06",
            body = FormBody.Builder().build(),
            contentType = "application/x-www-form-urlencoded",
        )
        val body = execute(request)
        val array = JSONObject(body).optJSONArray("content") ?: return emptyList()
        return buildList {
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue
                add(
                    HitaTerm(
                        year = item.optString("XN"),
                        term = item.optString("XQ"),
                        name = item.optString("XNXQMC"),
                        isCurrent = item.optString("SFDQXQ") == "1",
                    ),
                )
            }
        }
    }

    override suspend fun fetchWeeks(session: HitaAuthSession, term: HitaTerm): List<HitaWeek> {
        val body = JSONObject().put("xn", term.year).put("xq", term.term).toString()
        val request = buildRequest(
            path = HitaApiGuide.QueryWeeks,
            authorization = "bearer ${session.accessToken}",
            rolecode = "06",
            body = body.toRequestBody(jsonType),
            contentType = "application/json",
        )
        val response = execute(request)
        val array = JSONObject(response).optJSONArray("content") ?: return emptyList()
        return buildList {
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue
                add(HitaWeek(item.optInt("ZC"), item.optString("ZCMC")))
            }
        }
    }
    override suspend fun fetchDaySchedule(session: HitaAuthSession, date: String): List<HitaCourseBlock> {
        val body = FormBody.Builder()
            .add("nyr", date)
            .build()
        val request = buildRequest(
            path = HitaApiGuide.QueryDayCourses,
            authorization = "bearer ${session.accessToken}",
            rolecode = "06",
            body = body,
            contentType = "application/x-www-form-urlencoded",
        )
        val response = execute(request)
        val root = JSONObject(response)
        val teacherMap = fetchTeacherMap(session, null)
        val dayOfWeek = resolveDayOfWeek(date)
        val content = root.optJSONArray("content") ?: return emptyList()
        return buildList {
            for (index in 0 until content.length()) {
                val section = content.optJSONObject(index) ?: continue
                val majorIndex = section.optInt("DJ", index + 1)
                val courses = section.optJSONArray("kbrc") ?: continue
                for (courseIndex in 0 until courses.length()) {
                    val course = courses.optJSONObject(courseIndex) ?: continue
                    val name = course.optString("KCMC").ifBlank { course.optString("KCMC_EN") }
                    if (name.isBlank()) continue
                    add(
                        HitaCourseBlock(
                            courseName = name,
                            classroom = course.optString("CDMC"),
                            teacher = teacherMap[name].orEmpty(),
                            dayOfWeek = dayOfWeek,
                            majorIndex = majorIndex,
                            accent = accentFor(name),
                        ),
                    )
                }
            }
        }
    }

    override suspend fun fetchWeekSchedule(
        session: HitaAuthSession,
        term: HitaTerm,
        week: HitaWeek,
    ): HitaWeekSchedule {
        val body = JSONObject()
            .put("xn", term.year)
            .put("xq", term.term)
            .put("zc", week.index.toString())
            .put("type", "json")
            .toString()
        val request = buildRequest(
            path = HitaApiGuide.QueryWeekMatrix,
            authorization = "bearer ${session.accessToken}",
            rolecode = "06",
            body = body.toRequestBody(jsonType),
            contentType = "application/json",
        )
        val response = execute(request)
        val root = JSONObject(response)
        val teacherMap = fetchTeacherMap(session, term)
        val content = root.optJSONArray("content") ?: JSONArray()
        val parsedDays = parseDays(content)
        val parsedSlots = parseSlots(content)
        val parsedCourses = parseCourseObjects(content, teacherMap)

        return HitaWeekSchedule(
            term = term,
            week = week,
            days = parsedDays.ifEmpty { buildFallbackDays(week) },
            timeSlots = parsedSlots.ifEmpty { HitaSchedulePreview.schedule.timeSlots },
            courses = parsedCourses.ifEmpty { HitaSchedulePreview.schedule.courses },
        )
    }

    override suspend fun fetchGrades(session: HitaAuthSession, term: HitaTerm): List<FeedCard> {
        val body = JSONObject()
            .put("xn", term.year)
            .put("xq", term.term)
            .put("qzqmFlag", "qm")
            .put("type", "json")
            .toString()
        val request = buildRequest(
            path = HitaApiGuide.QueryGrades,
            authorization = "bearer ${session.accessToken}",
            rolecode = "06",
            body = body.toRequestBody(jsonType),
            contentType = "application/json",
        )
        val response = execute(request)
        val content = JSONObject(response).optJSONArray("content") ?: return emptyList()
        return buildList {
            for (index in 0 until content.length()) {
                val item = content.optJSONObject(index) ?: continue
                val courseName = stringOf(item, "kcmc", "KCMC")
                if (courseName.isBlank()) continue
                val score = stringOf(item, "zf", "ZF").ifBlank { "--" }
                val credits = stringOf(item, "xf", "XF").ifBlank { "--" }
                val assess = stringOf(item, "khfs", "KHFS")
                val gradePoint = stringOf(item, "jd", "JD")
                val usual = stringOf(item, "pscj", "PSCJ")
                val mid = stringOf(item, "qzcj", "QZCJ")
                val finalExam = stringOf(item, "qmcj", "QMCJ")
                val courseType = stringOf(item, "kcxzmc", "KCXZMC")
                val college = stringOf(item, "kkxymc", "KKXYMC")
                val scoreStatus = gradeStatus(score)
                add(
                    FeedCard(
                        id = stringOf(item, "id", "ID", "kcdm", "KCDM").ifBlank { "grade-$index" },
                        title = courseName,
                        source = buildString {
                            append(scoreStatus).append(" · 总评 ").append(score)
                            if (gradePoint.isNotBlank()) append(" · 绩点 ").append(gradePoint)
                        },
                        description = buildString {
                            append("学分 ").append(credits)
                            if (assess.isNotBlank()) append(" · ").append(assess)
                            if (courseType.isNotBlank()) append(" · ").append(courseType)
                            if (college.isNotBlank()) append(" · ").append(college)
                            if (usual.isNotBlank()) append("\n平时 ").append(usual)
                            if (mid.isNotBlank()) append(" · 期中 ").append(mid)
                            if (finalExam.isNotBlank()) append(" · 期末 ").append(finalExam)
                        },
                    ),
                )
            }
        }
    }

    override suspend fun fetchTeachingBuildings(session: HitaAuthSession): List<FeedCard> {
        val buildingRequest = buildRequest(
            path = HitaApiGuide.QueryBuildings,
            authorization = "bearer ${session.accessToken}",
            rolecode = "06",
            body = FormBody.Builder().build(),
            contentType = "application/x-www-form-urlencoded",
        )
        val buildingResponse = execute(buildingRequest)
        val buildings = JSONObject(buildingResponse).optJSONArray("content") ?: return emptyList()
        return buildList {
            for (index in 0 until buildings.length()) {
                val building = buildings.optJSONObject(index) ?: continue
                val buildingId = stringOf(building, "DM")
                val buildingName = stringOf(building, "MC").ifBlank { "教学楼" }
                if (buildingId.isBlank()) continue
                add(
                    FeedCard(
                        id = buildingId,
                        title = buildingName,
                        source = "教学楼栋",
                        description = "按日期查看该楼栋空教室",
                    ),
                )
            }
        }
    }

    override suspend fun fetchEmptyClassrooms(
        session: HitaAuthSession,
        date: String,
        buildingId: String,
    ): List<FeedCard> {
        val targetDate = normalizeDate(date)
        val buildings = fetchTeachingBuildings(session)
        val buildingName = buildings.firstOrNull { it.id == buildingId }?.title ?: "教学楼"
        val request = buildRequest(
            path = HitaApiGuide.QueryEmptyClassrooms,
            authorization = "bearer ${session.accessToken}",
            rolecode = "06",
            body = FormBody.Builder()
                .add("nyr", targetDate)
                .add("jxl", buildingId)
                .build(),
            contentType = "application/x-www-form-urlencoded",
        )
        val response = execute(request)
        val content = JSONObject(response).optJSONArray("content") ?: return emptyList()
        return buildList {
            for (roomIndex in 0 until content.length()) {
                val room = content.optJSONObject(roomIndex) ?: continue
                val roomName = stringOf(room, "CDMC")
                if (roomName.isBlank()) continue
                val freeSlots = buildList {
                    for (slot in 1..6) {
                        if (stringOf(room, "DJ$slot") == "0") add(emptyClassroomSlotLabel(slot))
                    }
                }
                if (freeSlots.isEmpty()) continue
                add(
                    FeedCard(
                        id = "$buildingId-$roomName",
                        title = roomName,
                        source = buildingName,
                        description = "空闲时段 ${freeSlots.joinToString("、")}",
                    ),
                )
            }
        }
    }

    private fun gradeStatus(score: String): String {
        val numeric = score.toDoubleOrNull()
        if (numeric != null) {
            return when {
                numeric >= 90 -> "优秀"
                numeric >= 60 -> "通过"
                else -> "预警"
            }
        }
        return when {
            score.contains("优") -> "优秀"
            score.contains("合格") || score.contains("通过") -> "通过"
            score.isBlank() || score == "--" -> "待出分"
            else -> "结果"
        }
    }

    private fun emptyClassroomSlotLabel(slot: Int): String {
        return when (slot) {
            1 -> "第1大节 08:00-09:45"
            2 -> "第2大节 10:00-11:45"
            3 -> "第3大节 14:00-15:45"
            4 -> "第4大节 16:00-17:45"
            5 -> "第5大节 19:00-20:45"
            6 -> "第6大节 20:45-22:30"
            else -> "第${slot}大节"
        }
    }

    private fun parseDays(content: JSONArray): List<HitaWeekDay> {
        val result = linkedMapOf<Int, HitaWeekDay>()
        for (index in 0 until content.length()) {
            val block = content.optJSONObject(index) ?: continue
            val days = block.optJSONArray("rqList") ?: continue
            for (dayIndex in 0 until days.length()) {
                val item = days.optJSONObject(dayIndex) ?: continue
                val dayOfWeek = item.optInt("XQDM", 0)
                val date = item.optString("RQ")
                if (dayOfWeek == 0 || date.isBlank()) continue
                result[dayOfWeek] = HitaWeekDay(
                    weekDay = dayOfWeek,
                    label = item.optString("XQMC").ifBlank { weekdayLabel(dayOfWeek) },
                    date = date.takeLast(5),
                    fullDate = date,
                )
            }
        }
        return result.values.sortedBy { it.weekDay }
    }

    private fun parseSlots(content: JSONArray): List<HitaTimeSlot> {
        val result = linkedMapOf<Int, HitaTimeSlot>()
        for (index in 0 until content.length()) {
            val block = content.optJSONObject(index) ?: continue
            val slots = block.optJSONArray("jcList") ?: continue
            for (slotIndex in 0 until slots.length()) {
                val item = slots.optJSONObject(slotIndex) ?: continue
                val major = item.optInt("DJ", 0)
                val start = item.optInt("KSJC", 0)
                val end = item.optInt("JSJC", start)
                if (major == 0 || start == 0) continue
                result[major] = HitaTimeSlot(
                    majorIndex = major,
                    label = "第 $start-$end 节",
                    timeRange = item.optString("SJ").ifBlank { buildTimeRange(item) },
                    startSection = start,
                    endSection = end,
                )
            }
        }
        return result.values.sortedBy { it.majorIndex }
    }

    private fun parseCourseObjects(content: JSONArray, teacherMap: Map<String, String>): List<HitaCourseBlock> {
        val result = mutableListOf<HitaCourseBlock>()
        for (index in 0 until content.length()) {
            val block = content.optJSONObject(index) ?: continue
            val courses = block.optJSONArray("kcxxList") ?: continue
            for (courseIndex in 0 until courses.length()) {
                val item = courses.optJSONObject(courseIndex) ?: continue
                val name = item.optString("KCMC").ifBlank {
                    item.optString("KBXX").lineSequence().firstOrNull().orEmpty().substringBefore("[")
                }.trim()
                if (name.isBlank()) continue
                val classroom = extractClassroom(item.optString("KBXX"))
                result += HitaCourseBlock(
                    courseName = name,
                    classroom = classroom,
                    teacher = teacherMap[item.optString("KCDM")].orEmpty().ifBlank { teacherMap[name].orEmpty() },
                    dayOfWeek = item.optInt("XQJ", 0),
                    majorIndex = item.optInt("DJ", 1),
                    accent = accentFor(name),
                )
            }
        }
        return result.distinctBy { "${it.courseName}-${it.dayOfWeek}-${it.majorIndex}-${it.classroom}" }
    }
    private suspend fun fetchTeacherMap(
        session: HitaAuthSession,
        term: HitaTerm?,
    ): Map<String, String> {
        val body = JSONObject().apply {
            if (term != null) {
                put("RoleCode", "01")
                put("p_pylx", session.studentType.ifBlank { "1" })
                put("p_xn", term.year)
                put("p_xq", term.term)
                put("p_xnxq", term.year + term.term)
                put("p_gjz", "")
                put("p_kc_gjz", "")
                put("p_xkfsdm", "yixuan")
            }
        }.toString()
        val request = buildRequest(
            path = HitaApiGuide.QuerySelectedCourses,
            authorization = "bearer ${session.accessToken}",
            rolecode = "06",
            body = body.toRequestBody(jsonType),
            contentType = "application/json",
        )
        return runCatching {
            val response = execute(request)
            val objects = collectObjects(JSONObject(response))
            buildMap {
                objects.forEach { item ->
                    val teacher = stringOf(item, "dgjsmc", "DGJSMC", "teacher", "JSXM")
                    if (teacher.isBlank()) return@forEach
                    val code = stringOf(item, "kcdm", "KCDM")
                    val name = stringOf(item, "kcmc", "KCMC", "courseName")
                    if (code.isNotBlank()) put(code, teacher)
                    if (name.isNotBlank()) put(name, teacher)
                }
            }
        }.getOrDefault(emptyMap())
    }

    private fun extractClassroom(text: String): String {
        if (text.isBlank()) return ""
        val match = Regex("\\[(.*?)]").find(text)
        return match?.groupValues?.getOrNull(1).orEmpty()
    }

    private fun collectObjects(root: Any?): List<JSONObject> {
        val result = mutableListOf<JSONObject>()
        fun walk(node: Any?) {
            when (node) {
                is JSONObject -> {
                    result += node
                    val keys = node.keys()
                    while (keys.hasNext()) {
                        walk(node.opt(keys.next()))
                    }
                }
                is JSONArray -> {
                    for (index in 0 until node.length()) {
                        walk(node.opt(index))
                    }
                }
            }
        }
        walk(root)
        return result
    }

    private fun stringOf(item: JSONObject, vararg keys: String): String {
        keys.forEach { key ->
            val value = item.optString(key)
            if (value.isNotBlank() && value != "null") return value.trim()
        }
        return ""
    }

    private fun intOf(item: JSONObject, vararg keys: String): Int? {
        keys.forEach { key ->
            when (val value = item.opt(key)) {
                is Number -> return value.toInt()
                is String -> value.toIntOrNull()?.let { return it }
            }
        }
        return null
    }

    private fun normalizeDate(raw: String): String {
        return runCatching { LocalDate.parse(raw).toString() }.getOrElse {
            val compact = raw.replace("/", "-")
            if (compact.length == 5) "${LocalDate.now().year}-$compact" else compact
        }
    }

    private fun weekdayLabel(dayOfWeek: Int): String {
        return when (dayOfWeek) {
            1 -> "周一"
            2 -> "周二"
            3 -> "周三"
            4 -> "周四"
            5 -> "周五"
            6 -> "周六"
            7 -> "周日"
            else -> "周?"
        }
    }

    private fun buildTimeRange(item: JSONObject): String {
        val startTime = stringOf(item, "KSSJ", "kssj", "startTime")
        val endTime = stringOf(item, "JSSJ", "jssj", "endTime")
        return if (startTime.isNotBlank() && endTime.isNotBlank()) "$startTime - $endTime" else "时间待定"
    }

    private fun buildFallbackDays(week: HitaWeek): List<HitaWeekDay> {
        val monday = LocalDate.now().minusDays(((LocalDate.now().dayOfWeek.value + 6) % 7).toLong())
        return (0..6).map { offset ->
            val date = monday.plusDays(offset.toLong())
            HitaWeekDay(
                weekDay = offset + 1,
                label = weekdayLabel(offset + 1),
                date = date.toString().takeLast(5),
                fullDate = date.toString(),
            )
        }
    }

    private fun resolveDayOfWeek(date: String): Int {
        return runCatching { LocalDate.parse(date).dayOfWeek.value }.getOrDefault(1)
    }

    private fun accentFor(name: String): Long {
        val value = kotlin.math.abs(name.hashCode()) % 5
        return when (value) {
            0 -> 0xFF4E8B68
            1 -> 0xFF7FAF74
            2 -> 0xFFC6A45A
            3 -> 0xFF5E9D7A
            else -> 0xFF8EB76D
        }
    }

    private fun parseRsaPublicKeys(body: String): List<PublicKey> {
        if (body.isBlank()) return emptyList()
        val json = JSONObject(body)
        val keys = mutableListOf<PublicKey>()
        val encoded = json.optString("CLIENT_RSA_EXPONENT")
        if (encoded.isNotBlank()) {
            runCatching {
                val keyBytes = Base64.decode(encoded, Base64.DEFAULT)
                val keySpec = X509EncodedKeySpec(keyBytes)
                keys += KeyFactory.getInstance("RSA").generatePublic(keySpec)
            }
        }
        val modulus = parseBigInteger(json.optString("CLIENT_RSA_MODULUS"), preferHex = true)
        val exponent = parseBigInteger(json.optString("CLIENT_RSA_EXPONENT"), preferHex = true)
        if (modulus != null && exponent != null) {
            runCatching {
                keys += KeyFactory.getInstance("RSA").generatePublic(RSAPublicKeySpec(modulus, exponent))
            }
        }
        return keys
    }

    private fun parseBigInteger(raw: String, preferHex: Boolean): BigInteger? {
        val value = raw.trim()
        if (value.isEmpty()) return null
        return runCatching {
            when {
                value.startsWith("0x", ignoreCase = true) -> BigInteger(value.substring(2), 16)
                preferHex -> BigInteger(value, 16)
                else -> BigInteger(value)
            }
        }.getOrNull()
    }

    private fun encryptPassword(password: String, publicKey: PublicKey): String? {
        return runCatching {
            val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
            cipher.init(Cipher.ENCRYPT_MODE, publicKey)
            Base64.encodeToString(cipher.doFinal(password.toByteArray()), Base64.NO_WRAP)
        }.getOrNull()
    }
}
