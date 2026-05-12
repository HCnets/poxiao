import org.json.JSONObject

fun main() {
    val regex = Regex("`chart:radar\\s*\\n(.*?)\\n\\s*`", setOf(RegexOption.DOT_MATCHES_ALL))
    val text = """
    这是分析：
    `chart:radar
    {
      "数学": 0.8,
      "英语": 0.5
    }
    `
    结束
    """.trimIndent()
    val match = regex.find(text)
    println("Match: ")
}
