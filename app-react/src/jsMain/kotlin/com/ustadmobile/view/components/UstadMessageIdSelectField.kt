package com.ustadmobile.view.components

import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.impl.locale.entityconstants.EnrolmentPolicyConstants
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.lib.db.entities.Clazz
import react.FC
import react.Props
import react.ReactNode
import react.useState


external interface UstadMessageIdSelectFieldProps: Props {
    /**
     * The currently selected value. If there is no such value in the list, the selection will be blank
     */
    var value: Int

    /**
     * A list of options to show.
     * @see MessageIdOption2
     */
    var options: List<MessageIdOption2>

    /**
     * Field label
     */
    var label: String

    /**
     * Event handler
     */
    var onChange: (MessageIdOption2) -> Unit

    /**
     * DOM element id
     */
    var id: String

    /**
     *
     */
    var enabled: Boolean?

    /**
     *
     */
    var error: String?

    var fullWidth: Boolean

    var helperText: ReactNode?
}

val UstadMessageIdSelectField = FC<UstadMessageIdSelectFieldProps> { props ->
    val strings = useStringProvider()

    UstadSelectField<MessageIdOption2> {
        value = props.options.firstOrNull { it.value == props.value }
            ?: MessageIdOption2.UNSET
        label = props.label
        options = props.options
        fullWidth = props.fullWidth
        itemLabel = { ReactNode(if(it.value != MessageIdOption2.UNSET_VALUE) strings[it.stringResource] else "") }
        itemValue = { it.value.toString() }
        onChange = {
            props.onChange(it)
        }
        id = props.id
        enabled = props.enabled
        error = props.error != null
        helperText = props.error?.let { ReactNode(it) } ?: props.helperText
    }
}

val UstadMessageIdSelectFieldPreview = FC<Props> {

    var policy by useState { Clazz.CLAZZ_ENROLMENT_POLICY_OPEN }

    UstadMessageIdSelectField {
        value = policy
        options = EnrolmentPolicyConstants.ENROLMENT_POLICY_MESSAGE_IDS
        label = "Policy"
        id = "policy"
        onChange = { option ->
            policy = option.value
        }
    }
}