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

class Action(val command: String, val isTerminating: Boolean = false, val execute: () -> Unit)

fun main() {
    val originalDeck = buildList {
        for (rank in Rank.entries) {
            for (suit in Suit.entries) {
                add(Card(rank, suit))
            }
        }
    }

    var currentDeck = originalDeck.toMutableList()

    val actions = buildList {
        add(Action("reset") {
            currentDeck = originalDeck.toMutableList()
            println("Card deck is reset.")
        })
        add(Action("shuffle") {
            currentDeck = currentDeck.shuffled().toMutableList()
            println("Card deck is shuffled.")
        })
        add(Action("get") {
            println("Number of cards:")
            readln().toIntOrNull().let { numberOfCards ->
                if (numberOfCards == null || numberOfCards !in 1..52) {
                    println("Invalid number of cards.")
                    return@let
                }
                if (numberOfCards > currentDeck.size) {
                    println("The remaining cards are insufficient to meet the request.")
                    return@let
                }

                val removedCards = currentDeck.take(numberOfCards)
                currentDeck = currentDeck.subList(numberOfCards, currentDeck.size)
                println(removedCards.joinToString(" "))
            }
        })
        add(Action("exit", true) {
            println("Bye")
        })
    }.associateBy { it.command }

    var commandString: String

    while(true) {
        println("Choose an action (${actions.keys.joinToString(", ")}):")
        commandString = readln()

        val action = actions[commandString]

        if (action == null) {
            println("Wrong action.")
            continue
        }

        action.execute.invoke()

        if (action.isTerminating) break
    }
}