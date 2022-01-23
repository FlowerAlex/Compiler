package nodes.operations

import interpreter.expressionResult.ExpressionResult
import interpreter.nodeVisitor.INodeVisitor
import nodes.Block
import nodes.expressions.Expression

class IfStatement(
    val condition: Expression,
    val ifTrue: Block,
    val ifElse: Block?,
    override var pos: Pair<Int,Int>,
) : Statement() {
    override fun accept(nodeVisitor: INodeVisitor): ExpressionResult? {
        return nodeVisitor.visit(this)
    }
}