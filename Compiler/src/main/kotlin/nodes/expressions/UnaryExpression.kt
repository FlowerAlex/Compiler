package nodes.expressions

import interpreter.expressionResult.ExpressionResult
import interpreter.nodeVisitor.INodeVisitor
import nodes.expressions.Expression

class UnaryExpression(
    val expression: Expression,
): Expression() {
    override fun accept(nodeVisitor: INodeVisitor): ExpressionResult {
        return nodeVisitor.visit(this)
    }
}