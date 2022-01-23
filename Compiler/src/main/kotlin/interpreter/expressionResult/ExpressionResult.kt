package interpreter.expressionResult

import nodes.Type

abstract class ExpressionResult(){
    abstract fun getValue(): Any
    abstract fun getType(): Type
}

class BoolExpressionResult(var value: Boolean):ExpressionResult() {
    override fun getValue(): Any {
        return value
    }

    override fun getType(): Type {
        return Type.BOOL
    }
}

class IntExpressionResult(var value: Int):ExpressionResult() {
    override fun getValue(): Any {
        return value
    }

    override fun getType(): Type {
        return Type.INT
    }
}

class DoubleExpressionResult(var value: Double):ExpressionResult() {
    override fun getValue(): Any {
        return value
    }

    override fun getType(): Type {
        return Type.DOUBLE
    }
}
class StringExpressionResult(var value: String):ExpressionResult() {
    override fun getValue(): Any {
        return value
    }

    override fun getType(): Type {
        return Type.STRING
    }
}
