package nodes

import interpreter.expressionResult.ExpressionResult
import interpreter.nodeVisitor.INodeVisitor
import nodes.expressions.Expression

class Function ( // Function
    val identifier : String,
    val argsDefList: MutableList<ArgumentDefinition>,
    val returnType: Type?,
    val block: Block,
    override var pos: Pair<Int,Int>,
):INode {
    override fun accept(nodeVisitor: INodeVisitor):ExpressionResult? {
        nodeVisitor.visit(this)
        return null
    }
}

class ArgumentDefinition(
    val name: String,
    val type: Type,
)

class FunctionArgument(
    val primaryExpression: Expression,
)