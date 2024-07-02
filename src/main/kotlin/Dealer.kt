package indigo

object Dealer: TurnTaker {
    private const val CARDS_ON_INIT = 4
    private const val CARDS_PER_TURN = 6

    private val newDeck = buildList {
        for (rank in Rank.entries) {
            for (suit in Suit.entries) {
                add(Card(rank, suit))
            }
        }
    }

    override var hand = mutableListOf<Card>()

    override fun takeTurn() = if (hand.size == 0) newGame() else deal()

    private fun newGame() {
        Person.hand.clear()
        Computer.hand.clear()
        Table.cards.clear()
        hand.clear()
        hand.addAll(newDeck.shuffled())

        Table.cards.addAll(hand.take(CARDS_ON_INIT))
        hand = hand.subList(CARDS_ON_INIT, hand.size)
        deal()

        println("Initial cards on the table: ${Table.cards.joinToString(" ")}")
        println(Table)
    }

    private fun deal() {
        dealHand(Person)
        dealHand(Computer)
    }

    private fun dealHand(player: Player) {
        player.hand.addAll(hand.take(CARDS_PER_TURN))
        hand = hand.subList(CARDS_PER_TURN, hand.size)
    }
}