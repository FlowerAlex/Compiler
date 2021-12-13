import parser.Parser
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
            val parser = Parser(scanner)

            val rootNode = parser.buildTree()

            bufferedReader.close()
        }
    }


}