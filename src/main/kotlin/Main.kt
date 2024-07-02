package indigo

enum class Rank(private val symbol: String, val value: Int) {
    ACE("A", 1),
    TWO("2", 0),
    THREE("3", 0),
    FOUR("4", 0),
    FIVE("5", 0),
    SIX("6", 0),
    SEVEN("7", 0),
    EIGHT("8", 0),
    NINE("9", 0),
    TEN("10", 1),
    JACK("J", 1),
    QUEEN("Q", 1),
    KING("K", 1);

    override fun toString(): String = symbol
}

enum class Suit(private val symbol: String) {
    DIAMONDS("♦"),
    HEARTS("♥"),
    SPADES("♠"),
    CLUBS("♣");

    override fun toString(): String = symbol
}

data class Card(val rank: Rank, val suit: Suit) {
    override fun toString(): String = "$rank$suit"
}

const val CARDS_ON_INIT = 4
const val CARDS_PER_TURN = 6

interface TurnTaker {
    val hand: MutableList<Card>
    fun takeTurn()
}

interface Player: TurnTaker {
    val wonCards: MutableList<Card>
    val name: String
}

private class GameOverException: Exception()

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

object Dealer: TurnTaker {
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

        (Table.lastPlayerThatWon?:startingPlayer).wonCards.addAll(Table.cards)
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

fun main() {
    println("Indigo Card Game")

    var input: String? = null

    while (input != "yes" && input != "no") {
        println("Play first?")
        input = readln()
    }

    try {
        Game.start(if(input == "yes") Person else Computer)
    } catch (e: GameOverException) {
        println("Game Over")
        return
    }
}