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
    val primaryExpression: Expression,
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

class LogicalExpression(
    val leftExpression: Expression,
    val logicalOperator: LogicalOperator,
    val rightExpression: Expression,
) : Expression()

enum class LogicalOperator{
    AND,
    OR,
}

class NegativeExpression(
    val expression: Expression,
):Expression()

class RelationalExpression(
    val leftExpression: Expression,
    val relationalOperator: RelationalOperator,
    val rightExpression: Expression,
):Expression()

enum class RelationalOperator{
    EQUAL,
    NOT_EQUAL,
    MORE_EXCLUSIVE,
    MORE_INCLUSIVE,
    LESS_EXCLUSIVE,
    LESS_INCLUSIVE,
}

class AddSubExpression(
    val leftExpression: Expression,
    val addSubOperator: AddSubOperator,
    val rightExpression: Expression,
):Expression()

enum class AddSubOperator{
    PLUS,
    MINUS,
}

class MulDivExpression(
    val leftExpression: Expression,
    val mulDivOperator: MulDivOperator,
    val rightExpression: Expression,
):Expression()

enum class MulDivOperator{
    MULTIPLY,
    DIVIDE,
}

class UnaryExpression(
    val expression: Expression,
):Expression()

abstract class Expression

class IntValue(
    val value: Int,
): Expression()

class DoubleValue(
    val value: Double,
): Expression()

class BoolValue(
    val value: Boolean,
): Expression()

class TextValue(
    val value: String,
): Expression()

class VariableExpression(
    val identifier: String,
): Expression()

class FunctionCallExpression(
    val functionCall: FunctionCall,
): Expression()