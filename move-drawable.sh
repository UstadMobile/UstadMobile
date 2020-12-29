#!/bin/bash

# When you add a new vector asset using Android Studio it will be placed (incorrectly) in the
# core/locale/main directory. This script moves it to the correct location in
# app-android/src/main/res/drawable

mv -v core/locale/main/drawable/*.xml app-android/src/main/res/drawable
rmdir core/locale/main/drawable
