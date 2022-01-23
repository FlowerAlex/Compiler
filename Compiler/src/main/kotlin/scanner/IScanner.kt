package scanner

import token.Token

interface IScanner {
    fun getTokenPosition(): Pair<Int,Int>
    fun getNextToken(): Token?
}