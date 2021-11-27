package scanner

import reader.Reader
import token.Token
import token.TokenType
import java.lang.Exception
import kotlin.math.pow

class Scanner(private val reader: Reader) : IScanner { // Refactor code add more methods!!!!!!!!!!!!!!!!!
    companion object{
        val SymbolicTokens = mapOf(
            "," to TokenType.COMMA,
            ":" to TokenType.COLON,
            ";" to TokenType.SEMICOLON,
            "{" to TokenType.LEFT_CURLY_BRACKET,
            "}" to TokenType.RIGHT_CURLY_BRACKET,
            "(" to TokenType.LEFT_ROUND_BRACKET,
            ")" to TokenType.RIGHT_ROUND_BRACKET,
            "=" to TokenType.ASSIGN,
            "!" to TokenType.EXCLAMATION_MARK,
            "||" to TokenType.OR,
            "&&" to TokenType.AND,
            "==" to TokenType.EQUAL,
            "!=" to TokenType.NOT_EQUAL,
            ">" to TokenType.MORE_EXCLUSIVE,
            ">=" to TokenType.MORE_INCLUSIVE,
            "<" to TokenType.LESS_EXCLUSIVE,
            "<=" to TokenType.LESS_INCLUSIVE,
            "+" to TokenType.PLUS,
            "-" to TokenType.MINUS,
            "*" to TokenType.MULTIPLY,
            "/" to TokenType.DIVIDE,
        )
         val KeywordTokens = mapOf(
             "fun" to TokenType.FUN,
             "if" to TokenType.IF,
             "else" to TokenType.ELSE,
             "while" to TokenType.WHILE,
             "return" to TokenType.RETURN,
             "int" to TokenType.INT,
             "double" to TokenType.DOUBLE,
             "bool" to TokenType.BOOL,
             "True" to TokenType.TRUE,
             "False" to TokenType.FALSE,
         )
    }

    override fun getNextToken(): Token {
        var currentChar = reader.getNextChar()

        while (isWhiteSpaceOrNextLine(currentChar, reader.peekNextChar())){
            if(currentChar != ' '.code)reader.currentLine++
            currentChar = reader.getNextChar()
        }

        while (reader.currentChar == '/'.code){
            if(reader.peekNextChar() == '/'.code){
                currentChar = reader.skipCommentsAndGetNextChar()
                while (isWhiteSpaceOrNextLine(currentChar,reader.peekNextChar())){
                    currentChar = reader.getNextChar()
                }
            }else{
                return Token(TokenType.DIVIDE,currentChar.toChar(), Pair(reader.currentLine,reader.currentIndexInLine))
            }
        }

        if(currentChar == -1){
            return Token(TokenType.EOF,null,Pair(reader.currentLine,reader.currentIndexInLine))
        }

        val currentToken = StringBuilder()

        if(currentChar == '"'.code){
            var nextCh = reader.getEveryNextChar()
            while (nextCh != '"'.code){
                if(nextCh == '\\'.code){
                    val nextNextCh = reader.peekNextChar()
                    when (nextNextCh) {
                        '\\'.code -> {
                            currentToken.append('\\')
                            reader.getNextChar()
                        }
                        't'.code -> {
                            currentToken.append('\t')
                            reader.getNextChar()
                        }
                        '"'.code -> {
                            currentToken.append('\"')
                            reader.getNextChar()
                        }
                        'b'.code -> {
                            currentToken.append('\b')
                            reader.getNextChar()
                        }
                        'r'.code -> {
                            currentToken.append('\r')
                            reader.getNextChar()
                        }
                        'n'.code -> {
                            currentToken.append('\n')
                            reader.getNextChar()
                        }
                        else -> {
                            throw Exception("Invalid string value, ${Pair(reader.currentLine,reader.currentIndexInLine).toString()}")
                        }
                    }

                }else{
                    currentToken.append(nextCh.toChar())
                    nextCh = reader.peekNextChar()

                }
                if(nextCh == '\n'.code || nextCh == '\r'.code || nextCh == -1) return Token(TokenType.UNKNOWN, currentToken.toString(), Pair(reader.currentLine,reader.currentIndexInLine))
                nextCh = reader.getEveryNextChar()
            }
            return Token(TokenType.TEXT,currentToken.toString(),Pair(reader.currentLine,reader.currentIndexInLine))
        }
        val pos = Pair(reader.currentLine,reader.currentIndexInLine)

        currentToken.append(currentChar.toChar())

        if(isDigit(currentChar)){
            var res = currentChar -'0'.code
            if(currentChar != '0'.code){
                while(isDigit(reader.peekNextChar())){
                    currentChar = reader.getNextChar()
                    res = 10*res + (currentChar -'0'.code)
                }
            }

            if(reader.peekNextChar() == '.'.code){
                reader.getNextChar()

                if(isDigit(reader.peekNextChar())){
                    var partRes = 0
                    var countOfDigits = 0
                    while (isDigit(reader.peekNextChar())){
                        partRes += partRes * 10 + (reader.getNextChar() - '0'.code)
                        countOfDigits++
                    }
                    return Token(TokenType.VALUE_DOUBLE,res.toDouble() + (partRes.toDouble()/ (10.0.pow(countOfDigits.toDouble()))) , pos)
                }
            }else{
                return Token(TokenType.VALUE_INT,res, pos)
            }
        }

        // symbolic token with 2 chars
        var nextChar = reader.peekNextChar().toChar()
        var tokenType = SymbolicTokens[currentToken.toString() + nextChar]
        if(tokenType != null){
            reader.getNextChar()
            return Token(tokenType, currentToken.toString() + nextChar,pos)
        }

        // symbolic token with 1 char
        tokenType = SymbolicTokens[currentToken.toString()]
        if(tokenType != null){
            return Token(tokenType, currentToken.toString(),pos)
        }

        while (isLetter(reader.peekNextChar()) || isDigit(reader.peekNextChar())){
            currentToken.append(reader.getNextChar().toChar())
        }

        // keyword tokens
        tokenType = KeywordTokens[currentToken.toString()]
        if(tokenType != null){
            return Token(tokenType, currentToken.toString(),pos)
        }

        if(isLetter(currentToken[0].code) ){
           return Token(TokenType.IDENTIFIER,currentToken.toString(),pos)
        }

        return Token(TokenType.UNKNOWN, currentToken.toString(),pos)
    }

    private fun isWhiteSpaceOrNextLine(ch: Int, nextCh: Int): Boolean{
        return ch == ' '.code ||
                (ch == '\n'.code && nextCh == '\r'.code) ||
                (ch == '\r'.code && nextCh == '\n'.code) ||
                (ch == '\r'.code) || (ch == '\n'.code)
    }
    private fun isLetter(ch: Int): Boolean{
        return Char(ch).isLetter()
    }
    private fun isDigit(ch: Int): Boolean{
        return (ch >= '0'.code && ch <= '9'.code)
    }
}