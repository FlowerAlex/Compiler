package scanner

import reader.IReader
import token.Token
import token.TokenType
import java.lang.Exception
import kotlin.math.pow

class Scanner(private val reader: IReader) : IScanner {
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

    override fun getTokenPosition(): Pair<Int, Int> {
        return Pair(reader.currentLine, reader.currentIndexInLine+1)
    }

    override fun getNextToken(): Token {
        var currentChar = reader.getNextChar()

        while (isWhiteSpaceOrNextLine(currentChar, reader.peekNextChar())){
            if(currentChar != ' '.code)reader.currentLine++
            currentChar = reader.getNextChar()
        }

        var res = recognizeCommentOrDivide(currentChar)

        if(res != null) return res
        currentChar = reader.currentChar!!

        val pos = Pair(reader.currentLine,reader.currentIndexInLine)

        if(currentChar == -1){
            return Token(TokenType.EOF,null, pos)
        }

        val currentToken = StringBuilder()

        res = recognizeString(currentChar,currentToken,pos)
        if(res != null) return res

        res = recognizeIntOrDouble(currentChar,pos)
        if(res != null) return res

        currentToken.append(currentChar.toChar())

        res = recognizeSymbolicToken(currentToken, pos)
        if(res != null) return res

        while (isLetter(reader.peekNextChar()) || isDigit(reader.peekNextChar())){
            currentToken.append(reader.getNextChar().toChar())
        }

        res = recognizeKeywordToken(currentToken, pos)
        if(res != null) return res

        res = recognizeIdentifierToken(currentToken, pos)
        if(res != null) return res

        return Token(TokenType.UNKNOWN, currentToken.toString(),pos)
    }

    private fun recognizeIdentifierToken(currentToken: StringBuilder, pos: Pair<Int, Int>): Token? {
        if(isLetter(currentToken[0].code) ){
            return Token(TokenType.IDENTIFIER,currentToken.toString(),pos)
        }
        return null
    }

    private fun recognizeKeywordToken(currentToken: StringBuilder, pos: Pair<Int, Int>): Token? {
        val tokenType = KeywordTokens[currentToken.toString()]
        if(tokenType != null){
            return Token(tokenType, currentToken.toString(),pos)
        }
        return null
    }

    private fun recognizeSymbolicToken(currentToken: StringBuilder,pos: Pair<Int, Int>): Token? {
        val nextChar = reader.peekNextChar().toChar()
        var tokenType = SymbolicTokens[currentToken.toString() + nextChar]
        if(tokenType != null){
            reader.getNextChar()
            return Token(tokenType, currentToken.toString() + nextChar,pos)
        }

        tokenType = SymbolicTokens[currentToken.toString()]
        if(tokenType != null){
            return Token(tokenType, currentToken.toString(),pos)
        }
        return null
    }

    private fun recognizeIntOrDouble(currentChar: Int,pos:Pair<Int,Int>): Token?{
        var tmpCurrentChar = currentChar
        if(isDigit(tmpCurrentChar)){
            var res = tmpCurrentChar -'0'.code
            if(tmpCurrentChar != '0'.code){
                while(isDigit(reader.peekNextChar())){
                    tmpCurrentChar = reader.getNextChar()
                    res = 10*res + (tmpCurrentChar -'0'.code)
                }
            }

            return if(reader.peekNextChar() == '.'.code){
                reader.getNextChar()

                if(isDigit(reader.peekNextChar())){
                    var partRes = 0
                    var countOfDigits = 0
                    while (isDigit(reader.peekNextChar())){
                        partRes += partRes * 10 + (reader.getNextChar() - '0'.code)
                        countOfDigits++
                    }
                    Token(TokenType.VALUE_DOUBLE,res.toDouble() + (partRes.toDouble()/ (10.0.pow(countOfDigits.toDouble()))) , pos)
                }else throw Exception("After '.' should be at least 1 digit")
            }else{
                Token(TokenType.VALUE_INT,res, pos)
            }
        }
        return null
    }

    private fun recognizeString(currentChar: Int, currentToken:StringBuilder,pos:Pair<Int,Int>): Token?{
        if(currentChar == '"'.code){
            var nextCh = reader.getEveryNextChar()
            while (nextCh != '"'.code){
                if(nextCh == '\\'.code){
                    when (reader.peekNextChar()) {
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
                            throw Exception("Invalid string value, $pos")
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
        return null
    }

    private fun recognizeCommentOrDivide(currentChar: Int):Token? {
        var tmpCurrentChar = currentChar
        while (reader.currentChar == '/'.code){
            if(reader.peekNextChar() == '/'.code){
                tmpCurrentChar = reader.skipCommentsAndGetNextChar()
                while (isWhiteSpaceOrNextLine(tmpCurrentChar,reader.peekNextChar())){
                    tmpCurrentChar = reader.getNextChar()
                }
            }else{
                return Token(TokenType.DIVIDE,tmpCurrentChar.toChar(), Pair(reader.currentLine,reader.currentIndexInLine))
            }
        }
        return null
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