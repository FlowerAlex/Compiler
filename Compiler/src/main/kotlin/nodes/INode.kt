package nodes

import interpreter.expressionResult.ExpressionResult
import interpreter.nodeVisitor.INodeVisitor

interface INode {
    fun accept(nodeVisitor: INodeVisitor): ExpressionResult?
}

interface IExpressionNode {
    fun accept(nodeVisitor: INodeVisitor): ExpressionResult
}