package nodes.expressions

import interpreter.expressionResult.ExpressionResult
import interpreter.nodeVisitor.INodeVisitor
import nodes.expressions.Expression

class VariableExpression(
    val identifier: String,
): Expression() {
    override fun accept(nodeVisitor: INodeVisitor): ExpressionResult {
        return nodeVisitor.visit(this)
    }
}