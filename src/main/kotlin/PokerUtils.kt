import io.ktor.http.cio.websocket.Frame
import java.awt.Color

fun getColorLevel(pokerHand: PokerHand?) = when (pokerHand) {
    PokerHand.ROYAL_FLUSH -> Color.decode("#D4AF37")
    PokerHand.STRAIGHT_FLUSH -> Color.decode("#C0C0C0")
    PokerHand.FOUR_KIND -> Color.decode("#b08d57")
    PokerHand.FULL_HOUSE -> Color.ORANGE
    PokerHand.FLUSH -> Color.RED
    PokerHand.STRAIGHT -> Color.PINK
    PokerHand.THREE_KIND -> Color.MAGENTA
    PokerHand.TWO_PAIR -> Color.GREEN
    PokerHand.PAIR -> Color.YELLOW
    PokerHand.HIGH_CARD -> Color.WHITE
    else -> Color.WHITE
}.rgb

fun PokerHand.printHand(hand: List<Card>) = "You have: $stringName with ${hand.map { it.toSymbol() }}".color(getColorLevel(this))

fun Card.toSymbol() = "${symbol}${suit.unicodeSymbol}"
fun List<Card?>.toSymbol() = joinToString { if (it == null) "[]" else "${it.symbol}${it.suit.unicodeSymbol}".color(Color.CYAN.rgb) }

enum class Continue {
    YES, NO;

    companion object {
        operator fun invoke(s: String) = when (s.toUpperCase()) {
            "Y", "YES" -> YES
            "N", "NO" -> NO
            else -> null
        }
    }
}

enum class PokerPlay {
    CONTINUE, STOP;

    companion object {
        operator fun invoke(s: String) = when (s.toUpperCase()) {
            "STOP", "S" -> STOP
            "CONTINUE", "C" -> CONTINUE
            else -> null
        }
    }
}

enum class Type {
    DRAW_CARDS, GET_HAND, UPDATE, CHAT, SUBMIT_HAND, RENAME, ANTE, BET_MONEY, MONEY_CHECK
}

data class CardType(val type: Type, val any: Any) {
    inline fun <reified T> getAnyType() = any.toJson().fromJson<T>()
    fun toFrameJson() = Frame.Text(toJson())
    inline operator fun <reified T> invoke() = getAnyType<T>()
}

enum class PokerHand(val stringName: String, val defaultWinning: Int) {
    ROYAL_FLUSH("Royal Flush", 250),
    STRAIGHT_FLUSH("Straight Flush", 50),
    FOUR_KIND("Four of a Kind", 25),
    FULL_HOUSE("Full House", 9),
    FLUSH("Flush", 6),
    STRAIGHT("Straight", 4),
    THREE_KIND("Three of a Kind", 3),
    TWO_PAIR("Two Pair", 2),
    PAIR("Pair", 1),
    HIGH_CARD("High Card", 0)
}