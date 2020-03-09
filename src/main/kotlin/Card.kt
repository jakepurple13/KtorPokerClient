enum class CardColor { BLACK, RED }
data class Card(val value: Int, val suit: Suit) {
    val valueTen: Int get() = if (value > 10) 10 else value
    val color: CardColor
        get() = when (suit) {
            Suit.SPADES, Suit.CLUBS -> CardColor.BLACK
            Suit.HEARTS, Suit.DIAMONDS -> CardColor.RED
        }
    val symbol: String
        get() = when (value) {
            13 -> "K"
            12 -> "Q"
            11 -> "J"
            1 -> "A"
            else -> "$value"
        }

    companion object {
        val RandomCard: Card get() = Card((1..13).random(), Suit.values().random())
        operator fun get(suit: Suit) = Card((1..13).random(), suit)
        operator fun get(vararg suit: Suit) = suit.map { Card((1..13).random(), it) }
        operator fun get(num: Int) = Card(num, Suit.values().random())
        operator fun get(vararg num: Int) = num.map { Card(it, Suit.values().random()) }
    }
}

enum class Suit(val printableName: String, val symbol: String, val unicodeSymbol: String) {
    SPADES("Spades", "S", "♠"),
    CLUBS("Clubs", "C", "♣"),
    DIAMONDS("Diamonds", "D", "♦"),
    HEARTS("Hearts", "H", "♥")
}