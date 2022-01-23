package nodes.operations

import interpreter.expressionResult.ExpressionResult
import interpreter.nodeVisitor.INodeVisitor
import nodes.expressions.Expression

class ReturnExpression(
    val expression: Expression,
    override var pos: Pair<Int,Int>,
) : Instruction() {
    override fun accept(nodeVisitor: INodeVisitor): ExpressionResult? {
        return nodeVisitor.visit(this)
    }
}