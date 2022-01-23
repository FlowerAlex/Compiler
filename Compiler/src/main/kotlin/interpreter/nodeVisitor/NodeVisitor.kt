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
):INodeVisitor {
    private val functionDeclarations: MutableList<Function>
    private val functionCallContexts: Stack<FunctionCallContext>
    private var isReturningState = false
    init {
        val printFunction = Function(
            "print",
            mutableListOf(
                ArgumentDefinition("text", Type.ANY)),
            null,
            Block(mutableListOf(), Pair(-1,-1)),
            Pair(-1,-1)
        )

        functionDeclarations = mutableListOf(printFunction)
        functionCallContexts = Stack<FunctionCallContext>()
        functionCallContexts.add(FunctionCallContext())
    }

    override fun visit(variableDeclaration: VariableDeclaration) {
        if(functionCallContexts.last().functionScopes.any{it.variableDataContexts.any{variableDataContexts -> variableDataContexts.name == variableDeclaration.identifier} }) throw Exception("Variable ${variableDeclaration.identifier} with the same name already defined. Position:${variableDeclaration.pos}")

        var expressionResult = if (variableDeclaration.expression != null) visit(variableDeclaration.expression) else null

        if(expressionResult != null){
            if(variableDeclaration.type == Type.DOUBLE && expressionResult.getType() == Type.INT){
                expressionResult = DoubleExpressionResult((expressionResult.getValue() as Int).toDouble())
            }else if(variableDeclaration.type != expressionResult.getType()) throw Exception("Incompatible assignment type for ${variableDeclaration.identifier}. Position:${variableDeclaration.pos}")
        }

        functionCallContexts.peek().functionScopes.last().variableDataContexts.add(VariableDataContext(variableDeclaration.type,variableDeclaration.identifier, expressionResult))
    }

    override fun visit(whileStatement: WhileStatement):ExpressionResult? {
        var conditionExpressionResult = visit(whileStatement.condition)
        if(conditionExpressionResult.getType() != Type.BOOL) throw Exception("Condition must be boolean type. Position:${whileStatement.pos}")

        while((conditionExpressionResult.getValue() as Boolean)){
            val res = visit(whileStatement.ifTrue)
            if(isReturningState){
                return res
            }
            conditionExpressionResult = visit(whileStatement.condition)
        }
        return null
    }

    override fun visit(rootNode: RootNode):ExpressionResult? {
        for(function in rootNode.functions){
            visit(function)
        }

        var res: ExpressionResult? = null
        for(operation in rootNode.operations){
            res = visit(operation)
            if(isReturningState){
                return res
            }
        }

        return null
    }

    override fun visit(functionArgument: FunctionArgument):ExpressionResult {
        return visit(functionArgument.primaryExpression)
    }

    override fun visit(function: Function) {
        if(functionCallContexts.any{ it.functionName == function.identifier}) throw Exception("Function with name: ${function.identifier} already defined. Position:${function.pos}")
        functionDeclarations.add(function)
    }

    override fun visit(block: Block): ExpressionResult? {
        functionCallContexts.last().functionScopes.add(FunctionScope())
        for(operation in block.operations){
            val res = visit(operation)
            if(isReturningState){
                functionCallContexts.last().functionScopes.removeLast()
                return res
            }
        }
        functionCallContexts.last().functionScopes.removeLast()
        return null
    }

    override fun visit(variableAssignment: VariableAssignment) {
        var expressionResult = visit(variableAssignment.expression)

        var isValueExistInScope = false
        for (functionScope in functionCallContexts.peek().functionScopes){
            for(variableDataContext in functionScope.variableDataContexts){
                if(variableDataContext.name == variableAssignment.identifier){
                    if(!isValidAssignment(variableDataContext.type,expressionResult.getType(),variableAssignment.pos)) throw Exception("Incompatible type for ${variableAssignment.identifier} variable. Position:${variableAssignment.pos}")
                    if(variableDataContext.type == Type.DOUBLE && expressionResult.getType() == Type.INT)
                    {
                        expressionResult = DoubleExpressionResult((expressionResult.getValue() as Int).toDouble())
                    }
                    variableDataContext.value = expressionResult
                    isValueExistInScope = true
                    break
                }
            }
            if(isValueExistInScope) break
        }

        if(!isValueExistInScope){
            throw Exception("Variable does not exist. Position:${variableAssignment.pos}")
        }
    }

    private fun isValidAssignment(target: Type, source: Type, pos: Pair<Int,Int>): Boolean{
        return when(target){
            Type.INT -> source == Type.INT
            Type.DOUBLE -> source == Type.INT || source == Type.DOUBLE
            Type.BOOL -> source == Type.BOOL
            Type.STRING -> source == Type.STRING
            Type.ANY -> true
        }
    }

    override fun visit(expression: Expression): ExpressionResult {
        return expression.accept(this)
    }

    override fun visit(addSubExpression: AddSubExpression): ExpressionResult {
        val leftRes = visit(addSubExpression.leftExpression)
        if(leftRes.getType() != Type.INT && leftRes.getType() != Type.DOUBLE) throw Exception("Numeric values expected. Position:${addSubExpression.pos}")
        val rightRes = visit(addSubExpression.rightExpression)
        if(rightRes.getType() != Type.INT && rightRes.getType() != Type.DOUBLE) throw Exception("Numeric values expected. Position:${addSubExpression.pos}")

        if(leftRes.getType() == Type.INT && rightRes.getType() == Type.INT){
            val leftVal = leftRes.getValue() as Int
            val rightVal = rightRes.getValue() as Int

            return when(addSubExpression.addSubOperator){
                AddSubOperator.PLUS -> IntExpressionResult(leftVal+rightVal)
                AddSubOperator.MINUS -> IntExpressionResult(leftVal-rightVal)
            }
        }else{
            val leftVal = if(leftRes.getType() == Type.INT) (leftRes.getValue() as Int).toDouble() else leftRes.getValue() as Double
            val rightVal = if(rightRes.getType() == Type.INT) (rightRes.getValue() as Int).toDouble() else rightRes.getValue() as Double

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
            ?: throw Exception("Function: ${functionCallExpression.functionCall.identifier} should return value. Position:${functionCallExpression.pos}")
    }

    override fun visit(intValue: IntValue): ExpressionResult {
        return IntExpressionResult(intValue.value)
    }

    override fun visit(logicalExpression: LogicalExpression): ExpressionResult {
        val leftRes = visit(logicalExpression.leftExpression)
        if(leftRes.getType() != Type.BOOL) throw Exception("Boolean type expected. Position:${logicalExpression.pos}")
        val leftVal = leftRes.getValue() as Boolean

        if(leftVal && logicalExpression.logicalOperator == LogicalOperator.OR) {
            return BoolExpressionResult(true)
        }

        val rightRes = visit(logicalExpression.rightExpression)
        if(rightRes.getType() != Type.BOOL) throw Exception("Boolean type expected. Position:${logicalExpression.pos}")
        val rightVal = rightRes.getValue() as Boolean


        return when(logicalExpression.logicalOperator){
            LogicalOperator.AND -> BoolExpressionResult(leftVal && rightVal)
            LogicalOperator.OR -> BoolExpressionResult(leftVal || rightVal)
        }

    }

    override fun visit(mulDivExpression: MulDivExpression): ExpressionResult {
        val leftRes = visit(mulDivExpression.leftExpression)
        if(leftRes.getType() != Type.INT && leftRes.getType() != Type.DOUBLE) throw Exception("Numeric values expected. Position:${mulDivExpression.pos}")
        val rightRes = visit(mulDivExpression.rightExpression)
        if(rightRes.getType() != Type.INT && rightRes.getType() != Type.DOUBLE) throw Exception("Numeric values expected. Position:${mulDivExpression.pos}")


        if(mulDivExpression.mulDivOperator == MulDivOperator.DIVIDE && rightRes.getValue() == 0) throw Exception("Divide by 0. Position:${mulDivExpression.pos}")

        if(leftRes.getType() == Type.INT && rightRes.getType() == Type.INT){
            val leftVal = leftRes.getValue() as Int
            val rightVal = rightRes.getValue() as Int

            return when(mulDivExpression.mulDivOperator){
                MulDivOperator.MULTIPLY -> IntExpressionResult(leftVal * rightVal)
                MulDivOperator.DIVIDE -> IntExpressionResult(leftVal / rightVal)
            }
        }else{
            val leftVal = if(leftRes.getType() == Type.INT) (leftRes.getValue() as Int).toDouble() else leftRes.getValue() as Double
            val rightVal = if(rightRes.getType() == Type.INT) (rightRes.getValue() as Int).toDouble() else rightRes.getValue() as Double

            return when(mulDivExpression.mulDivOperator){
                MulDivOperator.MULTIPLY -> DoubleExpressionResult(leftVal * rightVal)
                MulDivOperator.DIVIDE -> DoubleExpressionResult(leftVal / rightVal)
            }
        }
    }

    override fun visit(negativeExpression: NegativeExpression): ExpressionResult {
        val res = visit(negativeExpression.expression)

        if(res.getType() != Type.BOOL) throw Exception("Boolean value expected. Position:${negativeExpression.pos}")
        return BoolExpressionResult(!(res.getValue() as Boolean))
    }

    override fun visit(relationalExpression: RelationalExpression): ExpressionResult {
        val leftRes = visit(relationalExpression.leftExpression)
        if(leftRes.getType() != Type.INT && leftRes.getType() != Type.DOUBLE) throw Exception("Numeric values expected. Position:${relationalExpression.pos}")
        val rightRes = visit(relationalExpression.rightExpression)
        if(rightRes.getType() != Type.INT && rightRes.getType() != Type.DOUBLE) throw Exception("Numeric values expected. Position:${relationalExpression.pos}")


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
            return IntExpressionResult(-1*(res.getValue() as Int))
        } else if(res.getType() == Type.DOUBLE){
            return DoubleExpressionResult(-1*(res.getValue() as Double))
        }
        throw Exception("Numeric value expected. Position:${unaryExpression.pos}")
    }

    override fun visit(variableExpression: VariableExpression): ExpressionResult {
        for (functionScope in functionCallContexts.peek().functionScopes){
            for(variableDataContext in functionScope.variableDataContexts){
                if(variableDataContext.name == variableExpression.identifier){
                    val variableDataContextValue = variableDataContext.value
                    if(variableDataContextValue == null) throw Exception("Memory violation on ${variableDataContext.name} variable. Position:${variableExpression.pos}")
                    else return variableDataContextValue
                }
            }
        }
        throw Exception("Variable ${variableExpression.identifier} is not defined. Position:${variableExpression.pos}")
    }

    override fun visit(operation: Operation): ExpressionResult? {
        return operation.accept(this)
    }

    override fun visit(statement: Statement):ExpressionResult? {
        return statement.accept(this)
    }

    override fun visit(instruction: Instruction): ExpressionResult? {
        return instruction.accept(this)
    }

    override fun visit(functionCall: FunctionCall): ExpressionResult? {
        val function = functionDeclarations.firstOrNull{ it.identifier == functionCall.identifier }
            ?: throw Exception("Unresolved reference to function: ${functionCall.identifier}. Position:${functionCall.pos}")

        val variableDataContexts = functionCallContexts.peek().functionScopes.last().variableDataContexts
        val newVariableDataContexts = mutableListOf<VariableDataContext>()

        if(functionCall.args.size != function.argsDefList.size) throw Exception("Not valid count of arguments for function: ${functionCall.identifier}. Position:${functionCall.pos}")

        for((index, arg) in functionCall.args.withIndex()){
            val expressionResult = visit(arg.primaryExpression)

            if(!isValidAssignment(function.argsDefList[index].type,expressionResult.getType(),functionCall.pos)) throw Exception("Incompatible ${function.argsDefList[index].name} function argument type. Position:${functionCall.pos}")

            newVariableDataContexts.add(VariableDataContext(expressionResult.getType(),function.argsDefList[index].name,expressionResult))

        }

        functionCallContexts.add(FunctionCallContext(function.identifier, mutableListOf(FunctionScope(newVariableDataContexts))))

        checkIsPrintFunction(function.identifier,newVariableDataContexts,function.pos)

        val result = visit(function.block)
        functionCallContexts.removeLast()
        isReturningState = false

        if(function.returnType == null && result?.getType() != null) throw Exception("Function: ${function.identifier} has void return type, but ${result.getType().name} found. Position:${functionCall.pos}")
        if(function.returnType == null && result?.getType() == null) return null
        if(function.returnType != null && result?.getType() != null){
            if(!isValidAssignment(function.returnType, result.getType(),functionCall.pos)) throw Exception("Function: ${function.identifier} has ${function.returnType} return type, but ${result.getType().name} found. Position:${functionCall.pos}")
        }

        return result
    }

    private fun checkIsPrintFunction(functionName: String, variableDataContexts: MutableList<VariableDataContext>,pos:Pair<Int,Int>){
        if(functionName == "print"){
            val text = variableDataContexts.firstOrNull{ it.name == "text" } ?: throw Exception("Invalid handling print function. Position:${pos}")
            when(text.type){
                Type.INT -> {
                    val textValue = text.value?.getValue()
                    if(textValue != null) print(textValue as Int)
                }
                Type.DOUBLE -> {
                    val textValue = text.value?.getValue()
                    if(textValue != null) print(textValue as Double)
                }
                Type.BOOL -> {
                    val textValue = text.value?.getValue()
                    if(textValue != null) print(textValue as Boolean)
                }
                Type.STRING -> {
                    val textValue = text.value?.getValue()
                    if(textValue != null) print(textValue as String)
                }
                Type.ANY -> throw Exception("Unexpected behaviour. Position:${pos}")
            }
        }
    }

    override fun visit(ifStatement: IfStatement):ExpressionResult? {
        val expressionResult = visit(ifStatement.condition)
        if(expressionResult.getType() != Type.BOOL) throw Exception("Condition must be boolean type. Position:${ifStatement.pos}")

        var result: ExpressionResult? = null
        if((expressionResult.getValue() as Boolean)){
            result = visit(ifStatement.ifTrue)
        } else{
            if(ifStatement.ifElse != null){
                result = visit(ifStatement.ifElse)
            }
        }

        return result
    }

    override fun visit(returnExpression: ReturnExpression):ExpressionResult {
        val res = visit(returnExpression.expression)
        isReturningState = true
        return res
    }
}