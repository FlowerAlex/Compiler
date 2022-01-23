package nodes

import interpreter.expressionResult.ExpressionResult
import interpreter.nodeVisitor.INodeVisitor
import nodes.operations.Operation

class RootNode( // Program
    val functions:MutableList<Function>,
    val operations: MutableList<Operation>,
):INode {
    override fun accept(nodeVisitor: INodeVisitor):ExpressionResult? {
        nodeVisitor.visit(this)
        return null
    }
}