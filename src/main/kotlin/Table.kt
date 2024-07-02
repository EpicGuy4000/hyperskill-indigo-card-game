package indigo

object Table {
    val cards = mutableListOf<Card>()
    var lastPlayerThatWon: Player? = null

    fun checkIfWon(player: Player) {
        if (cards.size < 2) return

        val (lastCard, playedCard) = Pair(cards[cards.lastIndex - 1], cards.last())

        if (lastCard.suit == playedCard.suit || lastCard.rank == playedCard.rank) {
            player.wonCards.addAll(cards)
            cards.clear()
            println("${player.name} wins cards")
            Game.printScore()
            lastPlayerThatWon = player
        }
    }

    override fun toString(): String = if (cards.size > 0) "\n${cards.size} cards on the table, and the top card is ${cards.last()}"
        else "\nNo cards on the table"
}