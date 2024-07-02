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

class Person: Player {
    override val hand = mutableListOf<Card>()
    override val wonCards = mutableListOf<Card>()
    override val name = "Player"
    override fun takeTurn() {
        println("Cards in hand: ${hand.withIndex().joinToString(" ") { "${it.index + 1})${it.value}" }}")
        var chosenCard: Int? = null

        while (chosenCard == null || chosenCard !in 1..hand.size) {
            println("Choose a card to play (1-${hand.size}):")
            val input = readln()
            if (input == "exit") throw GameOverException()
            chosenCard = input.toIntOrNull()
        }

        Game.table.cards.add(hand.removeAt(chosenCard - 1))
        Game.table.checkIfWon(this)
        println(Game.table)
    }
}

class Computer: Player {
    override val wonCards = mutableListOf<Card>()
    override val hand = mutableListOf<Card>()
    override val name = "Computer"
    override fun takeTurn() {
        val cardToPlay = hand.removeAt(0)
        println("Computer plays $cardToPlay")
        Game.table.cards.add(cardToPlay)
        Game.table.checkIfWon(this)
        println(Game.table)
    }
}

class Table {
    val cards = mutableListOf<Card>()

    fun checkIfWon(player: Player) {
        if (cards.size < 2) return

        val (lastCard, playedCard) = Pair(cards[cards.lastIndex - 1], cards.last())

        if (lastCard.suit == playedCard.suit || lastCard.rank == playedCard.rank) {
            player.wonCards.addAll(cards)
            cards.clear()
            println("${player.name} wins cards")
            Game.printScore()
        }
    }

    override fun toString(): String = if (cards.size > 0) "\n${cards.size} cards on the table, and the top card is ${cards.last()}"
        else "\nNo cards on the table"
}

class Dealer: TurnTaker {
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
        Game.person.hand.clear()
        Game.computer.hand.clear()
        Game.table.cards.clear()
        hand.clear()
        hand.addAll(newDeck.shuffled())

        Game.table.cards.addAll(hand.take(CARDS_ON_INIT))
        hand = hand.subList(CARDS_ON_INIT, hand.size)
        deal()

        println("Initial cards on the table: ${Game.table.cards.joinToString(" ")}")
        println(Game.table)
    }

    private fun deal() {
        dealHand(Game.person)
        dealHand(Game.computer)
    }

    private fun dealHand(player: Player) {
        player.hand.addAll(hand.take(CARDS_PER_TURN))
        hand = hand.subList(CARDS_PER_TURN, hand.size)
    }
}

object Game {
    val person = Person()
    val computer = Computer()
    val table = Table()
    private val dealer = Dealer()

    private val turns = mutableListOf<TurnTaker>(dealer)
    private var turnNumber = 0
    private var startingPlayer: Player = person
    private var turnCount = 0

    fun start(startingPlayer: Player) {
        this.startingPlayer = startingPlayer
        turns.add(startingPlayer)
        if (startingPlayer !== person) turns.add(person)
        else turns.add(computer)

        while (!isOver()) {
            nextTurn()
        }

        if (table.cards.size != 0) {
            this.startingPlayer.wonCards.addAll(table.cards)
            table.cards.clear()
        }

        printScore(true)
        println("Game Over")
    }

    fun printScore(withBonusPoints: Boolean = false) {
        val personGetsBonusPoints = person.wonCards.size > computer.wonCards.size
                || (person.wonCards.size == computer.wonCards.size && startingPlayer == person)

        println("Score: ${person.name} ${person.wonCards.sumOf { it.rank.value } + (if (withBonusPoints && personGetsBonusPoints) 3 else 0)} - ${computer.name} ${computer.wonCards.sumOf { it.rank.value } + (if (withBonusPoints && !personGetsBonusPoints) 3 else 0)}")
        println("Cards: ${person.name} ${person.wonCards.size} - ${computer.name} ${computer.wonCards.size}")
    }

    private fun nextTurn() {
        val nextPlayer = turns[turnNumber % turns.size]
        turnNumber++

        if (nextPlayer === dealer && (person.hand.size != 0 || computer.hand.size != 0)) return
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
        Game.start(if(input == "yes") Game.person else Game.computer)
    } catch (e: GameOverException) {
        println("Game Over")
        return
    }
}