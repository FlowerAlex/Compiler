package nodes.expressions

import interpreter.expressionResult.ExpressionResult
import interpreter.nodeVisitor.INodeVisitor
import nodes.expressions.operators.AddSubOperator

class AddSubExpression(
    val leftExpression: Expression,
    val addSubOperator: AddSubOperator,
    val rightExpression: Expression,
): Expression() {
    override fun accept(nodeVisitor: INodeVisitor): ExpressionResult {
        return nodeVisitor.visit(this)
    }
}