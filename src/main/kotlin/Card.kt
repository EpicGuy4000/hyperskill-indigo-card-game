package indigo

data class Card(val rank: Rank, val suit: Suit) {
    override fun toString(): String = "$rank$suit"
}