package interpreter

import interpreter.nodeVisitor.INodeVisitor
import nodes.INode

class Interpreter(private val rootNode: INode, private val visitor: INodeVisitor) : IInterpreter{
    override fun start() {
        rootNode.accept(visitor)
    }
}