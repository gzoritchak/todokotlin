package io.data2viz.todo.fwk

import kotlinx.html.TagConsumer
import kotlinx.html.dom.JSDOMBuilder
import org.w3c.dom.*
import kotlin.browser.document
import kotlin.math.max

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


fun updateElement(parent:Element, newElement:Element?, oldElement: Element?) {
    if (oldElement != null && newElement != null) {
        if (newElement.tagDifferentFrom(oldElement)){
            parent.replaceChild(newElement, oldElement)
        } else {
            // console.log("updateElement merge", oldElement, newElement);
            updateAttrs(oldElement, newElement)
            oldElement.nodeValue = newElement.nodeValue
            val zip = zipWithNullable(
                newElement.children.asList(),
                oldElement.children.asList())
            zip.forEach {
                updateElement(oldElement, it.first, it.second)
            }
        }
    } else {
        if (oldElement == null && newElement != null) {
            parent.appendChild(newElement)
        } else if (newElement == null && oldElement != null) {
            parent.removeChild(oldElement)
        }
    }

}


fun zipWithNullable(list1: List<Element>, list2: List<Element>): List<Pair<Element?, Element?>>{
    val maxListSize = max (list1.size, list2.size)
    val complete1: List<Element?> = list1 + arrayOfNulls<Element?>(maxListSize - list1.size)
    val complete2: List<Element?> = list2 + arrayOfNulls<Element?>(maxListSize - list2.size)
    return complete1.zip(complete2)
}

/**
 * Update all attributes of an element from a
 */
fun updateAttrs(target: Element, newElement: Element) {
    val attrNames =
        target.attributes.asList().map { it.name } +
                newElement.attributes.asList().map { it.name }

    attrNames.toSet().forEach {
        updateAttr(target, it, newElement.getAttribute(it), target.getAttribute(it))
    }
}

/**
 * Update Element attribute, eventually remove it if it is not present in the new node.
 */
fun updateAttr(target:Element, name:String, newAttr:String?, oldAttr:String?) {
    if (newAttr == null) {
        target.removeAttribute(name)
    } else if (oldAttr == null || newAttr != oldAttr) {
        target.setAttribute(name, newAttr)
    }
}

fun Element.tagDifferentFrom(node2: Element): Boolean = tagName != node2.tagName