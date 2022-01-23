package nodes.operations

import interpreter.expressionResult.ExpressionResult
import interpreter.nodeVisitor.INodeVisitor
import nodes.expressions.Expression
import nodes.Type

class VariableDeclaration(
    val identifier: String,
    val type: Type,
    val expression: Expression?,
) : Instruction(){
    override fun accept(nodeVisitor: INodeVisitor): ExpressionResult? {
        nodeVisitor.visit(this)
        return null
    }
}