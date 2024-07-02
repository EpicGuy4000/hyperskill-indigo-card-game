package indigo

object Computer: Player {
    override val wonCards = mutableListOf<Card>()
    override val hand = mutableListOf<Card>()
    override val name = "Computer"
    override fun takeTurn() {
        println(hand.withIndex().joinToString(" ") { it.value.toString() })

        val chosenCard = chooseCard()

        hand.remove(chosenCard)
        Table.cards.add(chosenCard)

        println("Computer plays $chosenCard")
        Table.checkIfWon(this)
        println(Table)
    }

    private fun chooseCard(): Card {
        val candidateCards = getCandidateCards()

        return when {
            hand.size == 1 -> FirstCardStrategy.chooseCard(hand)
            candidateCards.size == 1 -> FirstCardStrategy.chooseCard(candidateCards)
            candidateCards.size > 1 -> LeastNeededCandidateCardStrategy.chooseCard(candidateCards, Table.cards.last())
            else -> LeastNeededCardStrategy.chooseCard(hand)
        }
    }

    private fun getCandidateCards(): List<Card> {
        if (Table.cards.isEmpty()) return emptyList()

        val topCard = Table.cards.last()

        return hand.filter { it.rank == topCard.rank || it.suit == topCard.suit }
    }

    object FirstCardStrategy {
        fun chooseCard(cards: List<Card>) = cards.first()
    }

    object LeastNeededCandidateCardStrategy {
        fun chooseCard(cards: List<Card>, topCard: Card): Card {
            val sameSuitCards = cards.filter { it.suit == topCard.suit }

            if (sameSuitCards.size > 1) return sameSuitCards.first()

            val sameRankCards = cards.filter { it.rank == topCard.rank }

            if (sameRankCards.size > 1) return sameRankCards.first()

            return cards.first()
        }
    }

    object LeastNeededCardStrategy {
        fun chooseCard(cards: List<Card>): Card {
            val sameSuitCards = cards.groupBy { it.suit }.filter { it.value.size > 1 }

            if (sameSuitCards.isNotEmpty()) return sameSuitCards.values.first().first()

            val sameRankCards = cards.groupBy { it.rank }.filter { it.value.size > 1 }

            if (sameRankCards.isNotEmpty()) return sameRankCards.values.first().first()

            return cards.first()
        }
    }
}