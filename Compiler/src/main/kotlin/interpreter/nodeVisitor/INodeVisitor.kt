package interpreter.nodeVisitor

import interpreter.FunctionScope
import interpreter.expressionResult.ExpressionResult
import nodes.*
import nodes.Function
import nodes.expressions.*
import nodes.operations.*

interface INodeVisitor {
    // expressions
    fun visit(expression: Expression): ExpressionResult

    fun visit(addSubExpression: AddSubExpression):ExpressionResult
    fun visit(boolValue: BoolValue):ExpressionResult
    fun visit(doubleValue: DoubleValue):ExpressionResult
    fun visit(functionCallExpression: FunctionCallExpression):ExpressionResult
    fun visit(intValue: IntValue):ExpressionResult
    fun visit(logicalExpression: LogicalExpression):ExpressionResult
    fun visit(mulDivExpression: MulDivExpression):ExpressionResult
    fun visit(negativeExpression: NegativeExpression):ExpressionResult
    fun visit(relationalExpression: RelationalExpression):ExpressionResult
    fun visit(textValue: TextValue):ExpressionResult
    fun visit(unaryExpression: UnaryExpression):ExpressionResult
    fun visit(variableExpression: VariableExpression):ExpressionResult

    // operations
    fun visit(operation: Operation): ExpressionResult?
    fun visit(statement: Statement): ExpressionResult?
    fun visit(instruction: Instruction): ExpressionResult?

    fun visit(functionCall: FunctionCall): ExpressionResult?
    fun visit(ifStatement: IfStatement): ExpressionResult?
    fun visit(returnExpression: ReturnExpression):ExpressionResult
    fun visit(variableAssignment: VariableAssignment)
    fun visit(variableDeclaration: VariableDeclaration)
    fun visit(whileStatement: WhileStatement):ExpressionResult?

    // others
    fun visit(rootNode: RootNode):ExpressionResult?
    fun visit(functionArgument: FunctionArgument): ExpressionResult
    fun visit(function: Function)
    fun visit(block: Block):ExpressionResult?

}