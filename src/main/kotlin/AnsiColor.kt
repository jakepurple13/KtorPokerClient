object AnsiColor {
    const val prefix = "\u001B"
    const val RESET = "$prefix[0m"
    private val isCompatible = !System.getProperty("os.name")?.toLowerCase()?.contains("win")!!
    fun getColor(r: Int, g: Int, b: Int) = "[38;2;$r;$g;$b"
    fun regularColor(r: Int, g: Int, b: Int) = if (isCompatible) "$prefix${getColor(r, g, b)}m" else ""
    fun regularColor(color: Int) = color.valueOf().let { regularColor(it.first, it.second, it.third) }
    fun colorText(s: String, color: Int) = "${regularColor(color)}$s$RESET"
    fun colorText(s: String, r: Int, g: Int, b: Int) = "${regularColor(r, g, b)}$s$RESET"
    private fun Int.valueOf(): Triple<Int, Int, Int> {
        val r = (this shr 16 and 0xff)// / 255.0f
        val g = (this shr 8 and 0xff)// / 255.0f
        val b = (this and 0xff)// / 255.0f
        return Triple(r, g, b)
    }
}

fun String.color(color: Int) = AnsiColor.colorText(this, color)
fun String.color(r: Int, g: Int, b: Int) = AnsiColor.colorText(this, r, g, b)