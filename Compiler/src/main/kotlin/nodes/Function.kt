package nodes

import interpreter.expressionResult.ExpressionResult
import interpreter.nodeVisitor.INodeVisitor

class Function ( // Function
    val identifier : String,
    val argsDefList: MutableList<ArgumentDefinition>,
    val returnType: Type?,
    val block: Block,
):INode {
    override fun accept(nodeVisitor: INodeVisitor):ExpressionResult? {
        nodeVisitor.visit(this)
        return null
    }
}