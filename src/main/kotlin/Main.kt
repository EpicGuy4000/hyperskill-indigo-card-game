package indigo

enum class Rank(val symbol: String, val value: Int) {
    ACE("A", 1),
    TWO("2", 2),
    THREE("3", 3),
    FOUR("4", 4),
    FIVE("5", 5),
    SIX("6", 6),
    SEVEN("7", 7),
    EIGHT("8", 8),
    NINE("9", 9),
    TEN("10", 10),
    JACK("J", 12),
    QUEEN("Q", 13),
    KING("K", 14);

    override fun toString(): String = symbol
}

enum class Suit(val symbol: String) {
    DIAMONDS("♦"),
    HEARTS("♥"),
    SPADES("♠"),
    CLUBS("♣");

    override fun toString(): String = symbol
}

data class Card(val rank: Rank, val suit: Suit) {
    override fun toString(): String = "$rank$suit"
}

fun main() {
    println(Rank.entries.joinToString(" ") { it.symbol } )
    println(Suit.entries.joinToString(" ") { it.symbol } )

    val deck = buildList {
        for (rank in Rank.entries) {
            for (suit in Suit.entries) {
                add(Card(rank, suit))
            }
        }
    }

    println(deck.shuffled().joinToString(" "))
}