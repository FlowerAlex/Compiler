package nodes

import interpreter.expressionResult.ExpressionResult
import interpreter.nodeVisitor.INodeVisitor

interface INode {
    var pos: Pair<Int,Int>

    fun accept(nodeVisitor: INodeVisitor): ExpressionResult?
}

interface IExpressionNode {
    var pos: Pair<Int,Int>

    fun accept(nodeVisitor: INodeVisitor): ExpressionResult
}