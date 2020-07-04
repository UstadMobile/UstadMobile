# Get the names of every string
cd ..

cat core/locale/main/values/strings_ui.xml | while read line; do
        a=`echo $line | awk -F\" '{ print $2 }'`
        if [[ $a = *[!\ ]* ]]; then
              echo $a >> ./tools/strings_names.txt
        else
              echo "empty"
        fi
done
#Build unused names list
cat ./tools/strings_names.txt | while read line; do
        echo ""
        echo $line
        l=`echo $line`
        #find . -not -name "merger.xml" -name "*.xml" | xargs grep "$line" >> ~/grepres.txt
        z="@string/$l"
        echo "${z}|"
        if grep -q -r --include=\*.xml "$z"; then
                echo ""
        else
                a="MessageID.${l}"
                echo "${a}|"
                if grep -q -r --include=\*.kt --exclude=MessageIDMap.kt "$a"; then
                        echo ""
                else
                        r="R.string.$l"
                        echo "${r}|"
                        if grep -q -r --include=\*.kt --exclude=MessageIDMap.kt "$r"; then
                                echo ""
                        else
                                echo $l >> ./tools/unused_string_ids.txt
                        fi
                fi
        fi
done

cd tools
echo "Unused string ids are in unused_string_ids.txt"

#Remove strings from main xml
#cp strings_classbook.xml removed.xml
#cat grepres.txt | while read line; do
#        l=`echo $line`
#        echo "${l}"
#        sed "/\<string name\=\"${l}\"/d" removed.xml > temp.xml
#        cp temp.xml removed.xml
#        rm -f temp.xml
#done
#rm -f grepres.xml
#rm -f string_names.xml
