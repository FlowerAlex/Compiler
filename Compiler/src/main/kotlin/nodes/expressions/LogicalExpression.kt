package nodes.expressions

import interpreter.expressionResult.ExpressionResult
import interpreter.nodeVisitor.INodeVisitor
import nodes.expressions.Expression
import nodes.expressions.operators.LogicalOperator

class LogicalExpression(
    val leftExpression: Expression,
    val logicalOperator: LogicalOperator,
    val rightExpression: Expression,
) : Expression() {
    override fun accept(nodeVisitor: INodeVisitor): ExpressionResult {
        return nodeVisitor.visit(this)
    }
}