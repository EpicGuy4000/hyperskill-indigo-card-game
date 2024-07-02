package indigo

object Game {
    private val turns = mutableListOf<TurnTaker>(Dealer)
    private var turnNumber = 0
    private var startingPlayer: Player = Person
    private var turnCount = 0

    fun start(startingPlayer: Player) {
        this.startingPlayer = startingPlayer
        turns.add(startingPlayer)
        if (startingPlayer !== Person) turns.add(Person)
        else turns.add(Computer)

        while (!isOver()) {
            nextTurn()
        }

        (Table.lastPlayerThatWon ?:startingPlayer).wonCards.addAll(Table.cards)
        Table.cards.clear()

        printScore(true)
        println("Game Over")
    }

    fun printScore(withBonusPoints: Boolean = false) {
        val personCards = Person.wonCards.size
        val computerCards = Computer.wonCards.size

        var personScore = Person.wonCards.sumOf { it.rank.value }
        var computerScore = Computer.wonCards.sumOf { it.rank.value }

        if (withBonusPoints) {
            if (personCards > computerCards) {
                personScore += 3
            } else if (computerCards > personCards) {
                computerScore += 3
            } else if (Person == startingPlayer) {
                personScore += 3
            } else {
                computerScore += 3
            }
        }

        val padLength = 1

        println("""
            Score: ${Person.name} ${personScore.toString().padStart(padLength, ' ')} - ${Computer.name} ${computerScore.toString().padStart(padLength, ' ')}
            Cards: ${Person.name} ${personCards.toString().padStart(padLength, ' ')} - ${Computer.name} ${computerCards.toString().padStart(padLength, ' ')}
        """.trimIndent().trim())
    }

    private fun nextTurn() {
        val nextPlayer = turns[turnNumber % turns.size]
        turnNumber++

        if (nextPlayer === Dealer && (Person.hand.size != 0 || Computer.hand.size != 0)) return
        nextPlayer.takeTurn()
        turnCount++
    }

    private fun isOver() = turnCount == 52
}