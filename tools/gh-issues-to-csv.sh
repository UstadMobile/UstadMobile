#!/bin/bash

# Requires Github CLI 

gh issue list --state all --limit 1000 --json "number,title,state,milestone"  --jq 'map([.number, .state, .title, .milestone.title] | @csv) | join("\n")'

