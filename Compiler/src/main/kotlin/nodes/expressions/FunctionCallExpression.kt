package nodes.expressions

import interpreter.expressionResult.ExpressionResult
import interpreter.nodeVisitor.INodeVisitor
import nodes.expressions.Expression
import nodes.operations.FunctionCall

class FunctionCallExpression(
    val functionCall: FunctionCall,
): Expression() {
    override fun accept(nodeVisitor: INodeVisitor): ExpressionResult {
        return nodeVisitor.visit(this)
    }
}