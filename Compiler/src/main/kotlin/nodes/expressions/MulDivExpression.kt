package nodes.expressions

import interpreter.expressionResult.ExpressionResult
import interpreter.nodeVisitor.INodeVisitor
import nodes.expressions.Expression
import nodes.expressions.operators.MulDivOperator

class MulDivExpression(
    val leftExpression: Expression,
    val mulDivOperator: MulDivOperator,
    val rightExpression: Expression,
): Expression() {
    override fun accept(nodeVisitor: INodeVisitor): ExpressionResult {
        return nodeVisitor.visit(this)
    }
}