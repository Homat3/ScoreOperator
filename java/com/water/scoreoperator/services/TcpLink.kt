package com.water.scoreoperator.services

import androidx.compose.runtime.mutableStateOf
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket


class TcpLink{

    //数据流
    private var socket = Socket()
    private var outStream : OutputStream? = null

    //显示信息
    // /当前连接状态
    var connectState = mutableStateOf("No connection")
    val connectCondition : String
        get() = if (socket.isConnected) "SUCCEED" else "FAILED"
    //连接操作结果
    var connectionTaskOutput = mutableStateOf(null.toString())
    //io操作结果
    var oTaskOutput = mutableStateOf(null.toString())

    //connect
    fun connectTask(IP : String, PORT : String){
        if (!socket.isConnected){
            try {
                //连接任务
                socket.connect(InetSocketAddress(IP, PORT.toInt()), 2000)
                outStream = socket.outputStream
                sendTask("^-V-0-!")

                //显示
                connectState.value = "Connected"
                connectionTaskOutput.value = "Connected to address(IP:${IP}, PORT:${PORT})"
            } catch (e : Exception) {
                //处理
                outStream = null
                socket = Socket()

                //显示
                connectState.value = "No connection"
                connectionTaskOutput.value = e.toString()
            }
        }
        else
        {
            connectionTaskOutput.value = "Already connected to address(IP:${IP}, PORT:${PORT})"
        }
    }

    //send
    fun sendTask(messageToBeSent : String) {
        try {
            if (!socket.isConnected){
                oTaskOutput.value = "No connection yet"
            } else if (outStream == null){
                oTaskOutput.value = "No out stream yet"
            } else{
                outStream!!.write(messageToBeSent.toByteArray())

                oTaskOutput.value = "Send $messageToBeSent"
            }
        }
        catch (e : Exception)
        {
            oTaskOutput.value = e.toString()
        }
    }

    //disconnect
    fun disconnectTask() {
        try {
            if (socket.isConnected) {
                //断连任务
                socket.shutdownInput()
                socket.shutdownOutput()
                outStream = null
                socket.close()
                socket= Socket()

                //显示
                connectionTaskOutput.value = "Disconnected from the address"
                connectState.value = "No connection"
            } else {
                socket= Socket()

                connectionTaskOutput.value = "Already disconnected"
            }
        }
        catch (e : Exception)
        {
            connectionTaskOutput.value = e.toString()
        }
    }

}