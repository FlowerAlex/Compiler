import interpreter.Interpreter
import interpreter.nodeVisitor.NodeVisitor
import parser.Parser
import reader.Reader
import scanner.Scanner
import java.io.File

fun main() {

    File("tests/").walk().forEach {
        if(it.isFile){
            with(it.bufferedReader()){
                val reader = Reader(this)
                val scanner = Scanner(reader)
                val parser = Parser(scanner)
                val rootNode = parser.buildTree()
                val interpreter = Interpreter(rootNode,NodeVisitor())
                interpreter.start()
            }
        }
    }


}