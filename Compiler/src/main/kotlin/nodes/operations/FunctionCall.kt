package nodes.operations

import interpreter.expressionResult.ExpressionResult
import interpreter.nodeVisitor.INodeVisitor
import nodes.FunctionArgument

class FunctionCall(
    val identifier: String,
    val args: MutableList<FunctionArgument>,
    override var pos: Pair<Int,Int>,
) : Instruction() {
    override fun accept(nodeVisitor: INodeVisitor): ExpressionResult? {
        return nodeVisitor.visit(this)
    }
}