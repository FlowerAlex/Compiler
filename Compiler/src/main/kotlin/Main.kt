import reader.Reader
import scanner.Scanner
import token.Token
import token.TokenType
import java.io.File

fun main() {

    File("tests/").walk().forEach {
        if(it.isFile){
            val bufferedReader = it.bufferedReader() // use with
            val reader = Reader(bufferedReader)
            val scanner = Scanner(reader)

            var tmpToken : Token? = null
            val tokens = mutableListOf<Token>()
            while(tmpToken?.tokenType != TokenType.EOF){
                tmpToken = scanner.getNextToken()
                tokens.add(tmpToken)
            }
            println(it.name)
            tokens.forEach{token ->
                println(token.toString())
            }
            println()

            bufferedReader.close()
        }
    }


}