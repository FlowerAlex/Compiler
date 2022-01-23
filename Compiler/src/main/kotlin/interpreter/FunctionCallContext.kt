package interpreter

class FunctionCallContext(val functionName:String, val functionScopes: MutableList<FunctionScope>)