package com.ustadmobile.view

import kotlinx.browser.window
import org.kodein.di.DI
import org.kodein.di.DIAware
import react.RBuilder
import react.RProps
import react.RState
import styled.styledDiv

interface EntryListProps: RProps

interface EntryListState: RState

class ContentEntryListComponent(props: EntryListProps): UmBaseComponent<RProps,EntryListState>(props) {

    override fun RBuilder.render() {
        styledDiv {
            +"Entry List"
        }
        console.log(di)
    }
}