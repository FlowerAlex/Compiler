package parser

import nodes.RootNode

interface IParser {
    fun buildTree(): RootNode
}