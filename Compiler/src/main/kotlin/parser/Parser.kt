package parser

import scanner.Scanner
import token.Token
import token.TokenType

class Parser(
    private val scanner: Scanner,
    ) :IParser{

    private var currentToken: Token? = null

    override fun buildTree(): RootNode {
        return buildRootNode()
    }

    private fun buildRootNode(): RootNode{
        currentToken = scanner.getNextToken()

        val functions = buildFunctionsNode()
        val operations = buildOperationsNode()

        return  RootNode(functions,operations)
    }

    private fun buildFunctionsNode(): MutableList<Function>{ // FIX!!!
        val functions = mutableListOf<Function>()
        while (currentToken?.tokenType == TokenType.FUN){

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

            functions.add(Function(identifier,argsDefList,returnType,Block(operations)))
        }
        return  functions
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
                    operations.add(Return(Expression(buildExpression())))

                    expect(TokenType.SEMICOLON)
                    currentToken = scanner.getNextToken()
                }
                TokenType.IF -> {
                    currentToken = scanner.getNextToken()
                    operations.add(buildIfStatement())
                }
                TokenType.WHILE -> {
                    currentToken = scanner.getNextToken()
                    operations.add(buildWhileStatement())
                }
                else -> throw Exception("Unexpected behaviour")
            }
        }
        return operations
    }

    private fun buildWhileStatement():WhileStatement{
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

        return WhileStatement(Expression(condition),Block(operationsForTrue))
    }

    private fun buildIfStatement():IfStatement{
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
            IfStatement(Expression(condition),Block(operationsForTrue),Block(operationsForFalse))
        }else{
            IfStatement(Expression(condition),Block(operationsForTrue),null)
        }
    }

    private fun buildVariableAssignmentOrDeclarationOrFunctionCall(identifier: String) : Operation{
        when (currentToken?.tokenType){
            TokenType.COLON -> {
                val type = buildColonWithType()
                return if (currentToken?.tokenType == TokenType.ASSIGN){
                    currentToken = scanner.getNextToken()
                    VariableDeclaration(identifier,type,Expression(buildExpression()))
                }else{
                    VariableDeclaration(identifier,type,null)
                }
            }
            TokenType.ASSIGN -> {
                currentToken = scanner.getNextToken()
                return VariableAssignment(identifier,Expression(buildExpression()))
            }
            TokenType.LEFT_ROUND_BRACKET ->{
                currentToken = scanner.getNextToken()
                val argsList = buildFunctionArgumentList()
                expect(TokenType.RIGHT_ROUND_BRACKET)
                currentToken = scanner.getNextToken()

                return FunctionCall(identifier,argsList)
            }
            else -> throw Exception("Unexpected behaviour")
        }
    }

    private fun buildArgsDefinitionList(): MutableList<ArgumentDefinition>{
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

    private fun buildColonWithType(): Type{
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

    private fun buildExpression(): LogicalExpression{
        val negativeExpression = buildNegativeExpression()

        val logicalOperator = when (currentToken?.tokenType) {
            TokenType.AND -> LogicalOperator.AND
            TokenType.OR ->
                LogicalOperator.OR
            else -> null
        }
        var logicalExpression: LogicalExpression? = null
        if(logicalOperator != null){
            currentToken = scanner.getNextToken()
            logicalExpression = buildExpression()
        }

        return LogicalExpression(negativeExpression,logicalOperator,logicalExpression)
    }

    private fun buildNegativeExpression(): NegativeExpression{
        var isExclamationMark = false
        if(currentToken?.tokenType == TokenType.EXCLAMATION_MARK){
            isExclamationMark = true
            currentToken = scanner.getNextToken()
        }

        return NegativeExpression(isExclamationMark,buildRelationalExpression())
    }

    private fun buildRelationalExpression(): RelationalExpression{
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
        var relationalExpression: RelationalExpression? = null
        if(relationalOperator != null){
            currentToken = scanner.getNextToken()
            relationalExpression = buildRelationalExpression()
        }

        return RelationalExpression(addSubExpression,relationalOperator,relationalExpression)
    }

    private fun buildAddSubExpression():AddSubExpression{
        val mulDivExpression = buildMulDivExpression()

        val addSubOperator = when (currentToken?.tokenType) {
            TokenType.PLUS -> AddSubOperator.PLUS
            TokenType.MINUS -> AddSubOperator.MINUS
            else -> null
        }
        var addSubExpression: AddSubExpression? = null
        if(addSubOperator != null){
            currentToken = scanner.getNextToken()
            addSubExpression = buildAddSubExpression()
        }

        return AddSubExpression(mulDivExpression,addSubOperator,addSubExpression)
    }

    private fun buildMulDivExpression(): MulDivExpression{
        val unaryExpression = buildUnaryExpression()

        val mulDivOperator = when (currentToken?.tokenType) {
            TokenType.MULTIPLY -> MulDivOperator.MULTIPLY
            TokenType.DIVIDE -> MulDivOperator.DIVIDE
            else -> null
        }
        var mulDivExpression: MulDivExpression? = null
        if(mulDivOperator != null){
            currentToken = scanner.getNextToken()
            mulDivExpression = buildMulDivExpression()
        }

        return MulDivExpression(unaryExpression,mulDivOperator,mulDivExpression)
    }

    private fun buildUnaryExpression(): UnaryExpression{
        var isMinus = false
        if(currentToken?.tokenType == TokenType.MINUS){
            isMinus = true
            currentToken = scanner.getNextToken()
        }

        return UnaryExpression(isMinus,buildPrimaryExpression())
    }

    private fun buildPrimaryExpression(): PrimaryExpression{
        return when(currentToken?.tokenType){
            TokenType.VALUE_INT -> {
                val intValue = IntValue(currentToken?.token as Int)
                currentToken = scanner.getNextToken()
                intValue
            }
            TokenType.VALUE_DOUBLE -> {
                val doubleValue = DoubleValue(currentToken?.token as Double)
                currentToken = scanner.getNextToken()
                doubleValue
            }
            TokenType.TRUE -> {
                val boolValue = BoolValue(true)
                currentToken = scanner.getNextToken()
                boolValue
            }
            TokenType.TEXT -> {
                val textValue = TextValue(currentToken?.token as String)
                currentToken = scanner.getNextToken()
                textValue
            }
            TokenType.FALSE -> {
                val boolValue = BoolValue(false)
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

                    return FunctionCallExpression(FunctionCall(identifier,argsList))
                }

                return VariableExpression(identifier)
            }
            else -> throw Exception("Unexpected behaviour")
        }
    }

    private fun buildFunctionArgumentList(): MutableList<FunctionArgument>{
        val functionArgumentList = mutableListOf<FunctionArgument>()

        while (currentToken?.tokenType != TokenType.RIGHT_ROUND_BRACKET){
            if(functionArgumentList.size != 0){
                expect(TokenType.COMMA)
                currentToken = scanner.getNextToken()
            }
            functionArgumentList.add(FunctionArgument(buildPrimaryExpression()))
        }

        return functionArgumentList
    }

    private fun expect(tokenType: TokenType){
        if(currentToken?.tokenType != tokenType){
            throw Exception("Unexpected behaviour")
        }
    }
}