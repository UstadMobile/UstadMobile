package com.ustadmobile.mui.components

import com.ustadmobile.mui.common.xs
import react.ReactNode
import react.FC
import mui.material.Grid
import mui.material.LinearProgress
import mui.material.LinearProgressVariant
import mui.system.PropsWithSx
import mui.system.sx
import web.cssom.TextAlign


external interface UstadProgressBarWithLabelProps: PropsWithSx {

    var label: ReactNode

    /**
     * Between 0 and 100 as per LinearProgressIndicator
     */
    var progressValue: Int

}

val UstadProgressBarWithLabel = FC<UstadProgressBarWithLabelProps> { props ->
    Grid {
        sx = props.sx

        container = true

        Grid {
            item = true
            xs = 6

            + props.label
        }

        Grid {
            item = true
            xs = 6
            sx {
                textAlign = TextAlign.end
            }

            + "${props.progressValue}%"
        }

        Grid {
            item = true
            xs = 12

            LinearProgress {
                variant = LinearProgressVariant.determinate
                value = props.progressValue
            }
        }
    }


}
