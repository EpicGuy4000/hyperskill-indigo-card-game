package indigo

interface Player: TurnTaker {
    val wonCards: MutableList<Card>
    val name: String
}