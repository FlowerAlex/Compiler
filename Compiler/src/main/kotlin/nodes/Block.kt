package nodes

import interpreter.expressionResult.ExpressionResult
import interpreter.nodeVisitor.INodeVisitor
import nodes.operations.Operation

class Block( // Block
    val operations: MutableList<Operation>,
    override var pos: Pair<Int,Int>,

    ):INode {
    override fun accept(nodeVisitor: INodeVisitor):ExpressionResult? {
        return nodeVisitor.visit(this)
    }
}