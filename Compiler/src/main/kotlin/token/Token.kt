package token

class Token (val tokenType: TokenType,private val token: Any?,private val pos: Pair<Int, Int>){
    override fun toString(): String {
        return token.toString() + ' ' + tokenType.toString() + ' ' + pos.toString()
    }
}


