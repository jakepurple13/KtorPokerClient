import io.ktor.client.HttpClient
import io.ktor.client.features.websocket.WebSockets
import io.ktor.client.features.websocket.ws
import io.ktor.http.HttpMethod
import io.ktor.http.cio.websocket.DefaultWebSocketSession
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.*
import java.awt.Color
import java.util.*
import kotlin.random.Random

suspend fun DefaultWebSocketSession.send(type: CardType) = send(type.toFrameJson())
suspend fun DefaultWebSocketSession.sendHand(cards: List<Card>) = send(CardType(Type.GET_HAND, cards))
suspend fun DefaultWebSocketSession.drawCards(amount: Int) = send(CardType(Type.DRAW_CARDS, amount))
suspend fun DefaultWebSocketSession.submitHand(cards: List<Card>) = send(CardType(Type.SUBMIT_HAND, cards))

fun printr(s: String) = print("$s\r")

@Suppress("BlockingMethodInNonBlockingContext")
suspend fun <T> animateString(s: String, waitFor: suspend () -> T): T {
    val wait = GlobalScope.launch {
        var count = 0
        while (true) {
            printr("$s${".".repeat(count % 4)}".color(Color.GREEN.rgb))
            count++
            delay(500)
            System.out.flush()
        }
    }
    wait.start()
    val waitingFor = waitFor()
    System.out.flush()
    wait.cancel()
    return waitingFor
}

fun input(prompt: String = "", scanner: Scanner = Scanner(System.`in`)): String = println(prompt).let { scanner.nextLine() }
suspend fun DefaultWebSocketSession.getMessage() = (incoming.receive() as Frame.Text).readText().fromJson<CardType>()!!

@KtorExperimentalAPI
fun main() = runBlocking {
    val hand = mutableListOf<Card>()
    val scan = Scanner(System.`in`)
    val client = HttpClient { install(WebSockets) }

    val host = input("Enter the host ip: (default can be 0.0.0.0)", scan).let { if (it.isBlank()) "0.0.0.0" else it }
    val port = input("Enter the port: (default can be 8080)", scan).let { if (it.isBlank()) "8080" else it }.toInt()

    client.ws(method = HttpMethod.Get, host = host, port = port, path = "/poker") {
        val chosenName = getMessage().let { if (it.type == Type.UPDATE) it.any.toString() else "" }
        val prompt = "The name given to you is: $chosenName. " +
                "You can change it now if you wish to. " +
                "Enter your name: (Leave empty if you want to keep your chosen name)"
        val name = input(prompt, scan)
        if (name.isNotBlank()) send(CardType(Type.RENAME, name))

        println("Players:\n${getMessage()<List<String>>()?.joinToString("\n") { it.color(Random.nextColor().rgb) }}")

        gameLoop@ while (isActive) {
            hand.clear()
            println("-".repeat(50).color(Random.nextColor().rgb))
            drawCards(5)
            hand.addAll(getMessage()<List<Card>>()!!)

            sendHand(hand)
            val pokerHand = getMessage()<PokerHand>()
            println(pokerHand?.printHand(hand))

            println(
                "Choose what ${"cards".color(Color.ORANGE.rgb)} to ${"discard".color(Color.RED.rgb)} via ${"index".color(Color.YELLOW.rgb)}. " +
                        "${"(Enter 1-5)".color(Color.CYAN.rgb)} " +
                        "(Or type \"(${"S".color(Color.RED.rgb)})top\" to stop ${"discarding".color(Color.RED.rgb)})"
            )

            var choice = PokerPlay.CONTINUE
            val tempHand: MutableList<Card?> = hand.toMutableList()
            println(tempHand.toSymbol())
            do {
                val choose = scan.nextLine()
                PokerPlay(choose)?.let { choice = it } ?: choose.toIntOrNull()?.let { if (it in 1..5) tempHand[it - 1] = null } ?: continue
                println(tempHand.toSymbol())
            } while (choice != PokerPlay.STOP && !tempHand.all { it == null })

            hand.clear()
            drawCards(tempHand.count { it == null })
            tempHand.removeIf { it == null }
            tempHand.addAll(getMessage()<List<Card>>()!!)
            hand.addAll(tempHand.filterNotNull())

            sendHand(hand)
            val pokerHand2 = getMessage()<PokerHand>()
            println(pokerHand2?.printHand(hand))

            submitHand(hand)
            val winnerIs = animateString("Waiting on other players") { getMessage().let { if (it.type == Type.CHAT) it<String>() else "" } }
            println(winnerIs?.frame(FrameType.BOX.copy(top = "You Had: ${pokerHand2?.stringName}"))?.color(getColorLevel(pokerHand2)))

            val again = "Would you like to keep playing? (${"Y".color(Color.GREEN.rgb)})es/(${"N".color(Color.RED.rgb)})o?"
            var playing: Continue?
            do playing = Continue(input(again, scan)) while (playing == null)
            when (playing) {
                Continue.YES -> continue@gameLoop
                Continue.NO -> break@gameLoop
            }
        }
    }
}
