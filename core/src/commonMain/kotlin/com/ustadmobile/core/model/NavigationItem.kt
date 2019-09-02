package com.ustadmobile.core.model

class NavigationItem (){
    lateinit var viewName: String
    var arguments:HashMap<String, String>? = null
    lateinit var title: String

    constructor(vn:String, arg:HashMap<String, String>, tt:String) : this() {
        viewName = vn
        arguments=arg
        title = tt
    }
}