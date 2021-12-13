package parser

enum class Type {
    INT,
    DOUBLE,
    BOOL
}

class RootNode( // Program
    val functions:MutableList<Function>,
    val operations: MutableList<Operation>,
)

class Function ( // Function
    val identifier : String,
    val argsDefList: MutableList<ArgumentDefinition>,
    val returnType: Type?,
    val block: Block,
)

class ArgumentDefinition(
    val name: String,
    val type:Type,
) // ArgsDefList

class Block( // Block
    val operations: MutableList<Operation>,
)

abstract class Operation // Instruction and Statement

open class Instruction(
) : Operation() // Instruction

class VariableAssignment(
    val identifier: String,
    val expression: Expression,
) : Instruction()

class VariableDeclaration(
    val identifier: String,
    val type: Type,
    val expression: Expression?,
) : Instruction()

class FunctionCall(
    val identifier: String,
    val args: MutableList<FunctionArgument>,
) : Instruction()

class Return(
    val expression: Expression,
) : Instruction()

class FunctionArgument(
    val primaryExpression: PrimaryExpression,
)
open class Statement : Operation() // Statement

class IfStatement(
    val condition: Expression,
    val ifTrue: Block,
    val ifElse: Block?,
) : Statement()

class WhileStatement(
    val condition: Expression,
    val ifTrue: Block,
) : Statement()

class Expression(
    val logicalExpression: LogicalExpression,
) : PrimaryExpression()

class LogicalExpression(
    val negativeExpression: NegativeExpression,
    val logicalOperator: LogicalOperator?,
    val logicalExspression: LogicalExpression?,
) : PrimaryExpression()

enum class LogicalOperator{
    AND,
    OR,
}

class NegativeExpression(
    val isNegative: Boolean,
    val relationalExpression: RelationalExpression,
)

class RelationalExpression(
    val addSubExpression: AddSubExpression,
    val relationalOperator: RelationalOperator?,
    val relationalExpression: RelationalExpression?,
)

enum class RelationalOperator{
    EQUAL,
    NOT_EQUAL,
    MORE_EXCLUSIVE,
    MORE_INCLUSIVE,
    LESS_EXCLUSIVE,
    LESS_INCLUSIVE,
}

class AddSubExpression(
    val mulDivExpression: MulDivExpression,
    val addSubOperator: AddSubOperator?,
    val addSubExpression: AddSubExpression?,
)

enum class AddSubOperator{
    PLUS,
    MINUS,
}

class MulDivExpression(
    val unaryExpression: UnaryExpression,
    val mulDivOperator: MulDivOperator?,
    val mulDivExpression: MulDivExpression?,
)

enum class MulDivOperator{
    MULTIPLY,
    DIVIDE,
}

class UnaryExpression(
    val withUnaryOperator: Boolean,
    val primaryExpression: PrimaryExpression,
)

abstract class PrimaryExpression

class IntValue(
    val value: Int,
): PrimaryExpression()

class DoubleValue(
    val value: Double,
): PrimaryExpression()

class BoolValue(
    val value: Boolean,
): PrimaryExpression()

class TextValue(
    val value: String,
): PrimaryExpression()

class VariableExpression(
    val identifier: String,
): PrimaryExpression()

class FunctionCallExpression(
    val functionCall: FunctionCall,
): PrimaryExpression()