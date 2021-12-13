package token

class Token (val tokenType: TokenType, val token: Any?, val pos: Pair<Int, Int>){
    override fun toString(): String {
        return token.toString() + ' ' + tokenType.toString() + ' ' + pos.toString()
    }
}


