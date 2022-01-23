package interpreter

class FunctionCallContext(val functionName:String = "main", val functionScopes: MutableList<FunctionScope> = mutableListOf(FunctionScope()))