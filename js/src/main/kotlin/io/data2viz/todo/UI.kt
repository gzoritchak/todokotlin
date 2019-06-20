package io.data2viz.todo

import kotlinx.html.TagConsumer
import kotlinx.html.dom.JSDOMBuilder
import org.w3c.dom.*
import kotlin.browser.document

fun <D> addEvents(selector: String, data: List<D>, apply: HTMLElement.(D) -> Unit) {
    val elements = document.querySelectorAll(selector).asList()
    if (elements.size != data.size)
        error("There should be as much selected element as data")

    val zip = elements.zip(data)

    zip.forEach {
        val htmlElement = it.first as HTMLElement
        apply(htmlElement, it.second)
    }
}

val render : TagConsumer<HTMLElement>
    get() = JSDOMBuilder(document)


val HTMLElement.uniqueChild: Node
    get() {
        return childNodes[0]!!
    }

fun Element.removeChildren() {
    while (firstElementChild != null) {
        removeChild(firstElementChild!!)
    }
}
