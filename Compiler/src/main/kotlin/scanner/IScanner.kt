package scanner

import token.Token

interface IScanner {
    fun getNextToken(): Token?
}