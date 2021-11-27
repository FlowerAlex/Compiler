package reader

interface IReader {
    fun getNextChar(): Int
    fun skipCommentsAndGetNextChar(): Int
    fun peekNextChar(): Int
    fun getEveryNextChar(): Int
}