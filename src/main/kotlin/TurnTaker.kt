package indigo

interface TurnTaker {
    val hand: MutableList<Card>
    fun takeTurn()
}