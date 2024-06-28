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

class Action(val command: String, val execute: () -> Unit)

const val CARDS_ON_INIT = 4
const val CARDS_PER_TURN = 6

fun main() {
    val originalDeck = buildList {
        for (rank in Rank.entries) {
            for (suit in Suit.entries) {
                add(Card(rank, suit))
            }
        }
    }

    var currentDeck = originalDeck.toMutableList()
    val currentTable = mutableListOf<Card>()
    val playerCards = mutableListOf<Card>()
    val computerCards = mutableListOf<Card>()
    var isGameCompleted = false

    var onWrongAction: () -> Unit = {
        println("Play first?")
    }

    val gameActions = buildList {
        add(Action("exit") {
            isGameCompleted = true
        })
    }

    val availableActions = mutableListOf<Action>()

    var hiddenActions: Map<String, Action> = emptyMap()

    hiddenActions = buildList {
        add(Action("init"){
            currentDeck = currentDeck.shuffled().toMutableList()
            currentTable.addAll(currentDeck.take(CARDS_ON_INIT))
            currentDeck = currentDeck.subList(CARDS_ON_INIT, currentDeck.size)

            println("Initial cards on the table: ${currentTable.joinToString(" ")}\n")
            onWrongAction = {
                println("Choose a card to play (1-${playerCards.lastIndex + 1}):")
            }

            hiddenActions["deal"]!!.execute()
        })
        add(Action("deal") {
            if (currentDeck.size == 0) {
                hiddenActions["prepareCards"]!!.execute()
                isGameCompleted = true
                return@Action
            }

            playerCards.addAll(currentDeck.take(CARDS_PER_TURN))
            currentDeck = currentDeck.subList(CARDS_PER_TURN, currentDeck.size)
            computerCards.addAll(currentDeck.take(CARDS_PER_TURN))
            currentDeck = currentDeck.subList(CARDS_PER_TURN, currentDeck.size)

            hiddenActions["prepareCards"]!!.execute()
        })
        add(Action("player") {
            println("Cards in hand: ${playerCards.withIndex().joinToString(" ") { "${it.index + 1})${it.value}" } }")
            println("Choose a card to play (1-${playerCards.lastIndex + 1}):")
        })
        add(Action("computer") {
            println("Computer plays ${computerCards.first()}\n")
            currentTable.add(computerCards.first())
            computerCards.removeAt(0)

            if (playerCards.size == 0) hiddenActions["deal"]!!.execute()
            else hiddenActions["prepareCards"]!!.execute()

            if (!isGameCompleted) hiddenActions["player"]!!.execute()
        })
        add(Action("prepareCards") {
            availableActions.apply {
                clear()
                addAll(gameActions)

                for ((index, card) in playerCards.withIndex()) {
                    add(Action("${index + 1}") InnerAction@{
                        playerCards.remove(card)
                        currentTable.add(card)

                        if (computerCards.size == 0) hiddenActions["deal"]!!.execute()
                        else hiddenActions["prepareCards"]!!.execute()

                        if (!isGameCompleted) hiddenActions["computer"]!!.execute()
                    })
                }

                println("${currentTable.size} cards on the table, and the top card is ${currentTable.last()}")
            }
        })
    }.associateBy { it.command }

    val startingActions = buildList {
        add(Action("yes") {
            hiddenActions["init"]!!.execute()
            hiddenActions["player"]!!.execute()
        })
        add(Action("no") {
            hiddenActions["init"]!!.execute()
            hiddenActions["computer"]!!.execute()
        })
    }

    println("Indigo Card Game")
    println("Play first?")

    availableActions.addAll(startingActions)

    while(!isGameCompleted) {
        val commandString = readln().lowercase()

        val action = availableActions.firstOrNull { it.command == commandString }

        if (action == null) {
            onWrongAction()
            continue
        }

        action.execute()
    }

    println("Game over")
}