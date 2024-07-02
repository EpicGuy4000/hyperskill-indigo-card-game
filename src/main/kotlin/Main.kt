package indigo

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