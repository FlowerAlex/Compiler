package interpreter.nodeVisitor

import interpreter.FunctionCallContext
import interpreter.FunctionScope
import interpreter.VariableDataContext
import interpreter.expressionResult.*
import nodes.*
import nodes.Function
import nodes.expressions.*
import nodes.expressions.operators.AddSubOperator
import nodes.expressions.operators.LogicalOperator
import nodes.expressions.operators.MulDivOperator
import nodes.expressions.operators.RelationalOperator
import nodes.operations.*
import java.util.Stack

class NodeVisitor(
    val functionDeclarations: MutableList<Function>,
    val functionCallContexts: Stack<FunctionCallContext>,
):INodeVisitor {
    override fun visit(variableDeclaration: VariableDeclaration) {
        if(functionCallContexts.last().functionScopes.any{it.variableDataContexts.any{variableDataContexts -> variableDataContexts.name == variableDeclaration.identifier} }) throw Exception("Variable with the same name already defined")
        if (variableDeclaration.expression != null) {
            visit(variableDeclaration.expression)
        }
        val expressionResult = if (variableDeclaration.expression != null) visit(variableDeclaration.expression) else null
        functionCallContexts.peek().functionScopes.last().variableDataContexts.add(VariableDataContext(variableDeclaration.type,variableDeclaration.identifier, expressionResult))
    }

    override fun visit(whileStatement: WhileStatement) {
        var expressionResult = visit(whileStatement.condition)
        if(expressionResult.getType() != Type.BOOL) throw Exception("Condition must be boolean type")

        while((expressionResult.getValue() as Boolean)){
            functionCallContexts.last().functionScopes.add(FunctionScope(mutableListOf()))

            visit(whileStatement.ifTrue)
            expressionResult = visit(whileStatement.condition)

            functionCallContexts.last().functionScopes.removeLast()
        }
    }

    override fun visit(rootNode: RootNode) {
        for(function in rootNode.functions){
            visit(function)
        }
        for(operation in rootNode.operations){
            visit(operation)
        }
    }

    override fun visit(functionArgument: FunctionArgument):ExpressionResult {
        return visit(functionArgument.primaryExpression)
    }

    override fun visit(function: Function) {
        if(functionCallContexts.any{ it.functionName == function.identifier}) throw Exception("Function with name: ${function.identifier} already defined")
        functionDeclarations.add(function)
    }

    override fun visit(block: Block): ExpressionResult? {
        var res: ExpressionResult? = null
        for(operation in block.operations){
            res = visit(operation)
            if (res != null) break
        }
        return res
    }

    override fun visit(variableAssignment: VariableAssignment) {
        val expressionResult = variableAssignment.expression.accept(this)

        var isValueExistInScope = false
        for (functionScope in functionCallContexts.peek().functionScopes){
            for(variableDataContext in functionScope.variableDataContexts){
                if(variableDataContext.name == variableAssignment.identifier){
                    if(variableDataContext.type != expressionResult.getType()) throw Exception("Variable type is not the same")
                    variableDataContext.value = expressionResult
                    isValueExistInScope = true
                    break
                }
            }
        }

        if(!isValueExistInScope){
            throw Exception("Variable does not exist")
        }
    }

    override fun visit(expression: Expression): ExpressionResult {
        return expression.accept(this)
    }

    override fun visit(addSubExpression: AddSubExpression): ExpressionResult {
        val leftRes = visit(addSubExpression.leftExpression)
        if(leftRes.getType() != Type.INT && leftRes.getType() != Type.DOUBLE) throw Exception("Numeric values expected")
        val rightRes = visit(addSubExpression.rightExpression)
        if(rightRes.getType() != Type.INT && rightRes.getType() != Type.DOUBLE) throw Exception("Numeric values expected")

        if(leftRes.getType() == Type.INT && rightRes.getType() == Type.INT){
            val leftVal = leftRes.getValue() as Int
            val rightVal = rightRes.getValue() as Int

            return when(addSubExpression.addSubOperator){
                AddSubOperator.PLUS -> IntExpressionResult(leftVal+rightVal)
                AddSubOperator.MINUS -> IntExpressionResult(leftVal-rightVal)
            }
        }else{
            val leftVal = leftRes.getValue() as Double
            val rightVal = rightRes.getValue() as Double

            return when(addSubExpression.addSubOperator){
                AddSubOperator.PLUS -> DoubleExpressionResult(leftVal+rightVal)
                AddSubOperator.MINUS -> DoubleExpressionResult(leftVal-rightVal)
            }
        }
    }

    override fun visit(boolValue: BoolValue): ExpressionResult {
        return BoolExpressionResult(boolValue.value)
    }

    override fun visit(doubleValue: DoubleValue): ExpressionResult {
        return DoubleExpressionResult(doubleValue.value)
    }

    override fun visit(functionCallExpression: FunctionCallExpression): ExpressionResult {
        return visit(functionCallExpression.functionCall)
            ?: throw Exception("Function: ${functionCallExpression.functionCall.identifier} should return value")
    }

    override fun visit(intValue: IntValue): ExpressionResult {
        return IntExpressionResult(intValue.value)
    }

    override fun visit(logicalExpression: LogicalExpression): ExpressionResult {
        val leftRes = visit(logicalExpression.leftExpression)
        if(leftRes.getType() != Type.BOOL) throw Exception("Boolean type expected")
        val leftVal = leftRes.getValue() as Boolean

        if(leftVal && logicalExpression.logicalOperator == LogicalOperator.OR) {
            return BoolExpressionResult(true)
        }

        val rightRes = visit(logicalExpression.rightExpression)
        if(rightRes.getType() != Type.BOOL) throw Exception("Boolean type expected")
        val rightVal = rightRes.getValue() as Boolean


        return when(logicalExpression.logicalOperator){
            LogicalOperator.AND -> BoolExpressionResult(leftVal && rightVal)
            LogicalOperator.OR -> BoolExpressionResult(leftVal || rightVal)
        }

    }

    override fun visit(mulDivExpression: MulDivExpression): ExpressionResult {
        val leftRes = visit(mulDivExpression.leftExpression)
        if(leftRes.getType() != Type.INT && leftRes.getType() != Type.DOUBLE) throw Exception("Numeric values expected")
        val rightRes = visit(mulDivExpression.rightExpression)
        if(rightRes.getType() != Type.INT && rightRes.getType() != Type.DOUBLE) throw Exception("Numeric values expected")


        if(mulDivExpression.mulDivOperator == MulDivOperator.DIVIDE && rightRes.getValue() == 0) throw Exception("Divide by 0")

        if(leftRes.getType() == Type.INT && rightRes.getType() == Type.INT){
            val leftVal = leftRes.getValue() as Int
            val rightVal = rightRes.getValue() as Int

            return when(mulDivExpression.mulDivOperator){
                MulDivOperator.MULTIPLY -> IntExpressionResult(leftVal * rightVal)
                MulDivOperator.DIVIDE -> IntExpressionResult(leftVal / rightVal)
            }
        }else{
            val leftVal = leftRes.getValue() as Double
            val rightVal = rightRes.getValue() as Double

            return when(mulDivExpression.mulDivOperator){
                MulDivOperator.MULTIPLY -> DoubleExpressionResult(leftVal * rightVal)
                MulDivOperator.DIVIDE -> DoubleExpressionResult(leftVal / rightVal)
            }
        }
    }

    override fun visit(negativeExpression: NegativeExpression): ExpressionResult {
        val res = visit(negativeExpression.expression)

        if(res.getType() != Type.BOOL) throw Exception("Boolean value expected")
        return BoolExpressionResult(!(res.getValue() as Boolean))
    }

    override fun visit(relationalExpression: RelationalExpression): ExpressionResult {
        val leftRes = visit(relationalExpression.leftExpression)
        if(leftRes.getType() != Type.INT && leftRes.getType() != Type.DOUBLE) throw Exception("Numeric values expected")
        val rightRes = visit(relationalExpression.rightExpression)
        if(rightRes.getType() != Type.INT && rightRes.getType() != Type.DOUBLE) throw Exception("Numeric values expected")


        if(leftRes.getType() == Type.INT && rightRes.getType() == Type.INT){
            val leftVal = leftRes.getValue() as Int
            val rightVal = rightRes.getValue() as Int

            return when(relationalExpression.relationalOperator){
                RelationalOperator.EQUAL -> BoolExpressionResult(leftVal == rightVal)
                RelationalOperator.NOT_EQUAL -> BoolExpressionResult(leftVal != rightVal)
                RelationalOperator.MORE_EXCLUSIVE -> BoolExpressionResult(leftVal > rightVal)
                RelationalOperator.MORE_INCLUSIVE -> BoolExpressionResult(leftVal >= rightVal)
                RelationalOperator.LESS_EXCLUSIVE -> BoolExpressionResult(leftVal < rightVal)
                RelationalOperator.LESS_INCLUSIVE -> BoolExpressionResult(leftVal <= rightVal)
            }
        }else{
            val leftVal = leftRes.getValue() as Double
            val rightVal = rightRes.getValue() as Double

            return when(relationalExpression.relationalOperator){
                RelationalOperator.EQUAL -> BoolExpressionResult(leftVal == rightVal)
                RelationalOperator.NOT_EQUAL -> BoolExpressionResult(leftVal != rightVal)
                RelationalOperator.MORE_EXCLUSIVE -> BoolExpressionResult(leftVal > rightVal)
                RelationalOperator.MORE_INCLUSIVE -> BoolExpressionResult(leftVal >= rightVal)
                RelationalOperator.LESS_EXCLUSIVE -> BoolExpressionResult(leftVal < rightVal)
                RelationalOperator.LESS_INCLUSIVE -> BoolExpressionResult(leftVal <= rightVal)
            }
        }

    }

    override fun visit(textValue: TextValue): ExpressionResult {
        return StringExpressionResult(textValue.value)
    }

    override fun visit(unaryExpression: UnaryExpression): ExpressionResult {
        val res = visit(unaryExpression.expression)
        if(res.getType() == Type.INT){
            return IntExpressionResult(res.getValue() as Int)
        } else if(res.getType() == Type.DOUBLE){
            return DoubleExpressionResult(res.getValue() as Double)
        }
        throw Exception("Numeric value expected")
    }

    override fun visit(variableExpression: VariableExpression): ExpressionResult {
        for (functionScope in functionCallContexts.peek().functionScopes){
            for(variableDataContext in functionScope.variableDataContexts){
                if(variableDataContext.name == variableExpression.identifier){
                    val variableDataContextValue = variableDataContext.value
                    if(variableDataContextValue == null) throw Exception("Memory violation on ${variableDataContext.name} variable")
                    else return variableDataContextValue
                }
            }
        }
        throw Exception("Variable ${variableExpression.identifier} is not defined")
    }

    override fun visit(operation: Operation): ExpressionResult? {
        return operation.accept(this)
    }

    override fun visit(statement: Statement) {
        statement.accept(this)
    }

    override fun visit(instruction: Instruction): ExpressionResult? {
        return instruction.accept(this)
    }

    override fun visit(functionCall: FunctionCall): ExpressionResult? {
        val function = functionDeclarations.firstOrNull{ it.identifier == functionCall.identifier }
            ?: throw Exception("Unresolved reference to function: ${functionCall.identifier}")

        functionCallContexts.add(FunctionCallContext(function.identifier, mutableListOf(FunctionScope(mutableListOf()))))

        val variableDataContexts = functionCallContexts.peek().functionScopes.last().variableDataContexts

        for((index, arg) in functionCall.args.withIndex()){
            val expressionResult = visit(arg.primaryExpression)

            val variableDataContext = variableDataContexts.firstOrNull{ it.name == function.argsDefList[index].name }
            if(variableDataContext != null){
                variableDataContext.value = expressionResult
            }else{
                variableDataContexts.add(VariableDataContext(expressionResult.getType(),function.argsDefList[index].name,expressionResult))
            }
        }
        val result = visit(function.block)
        functionCallContexts.removeLast()

        return result
    }

    override fun visit(ifStatement: IfStatement) {
        val expressionResult = visit(ifStatement.condition)
        if(expressionResult.getType() != Type.BOOL) throw Exception("Condition must be boolean type")

        functionCallContexts.last().functionScopes.add(FunctionScope(mutableListOf()))
        if((expressionResult.getValue() as Boolean)){
            visit(ifStatement.ifTrue)
        } else{
            if(ifStatement.ifElse != null){
                visit(ifStatement.ifElse)
            }
        }
        functionCallContexts.last().functionScopes.removeLast()

    }

    override fun visit(returnExpression: ReturnExpression):ExpressionResult {
        return visit(returnExpression.expression)
    }
}