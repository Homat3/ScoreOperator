package com.water.scoreoperator.utils

import android.app.Activity
import android.app.AlertDialog

interface MessageBox {
    val parent : Activity?

    fun showMessageBox(title : String, message : String){
        parent?.let {
            val builder = AlertDialog.Builder(it)

            builder.setTitle(title)
            builder.setMessage(message)
            builder.setPositiveButton("OK") { dialog, which -> {} }

            builder.create().show()
        }
    }
}