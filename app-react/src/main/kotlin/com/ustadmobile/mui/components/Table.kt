package com.ustadmobile.mui.components

import com.ustadmobile.mui.ext.createReUsableComponent
import mui.material.*
import react.RBuilder
import styled.StyledHandler

fun RBuilder.umTable(
    stickyHeader: Boolean = false,
    size: Size = Size.medium,
    padding: TablePadding = TablePadding.normal,
    className: String? = null,
    handler: StyledHandler<TableProps>? = null
) = createReUsableComponent(Table, className, handler) {
    attrs.padding = padding
    attrs.size = size as BaseSize
    attrs.stickyHeader = stickyHeader
}

fun RBuilder.umTableBody(
    className: String? = null,
    handler: StyledHandler<TableBodyProps>? = null
) = createReUsableComponent(TableBody, className, handler) {}


fun RBuilder.umTableHead(
    className: String? = null,
    handler: StyledHandler<TableHeadProps>? = null
) = createReUsableComponent(TableHead, className, handler) {}

fun RBuilder.umTableRow(
    className: String? = null,
    hover: Boolean = false,
    selected: Boolean = false,
    handler: StyledHandler<TableRowProps>? = null
) = createReUsableComponent(TableRow, className, handler) {
    attrs.hover = hover
    attrs.selected = selected
}

fun RBuilder.umTableCell(
    align: TableCellAlign = TableCellAlign.left,
    padding: TableCellPadding = TableCellPadding.normal,
    scope: String? = "row",
    colSpan: Int = 1,
    size: Size = Size.medium,
    className: String? = null,
    handler: StyledHandler<TableCellProps>? = null
) = createReUsableComponent(TableCell, className, handler) {
    attrs.padding = padding
    attrs.size = size as? BaseSize
    attrs.align = align
    attrs.scope = scope
    attrs.colSpan = colSpan
}

fun RBuilder.umTableContainer(
    className: String? = null,
    handler: StyledHandler<TableContainerProps>? = null
) = createReUsableComponent(TableContainer, className, handler){}