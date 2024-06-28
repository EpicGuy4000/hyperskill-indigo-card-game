package indigo

enum class Rank(private val symbol: String, val value: Int) {
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

interface Player {
    val cards: MutableList<Card>
    fun takeTurn()
}

private class GameOverException: Exception()

class Person: Player {
    override val cards = mutableListOf<Card>()
    override fun takeTurn() {
        println("Cards in hand: ${cards.withIndex().joinToString(" ") { "${it.index + 1})${it.value}" }}")
        var chosenCard: Int? = null

        while (chosenCard == null || chosenCard !in 1..cards.size) {
            println("Choose a card to play (1-${cards.size}):")
            val input = readln()
            if (input == "exit") throw GameOverException()
            chosenCard = input.toIntOrNull()
        }

        Game.table.cards.add(cards.removeAt(chosenCard - 1))
        println(Game.table)
    }
}

class Computer: Player {
    override val cards = mutableListOf<Card>()
    override fun takeTurn() {
        val cardToPlay = cards.removeAt(0)
        Game.table.cards.add(cardToPlay)
        println("Computer plays $cardToPlay\n")
        println(Game.table)
    }
}

class Table {
    val cards = mutableListOf<Card>()

    override fun toString(): String = "${cards.size} cards on the table, and the top card is ${cards.last()}"
}

class Dealer: Player {
    private val newDeck = buildList {
        for (rank in Rank.entries) {
            for (suit in Suit.entries) {
                add(Card(rank, suit))
            }
        }
    }

    override var cards = mutableListOf<Card>()

    override fun takeTurn() = if (cards.size == 0) newGame() else deal()

    private fun newGame() {
        Game.person.cards.clear()
        Game.computer.cards.clear()
        Game.table.cards.clear()
        cards.clear()
        cards.addAll(newDeck.shuffled())

        Game.table.cards.addAll(cards.take(CARDS_ON_INIT))
        cards = cards.subList(CARDS_ON_INIT, cards.size)
        deal()

        println("Initial cards on the table: ${Game.table.cards.joinToString(" ")}\n")
        println(Game.table)
    }

    private fun deal() {
        dealHand(Game.person)
        dealHand(Game.computer)
    }

    private fun dealHand(player: Player) {
        player.cards.addAll(cards.take(CARDS_PER_TURN))
        cards = cards.subList(CARDS_PER_TURN, cards.size)
    }
}

object Game {
    val person = Person()
    val computer = Computer()
    val table = Table()
    private val dealer = Dealer()

    private val turns = mutableListOf<Player>(dealer)
    private var turnNumber = 0

    fun start(startingPlayer: Player) {
        turns.add(startingPlayer)
        if (startingPlayer !== person) turns.add(person)
        else turns.add(computer)

        while (!isOver()) {
            nextTurn()
        }

        println("Game over")
    }

    private fun nextTurn() {
        val nextPlayer = turns[turnNumber % turns.size]
        turnNumber++

        if (nextPlayer === dealer && (person.cards.size != 0 || computer.cards.size != 0)) return
        nextPlayer.takeTurn()
    }

    private fun isOver() = table.cards.size == 52
}

fun main() {
    println("Indigo Card Game")

    var input: String? = null

    while (input != "yes" && input != "no") {
        println("Play first?")
        input = readln()
    }

    try {
        Game.start(if(input == "yes") Game.person else Game.computer)
    } catch (e: GameOverException) {
        println("Game over")
        return
    }
}