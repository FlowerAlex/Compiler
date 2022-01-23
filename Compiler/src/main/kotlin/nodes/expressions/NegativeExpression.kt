package nodes.expressions

import interpreter.expressionResult.ExpressionResult
import interpreter.nodeVisitor.INodeVisitor

class NegativeExpression(
    val expression: Expression,
    override var pos: Pair<Int,Int>,
): Expression() {
    override fun accept(nodeVisitor: INodeVisitor): ExpressionResult {
        return nodeVisitor.visit(this)
    }
}