package nodes.operations

import interpreter.expressionResult.ExpressionResult
import interpreter.nodeVisitor.INodeVisitor
import nodes.expressions.Expression

class VariableAssignment(
    val identifier: String,
    val expression: Expression,
) : Instruction(){
    override fun accept(nodeVisitor: INodeVisitor): ExpressionResult? {
        nodeVisitor.visit(this)
        return null
    }
}