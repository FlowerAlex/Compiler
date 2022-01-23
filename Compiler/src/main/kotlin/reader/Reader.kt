package reader

import java.io.BufferedReader

class Reader(
    private val bufferedReader: BufferedReader,
    override var currentChar: Int? = null,
    override var currentLine: Int = 1,
    override var currentIndexInLine: Int = 0,
) : IReader {
    override fun getNextChar(): Int {
        while(currentChar != -1 || currentChar == '\n'.code || currentChar == '\r'.code){
            currentIndexInLine++
            currentChar = bufferedReader.read()

            if(currentChar == '\n'.code && peekNextChar() == '\r'.code){
                currentLine++
                currentIndexInLine = 0
                currentChar = bufferedReader.read()
                currentChar = bufferedReader.read()
                break
            }
            if(currentChar == '\r'.code && peekNextChar() == '\n'.code){
                currentLine++
                currentIndexInLine = 0
                currentChar = bufferedReader.read()
                currentChar = bufferedReader.read()
                break
            }
            if(currentChar == '\n'.code){
                currentLine++
                currentIndexInLine = 0
                currentChar = bufferedReader.read()
                break
            }
            if(currentChar == '\r'.code){
                currentLine++
                currentIndexInLine = 0
                currentChar = bufferedReader.read()
                break
            }
            return currentChar!!
        }
        return currentChar!!
    }

    override fun skipCommentsAndGetNextChar(): Int {
        skipLine()

        var needToMoveToNextLine = true
        var needToSkipLine = false
        while (needToMoveToNextLine){
            needToMoveToNextLine = false
            if(needToSkipLine) {
                skipLine()
                needToSkipLine = false
            }
            while (currentChar != -1) {
                if(currentChar == '\n'.code && peekNextChar() == '\r'.code){
                    needToMoveToNextLine = true
                    currentLine++
                    currentIndexInLine = 0
                    currentChar = bufferedReader.read()
                    currentChar = bufferedReader.read()
                    break
                }
                if(currentChar == '\r'.code && peekNextChar() == '\n'.code){
                    needToMoveToNextLine = true
                    currentLine++
                    currentIndexInLine = 0
                    currentChar = bufferedReader.read()
                    currentChar = bufferedReader.read()
                    break
                }
                if(currentChar == '\n'.code){
                    needToMoveToNextLine = true
                    currentLine++
                    currentIndexInLine = 0
                    currentChar = bufferedReader.read()
                    break
                }
                if(currentChar == '\r'.code){
                    needToMoveToNextLine = true
                    currentLine++
                    currentIndexInLine = 0
                    currentChar = bufferedReader.read()
                    break
                }
                if(currentChar == '/'.code && peekNextChar() == '/'.code){
                    getNextChar()
                    getNextChar()
                    needToMoveToNextLine = true
                    needToSkipLine = true
                    break
                }
                return currentChar!!
            }
        }
        return -1
    }

    override fun peekNextChar(): Int {
        bufferedReader.mark(1)
        val nextChar = bufferedReader.read()
        bufferedReader.reset()
        return nextChar
    }

    override fun getEveryNextChar(): Int {
        currentChar = bufferedReader.read()
        currentIndexInLine++
        return currentChar!!
    }

    private fun skipLine(){
        while (!((currentChar == '\n'.code && peekNextChar() == '\r'.code) ||
                    (currentChar == '\r'.code && peekNextChar() == '\n'.code)||
                    (currentChar == '\n'.code) ||
                    (currentChar == '\r'.code)) && currentChar != -1){
            currentChar = bufferedReader.read()
            currentIndexInLine++
        }
    }
}