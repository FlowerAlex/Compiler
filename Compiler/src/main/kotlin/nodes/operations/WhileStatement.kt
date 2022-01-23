package nodes.operations

import interpreter.expressionResult.ExpressionResult
import interpreter.nodeVisitor.INodeVisitor
import nodes.Block
import nodes.expressions.Expression

class WhileStatement(
    val condition: Expression,
    val ifTrue: Block,
) : Statement() {
    override fun accept(nodeVisitor: INodeVisitor):ExpressionResult? {
        nodeVisitor.visit(this)
        return null
    }
}