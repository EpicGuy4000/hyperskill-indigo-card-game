package indigo

object Person: Player {
    override val hand = mutableListOf<Card>()
    override val wonCards = mutableListOf<Card>()
    override val name = "Player"
    override fun takeTurn() {
        println("Cards in hand: ${hand.withIndex().joinToString(" ") { "${it.index + 1})${it.value}" }}")
        val chosenCard = chooseCard()

        hand.remove(chosenCard)
        Table.cards.add(chosenCard)
        Table.checkIfWon(this)
        println(Table)
    }

    private fun chooseCard(): Card {
        var chosenCardIndex: Int? = null

        while (chosenCardIndex == null || chosenCardIndex !in 1..hand.size) {
            println("Choose a card to play (1-${hand.size}):")
            val input = readln()
            if (input == "exit") throw GameOverException()
            chosenCardIndex = input.toIntOrNull()
        }

        return hand[chosenCardIndex - 1]
    }
}