#!/bin/bash

# Get the names of every string
cat core/locale/main/values/strings_ui.xml | while read line; do
        a=`echo $line | awk -F\" '{ print $2 }'`
        if [[ $a = *[!\ ]* ]]; then
              echo $a >> string_ids.txt
        else
              echo "empty"
        fi
done

#Build unused names list
cat string_ids.txt | while read line; do
        echo ""
        echo $line
        l=`echo $line`
        z="@string/$l"
        if grep -q -r --include=\*.xml "$z"; then
                echo ""
        else
                a="MessageID.${l}"
                if grep -q -r --include=\*.kt --exclude=MessageIDMap.kt "$a"; then
                        echo ""
                else
                        r="R.string.$l"
                        echo "${r}|"
                        if grep -q -r --include=\*.kt --exclude=MessageIDMap.kt "$r"; then
                                echo ""
                        else
                                echo $l >> unused_strings.txt
                        fi
                fi
        fi
done

rm string_ids.txt
echo "Saved unused string ids to unused_strings.txt"

