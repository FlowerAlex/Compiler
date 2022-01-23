package interpreter

import interpreter.expressionResult.ExpressionResult
import nodes.Type

class VariableDataContext(val type: Type, val name: String, var value: ExpressionResult?)