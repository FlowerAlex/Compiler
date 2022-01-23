package reader

interface IReader {
    var currentChar: Int?
    var currentLine: Int
    var currentIndexInLine: Int

    fun getNextChar(): Int
    fun skipCommentsAndGetNextChar(): Int
    fun peekNextChar(): Int
    fun getEveryNextChar(): Int
}