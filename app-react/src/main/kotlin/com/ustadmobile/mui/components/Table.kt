package com.ustadmobile.mui.components

import com.ustadmobile.mui.ext.createStyledComponent
import kotlinx.css.Align
import kotlinx.css.hyphenize
import mui.material.*
import react.RBuilder
import styled.StyledHandler
@Suppress("EnumEntryName")
enum class TableSize {
    small, medium, large;

    override fun toString(): String {
        return super.toString().hyphenize()
    }
}

enum class TablePadding{
    normal, checkbox, none;

    override fun toString(): String {
        return super.toString().hyphenize()
    }
}

fun RBuilder.umTable(
    stickyHeader: Boolean = false,
    size: TableSize = TableSize.medium,
    padding: TablePadding = TablePadding.normal,
    className: String? = null,
    handler: StyledHandler<TableProps>? = null
) = createStyledComponent(Table, className, handler) {
    attrs.padding = padding.toString()
    attrs.size = size.toString()
    attrs.stickyHeader = stickyHeader
}

fun RBuilder.umTableBody(
    className: String? = null,
    handler: StyledHandler<TableBodyProps>? = null
) = createStyledComponent(TableBody, className, handler) {}


fun RBuilder.umTableHead(
    className: String? = null,
    handler: StyledHandler<TableHeadProps>? = null
) = createStyledComponent(TableHead, className, handler) {}

fun RBuilder.umTableRow(
    className: String? = null,
    hover: Boolean = false,
    selected: Boolean = false,
    handler: StyledHandler<TableRowProps>? = null
) = createStyledComponent(TableRow, className, handler) {
    attrs.hover = hover
    attrs.selected = selected
}

fun RBuilder.umTableCell(
    align: Align = Align.inherit,
    padding: TablePadding = TablePadding.normal,
    scope: String? = "row",
    colSpan: Int = 1,
    size: TableSize = TableSize.medium,
    className: String? = null,
    handler: StyledHandler<TableCellProps>? = null
) = createStyledComponent(TableCell, className, handler) {
    attrs.padding = padding.toString()
    attrs.size = size.toString()
    attrs.align = align.toString()
    attrs.scope = scope
    attrs.asDynamic().colSpan = colSpan.toString()
}

fun RBuilder.umTableContainer(
    className: String? = null,
    handler: StyledHandler<TableContainerProps>? = null
) = createStyledComponent(TableContainer, className, handler){}