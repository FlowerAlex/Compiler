package parser

import nodes.*
import nodes.Function
import nodes.expressions.*
import nodes.expressions.operators.AddSubOperator
import nodes.expressions.operators.LogicalOperator
import nodes.expressions.operators.MulDivOperator
import nodes.expressions.operators.RelationalOperator
import nodes.operations.*
import scanner.IScanner
import token.Token
import token.TokenType

class Parser(
    private val scanner: IScanner,
    ) :IParser{

    private var currentToken: Token? = null

    override fun buildTree(): RootNode {
        return buildRootNode()
    }

    private fun buildRootNode(): RootNode {
        currentToken = scanner.getNextToken()

        val functions = buildFunctionsNode()
        val operations = buildOperationsNode()

        return  RootNode(functions,operations,scanner.getTokenPosition())
    }

    private fun buildFunctionsNode(): MutableList<Function>{
        val functions = mutableListOf<Function>()

        var functionNode : Function? = buildFunctionNode()
        while (functionNode != null){
            functions.add(functionNode)
            functionNode = buildFunctionNode()
        }
        return functions
    }

    private fun buildFunctionNode(): Function?{
        if(currentToken?.tokenType != TokenType.FUN){
            return null
        }
        currentToken = scanner.getNextToken()
        expect(TokenType.IDENTIFIER)
        val identifier = currentToken?.token.toString()

        currentToken = scanner.getNextToken()
        expect(TokenType.LEFT_ROUND_BRACKET)

        currentToken = scanner.getNextToken()
        val argsDefList = buildArgsDefinitionList()

        expect(TokenType.RIGHT_ROUND_BRACKET)
        currentToken = scanner.getNextToken()

        var returnType: Type? = null
        if(currentToken?.tokenType == TokenType.COLON){
            returnType = buildColonWithType()
        }

        expect(TokenType.LEFT_CURLY_BRACKET)
        currentToken = scanner.getNextToken()

        var operations: MutableList<Operation> = mutableListOf()
        if(currentToken?.tokenType != TokenType.RIGHT_CURLY_BRACKET){
            operations = buildOperationsNode()
        }

        expect(TokenType.RIGHT_CURLY_BRACKET)
        currentToken = scanner.getNextToken()

        return Function(identifier,argsDefList,returnType, Block(operations,scanner.getTokenPosition()), scanner.getTokenPosition())
    }

    private fun buildOperationsNode(): MutableList<Operation>{
        val operations = mutableListOf<Operation>()

        while (currentToken?.tokenType != TokenType.EOF && currentToken?.tokenType != TokenType.RIGHT_CURLY_BRACKET){
            when (currentToken?.tokenType){
                TokenType.IDENTIFIER -> {
                    val identifier = currentToken?.token as String
                    currentToken = scanner.getNextToken()
                    operations.add(buildVariableAssignmentOrDeclarationOrFunctionCall(identifier))

                    expect(TokenType.SEMICOLON)
                    currentToken = scanner.getNextToken()
                }
                TokenType.RETURN -> {
                    currentToken = scanner.getNextToken()
                    operations.add(ReturnExpression(buildExpression(),scanner.getTokenPosition()))

                    expect(TokenType.SEMICOLON)
                    currentToken = scanner.getNextToken()
                }
                TokenType.IF -> {
                    operations.add(buildIfStatement())
                }
                TokenType.WHILE -> {
                    operations.add(buildWhileStatement())
                }
                else -> throw Exception("Unexpected behaviour")
            }
        }
        return operations
    }

    private fun buildWhileStatement(): WhileStatement {
        expect(TokenType.WHILE)
        currentToken = scanner.getNextToken()

        expect(TokenType.LEFT_ROUND_BRACKET)
        currentToken = scanner.getNextToken()

        val condition = buildExpression()

        expect(TokenType.RIGHT_ROUND_BRACKET)
        currentToken = scanner.getNextToken()

        expect(TokenType.LEFT_CURLY_BRACKET)
        currentToken = scanner.getNextToken()

        val operationsForTrue = buildOperationsNode()

        expect(TokenType.RIGHT_CURLY_BRACKET)
        currentToken = scanner.getNextToken()

        return WhileStatement(condition, Block(operationsForTrue,scanner.getTokenPosition()),scanner.getTokenPosition())
    }

    private fun buildIfStatement(): IfStatement {
        expect(TokenType.IF)
        currentToken = scanner.getNextToken()

        expect(TokenType.LEFT_ROUND_BRACKET)
        currentToken = scanner.getNextToken()

        val condition = buildExpression()

        expect(TokenType.RIGHT_ROUND_BRACKET)
        currentToken = scanner.getNextToken()

        expect(TokenType.LEFT_CURLY_BRACKET)
        currentToken = scanner.getNextToken()

        val operationsForTrue = buildOperationsNode()

        expect(TokenType.RIGHT_CURLY_BRACKET)
        currentToken = scanner.getNextToken()

        var operationsForFalse: MutableList<Operation>? = null
        if(currentToken?.tokenType == TokenType.ELSE){
            currentToken = scanner.getNextToken()
            expect(TokenType.LEFT_CURLY_BRACKET)

            currentToken = scanner.getNextToken()
            operationsForFalse = buildOperationsNode()

            expect(TokenType.RIGHT_CURLY_BRACKET)
            currentToken = scanner.getNextToken()
        }

        return if(operationsForFalse != null){
            IfStatement(condition, Block(operationsForTrue,scanner.getTokenPosition()), Block(operationsForFalse,scanner.getTokenPosition()),scanner.getTokenPosition())
        }else{
            IfStatement(condition, Block(operationsForTrue,scanner.getTokenPosition()),null,scanner.getTokenPosition())
        }
    }

    private fun buildVariableAssignmentOrDeclarationOrFunctionCall(identifier: String) : Operation {
        when (currentToken?.tokenType){
            TokenType.COLON -> {
                val type = buildColonWithType()
                return if (currentToken?.tokenType == TokenType.ASSIGN){
                    currentToken = scanner.getNextToken()
                    VariableDeclaration(identifier,type,buildExpression(),scanner.getTokenPosition())
                }else{
                    VariableDeclaration(identifier,type,null,scanner.getTokenPosition())
                }
            }
            TokenType.ASSIGN -> {
                currentToken = scanner.getNextToken()
                return VariableAssignment(identifier,buildExpression(),scanner.getTokenPosition())
            }
            TokenType.LEFT_ROUND_BRACKET ->{
                currentToken = scanner.getNextToken()
                val argsList = buildFunctionArgumentList()
                expect(TokenType.RIGHT_ROUND_BRACKET)
                currentToken = scanner.getNextToken()

                return FunctionCall(identifier,argsList,scanner.getTokenPosition())
            }
            else -> throw Exception("Unexpected behaviour")
        }
    }

    private fun buildArgsDefinitionList(): MutableList<ArgumentDefinition>{ //
        val argsDefinitionList = mutableListOf<ArgumentDefinition>()
        while (currentToken?.tokenType != TokenType.RIGHT_ROUND_BRACKET) {
            if(argsDefinitionList.size != 0){
                expect(TokenType.COMMA)
                currentToken = scanner.getNextToken()
            }
            expect(TokenType.IDENTIFIER)
            val name = currentToken?.token.toString()
            currentToken = scanner.getNextToken()
            val type = buildColonWithType()

            argsDefinitionList.add(ArgumentDefinition(name,type))
        }

        return argsDefinitionList
    }

    private fun buildColonWithType(): Type {
        var returnType: Type? = null
        if(currentToken?.tokenType == TokenType.COLON){
            currentToken = scanner.getNextToken()
            var isType = false
            if(currentToken?.tokenType == TokenType.INT){
                returnType = Type.INT
                isType = true
            }

            if(currentToken?.tokenType == TokenType.DOUBLE){
                returnType = Type.DOUBLE
                isType = true
            }

            if(currentToken?.tokenType == TokenType.BOOL){
                returnType = Type.BOOL
                isType = true
            }
            if(!isType) throw Exception("Unexpected behaviour")
            currentToken = scanner.getNextToken()
        }
        return returnType!!
    }

    private fun buildExpression(): Expression { // if have nulls return NegativeExrpession
        val negativeExpression = buildNegativeExpression()

        val logicalOperator = when (currentToken?.tokenType) {
            TokenType.AND -> LogicalOperator.AND
            TokenType.OR ->
                LogicalOperator.OR
            else -> null
        }
        var logicalExpression: Expression? = null
        if(logicalOperator != null){
            currentToken = scanner.getNextToken()
            logicalExpression = buildExpression()
        }

        if(negativeExpression !=null){
            return if(logicalOperator != null && logicalExpression != null){
                LogicalExpression(negativeExpression,logicalOperator,logicalExpression,scanner.getTokenPosition())
            }else {
                negativeExpression
            }
        }
        throw Exception("Unexpected behaviour")
    }

    private fun buildNegativeExpression(): Expression?{
        var isExclamationMark = false
        if(currentToken?.tokenType == TokenType.EXCLAMATION_MARK){
            isExclamationMark = true
            currentToken = scanner.getNextToken()
        }
        val relationalExpression = buildRelationalExpression()

        if(relationalExpression != null){
            return if(isExclamationMark){
                NegativeExpression(relationalExpression,scanner.getTokenPosition())
            } else {
                relationalExpression
            }
        }
        return null
    }

    private fun buildRelationalExpression(): Expression?{
        val addSubExpression = buildAddSubExpression()

        val relationalOperator = when (currentToken?.tokenType) {
            TokenType.EQUAL -> RelationalOperator.EQUAL
            TokenType.NOT_EQUAL ->
                RelationalOperator.NOT_EQUAL
            TokenType.LESS_EXCLUSIVE -> RelationalOperator.LESS_EXCLUSIVE
            TokenType.LESS_INCLUSIVE -> RelationalOperator.LESS_INCLUSIVE
            TokenType.MORE_EXCLUSIVE -> RelationalOperator.MORE_EXCLUSIVE
            TokenType.MORE_INCLUSIVE -> RelationalOperator.MORE_INCLUSIVE
            else -> null
        }
        var relationalExpression: Expression? = null
        if(relationalOperator != null){
            currentToken = scanner.getNextToken()
            relationalExpression = buildRelationalExpression()
        }

        if(addSubExpression !=null){
            return if(relationalOperator != null && relationalExpression != null){
                RelationalExpression(addSubExpression,relationalOperator,relationalExpression,scanner.getTokenPosition())
            }else {
                addSubExpression
            }
        }
        return null
    }

    private fun buildAddSubExpression(): Expression?{
        val mulDivExpression = buildMulDivExpression()

        val addSubOperator = when (currentToken?.tokenType) {
            TokenType.PLUS -> AddSubOperator.PLUS
            TokenType.MINUS -> AddSubOperator.MINUS
            else -> null
        }
        var addSubExpression: Expression? = null
        if(addSubOperator != null){
            currentToken = scanner.getNextToken()
            addSubExpression = buildAddSubExpression()
        }

        if(mulDivExpression !=null){
            return if(addSubOperator != null && addSubExpression != null){
                 AddSubExpression(mulDivExpression,addSubOperator,addSubExpression,scanner.getTokenPosition())
            }else {
                mulDivExpression
            }

        }
        return null
    }

    private fun buildMulDivExpression(): Expression?{
        val unaryExpression = buildUnaryExpression()

        val mulDivOperator = when (currentToken?.tokenType) {
            TokenType.MULTIPLY -> MulDivOperator.MULTIPLY
            TokenType.DIVIDE -> MulDivOperator.DIVIDE
            else -> null
        }
        var mulDivExpression: Expression? = null
        if(mulDivOperator != null){
            currentToken = scanner.getNextToken()
            mulDivExpression = buildMulDivExpression()
        }

        if(unaryExpression !=null){
            return if(mulDivOperator != null && mulDivExpression != null){
                MulDivExpression(unaryExpression,mulDivOperator,mulDivExpression,scanner.getTokenPosition())
            }else {
                unaryExpression
            }

        }
        return null
    }

    private fun buildUnaryExpression(): Expression?{
        var isMinus = false
        if(currentToken?.tokenType == TokenType.MINUS){
            isMinus = true
            currentToken = scanner.getNextToken()
        }

        val primaryExpression = buildPrimaryExpression()

        if(primaryExpression != null){
            return if(isMinus){
                UnaryExpression(primaryExpression,scanner.getTokenPosition())
            }else{
                primaryExpression
            }
        }
        return null
    }

    private fun buildPrimaryExpression(): Expression?{
        return when(currentToken?.tokenType){
            TokenType.VALUE_INT -> {
                val intValue = IntValue(currentToken?.token as Int,scanner.getTokenPosition())
                currentToken = scanner.getNextToken()
                intValue
            }
            TokenType.VALUE_DOUBLE -> {
                val doubleValue = DoubleValue(currentToken?.token as Double,scanner.getTokenPosition())
                currentToken = scanner.getNextToken()
                doubleValue
            }
            TokenType.TRUE -> {
                val boolValue = BoolValue(true,scanner.getTokenPosition())
                currentToken = scanner.getNextToken()
                boolValue
            }
            TokenType.TEXT -> {
                val textValue = TextValue(currentToken?.token as String,scanner.getTokenPosition())
                currentToken = scanner.getNextToken()
                textValue
            }
            TokenType.FALSE -> {
                val boolValue = BoolValue(false,scanner.getTokenPosition())
                currentToken = scanner.getNextToken()
                boolValue
            }
            TokenType.LEFT_ROUND_BRACKET -> {
                currentToken = scanner.getNextToken()
                val expression = buildExpression()

                expect(TokenType.RIGHT_ROUND_BRACKET)
                currentToken = scanner.getNextToken()

                return expression
            }
            TokenType.IDENTIFIER ->{
                val identifier = currentToken?.token as String

                currentToken = scanner.getNextToken()
                if(currentToken?.tokenType == TokenType.LEFT_ROUND_BRACKET){
                    currentToken = scanner.getNextToken()
                    val argsList = buildFunctionArgumentList()
                    expect(TokenType.RIGHT_ROUND_BRACKET)
                    currentToken = scanner.getNextToken()

                    return FunctionCallExpression(FunctionCall(identifier,argsList,scanner.getTokenPosition()),scanner.getTokenPosition())
                }

                return VariableExpression(identifier,scanner.getTokenPosition())
            }
            else -> return null
        }
    }

    private fun buildFunctionArgumentList(): MutableList<FunctionArgument>{
        val functionArgumentList = mutableListOf<FunctionArgument>()

        while (currentToken?.tokenType != TokenType.RIGHT_ROUND_BRACKET){
            if(functionArgumentList.size != 0){
                expect(TokenType.COMMA)
                currentToken = scanner.getNextToken()
            }
            val primaryExpression = buildExpression()

            functionArgumentList.add(FunctionArgument(primaryExpression))
        }

        return functionArgumentList
    }

    private fun expect(tokenType: TokenType){
        if(currentToken?.tokenType != tokenType){
            throw Exception("Unexpected behaviour. Position ${scanner.getTokenPosition()}")
        }
    }
}