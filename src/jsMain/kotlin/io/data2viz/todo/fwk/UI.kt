package io.data2viz.todo.fwk

import kotlinx.html.*
import kotlinx.html.dom.JSDOMBuilder
import org.w3c.dom.*
import kotlin.browser.document
import kotlin.math.*

/**
 * Returns a single element and type it.
 */
fun <T> String.select(): T = document.querySelector(this) as T

/**
 * See https://developer.mozilla.org/fr/docs/Web/API/Element/classList
 */
fun HTMLElement.classToggle(clazz: String) {
	val classList = (getAttribute("class") ?: "")
		.split(" ".toRegex())
		.map { it.trim() }
		.filterNot { it.isEmpty() }

	val toggledList = if ( classList.contains(clazz) )
		classList - clazz
	else
		classList + clazz
	setAttribute("class", toggledList.joinToString(" "))
}


fun <D> addEvents(selector: String, data: List<D>, apply: HTMLElement.(D) -> Unit) {
	val elements = document.querySelectorAll(selector).asList()
	if (elements.size != data.size)
		error("There should be as much selected element as data (${elements.size} vs ${data.size}")

	val zip = elements.zip(data)

	zip.forEach {
		val htmlElement = it.first as HTMLElement
		apply(htmlElement, it.second)
	}
}

val domBuilder : TagConsumer<HTMLElement>
	get() = JSDOMBuilder(document)


val HTMLElement.uniqueChild: Node
	get() = childNodes[0]!!

fun Element.removeChildren() {
	while (firstElementChild != null) {
		removeChild(firstElementChild!!)
	}
}

/**
 * Synchronize current element from new element, removing/adding needed nodes.
 *
 * @param updateRootAttrs indicates if root attributes should also be synchronized.
 * In most case it is better to not update the root attributes in order not to loose
 * the classes of the root element.
 */
fun Element.updateWith(newElement: Element?, updateRootAttrs: Boolean = false) {
	val parent = this.parentNode ?: error("It's only possible to update a tree which has a parent.")
	updateElement(parent, newElement, this, updateRootAttrs)
}

private fun updateElement(parent:Node, newElement:Node?, oldElement: Node?, updateAttrs: Boolean) {
	if (oldElement != null && newElement != null) {
		if (newElement.nodeDifferentFrom(oldElement)){
			parent.replaceChild(newElement, oldElement)
		} else {
			updateAttributesAndText(oldElement, newElement, updateAttrs)
			oldElement.nodeValue = newElement.nodeValue
			val zip = cleanZipWithNullable(
				newElement.childNodes,
				oldElement.childNodes)
			zip.forEach {
				updateElement(oldElement, it.first, it.second, true)
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

private fun Node.nodeDifferentFrom(node2: Node): Boolean =
	if( this is Element && node2 is Element) tagName != node2.tagName
	else if (node2 is Element && this !is Element) true
	else if (this is Element && node2 !is Element) true
	else if (this is Text && node2 is Text) (this.data != node2.data)
	else false

private fun updateAttributesAndText(target: Node, newElement: Node, updateAttrs: Boolean) {
	if( target is Element && newElement is Element && updateAttrs){
		val attrNames =
			target.attributes.asList().map { it.name } +
				newElement.attributes.asList().map { it.name }

		attrNames.toSet().forEach {
			updateOrRemoveAttribute(target, it, newElement.getAttribute(it), target.getAttribute(it))
		}
	}

	if (target is Text && newElement is Text) {
		target.data = newElement.data
	}
	target.nodeValue
}

private fun updateOrRemoveAttribute(target:Element, name:String, newAttr:String?, oldAttr:String?) {
	if (newAttr == null)
		target.removeAttribute(name)
	else if (oldAttr == null || newAttr != oldAttr)
		target.setAttribute(name, newAttr)
}

private fun cleanZipWithNullable(list1: NodeList, list2: NodeList): List<Pair<Node?, Node?>>{

	val cleanedList1 = list1.asList().filterCommentAndBlankTextNodes()
	val cleanedList2 = list2.asList().filterCommentAndBlankTextNodes()

	val maxListSize = max (cleanedList1.size, cleanedList2.size)

	val complete1: List<Node?> = cleanedList1 + arrayOfNulls<Node?>(maxListSize - cleanedList1.size)
	val complete2: List<Node?> = cleanedList2 + arrayOfNulls<Node?>(maxListSize - cleanedList2.size)
	return complete1.zip(complete2)
}

/**
 * Remove Blank text and comment nodes.
 * https://www.sitepoint.com/removing-useless-nodes-from-the-dom/
 */
private fun List<Node>.filterCommentAndBlankTextNodes(): List<Node> =
	filterNot {
		it as? Comment != null ||
			(it as? Text != null && it.nodeValue.isNullOrBlank())
	}
