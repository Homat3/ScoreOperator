package com.water.scoreoperator.views

import android.annotation.SuppressLint
import android.app.Activity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import com.water.scoreoperator.R
import com.water.scoreoperator.services.TcpLink
import com.water.scoreoperator.utils.ConnectDevice
import com.water.scoreoperator.utils.DeviceKind
import com.water.scoreoperator.utils.MessageBox
import com.water.scoreoperator.utils.PermissionsManager
import com.water.scoreoperator.utils.mainJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
class SettingView() : View() {
    //View标识
    override fun getRoute(): String = ViewPage.SettingView.name
    companion object BaseTool : MessageBox{
        @SuppressLint("StaticFieldLeak")
        override var parent: Activity? = null
        var tcpLink: TcpLink? = null

        fun bindTools(parent: Activity, tcpLink: TcpLink){
            this.parent = parent
            this.tcpLink = tcpLink
        }
    }

    //Devices
    //连接设备
    val connectDevice = object : ConnectDevice{
        //设备标识
        override val deviceKind = DeviceKind.ConnectDevice

        //设备变量
        override var IP = mutableStateOf("192.168.1.1")
        override var PORT = mutableStateOf("2001")

        //设备运行时
        override val job = Job(mainJob)
        override val scope = CoroutineScope(job)

        //设备UI
        @Composable
        override fun DeviceBlock(modifier: Modifier) {
            Card(modifier) {
                Column {
                    ConnectionSettingBlock(modifier)
                }
            }
        }

        //UI所需响应函数
        fun onGetConnectionToDeviceButton() = scope.launch{ tcpLink?.connectTask(IP.value, PORT.value) }
        fun onDelConnectionToDeviceButton() = scope.launch{ tcpLink?.disconnectTask() }
        fun onGetConnectionInfoButton() = tcpLink?.let {
            showMessageBox("Connection Information", "Connection to address(IP:${IP.value},PORT:${PORT.value}):${it.connectCondition}")
        }

        //UI组件
        @Composable
        fun ConnectionSettingBlock(modifier: Modifier){

            Column(modifier) {
                //GetConnectionToDeviceButton
                Button(
                    onClick = { onGetConnectionToDeviceButton() }
                ) {
                    Text( stringResource(R.string.GetConnectionToDeviceButton) )
                }
                //DelConnectionToDeviceButton
                Button(
                    onClick = { onDelConnectionToDeviceButton() }
                ) {
                    Text( stringResource(R.string.DelConnectionToDeviceButton) )
                }

                //connectInfo
                TextField(
                    value = IP.value,
                    onValueChange = { IP.value = it },
                    label = { Text(stringResource(R.string.tcp_id)) },
                    singleLine = true,
                )
                TextField(
                    value = PORT.value,
                    onValueChange = { PORT.value = it },
                    label = { Text(stringResource(R.string.tcp_port)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                //GetConnectionInfoButton
                Button(
                    onClick = { onGetConnectionInfoButton() }
                ) {
                    Text( stringResource(R.string.GetConnectionInfoButton) )
                }

                Text( "Output:\n${tcpLink?.connectionTaskOutput?.value}")
            }
        }

    }

    //View UI
    @Composable
    override fun OnCreate(modifier: Modifier) {
        parent?.let { PermissionsManager.updatePermitted(it) }
        Column(modifier) {
            RequestPermissionsBlock(modifier)
            connectDevice.DeviceBlock(modifier)
        }
    }

    //UI所需响应函数
    fun onRequestPermissions(){
        parent?.requestPermissions(PermissionsManager.toNameArray(), 0)
        parent?.let { PermissionsManager.updatePermitted(it) }
    }

    //UI组件
    @Composable
    fun RequestPermissionsBlock(modifier: Modifier){
        Card(modifier.fillMaxWidth()) {
            Column(modifier) {
                Text( "Permission:" + PermissionsManager.allIsPermitted.value.toString() )

                //GetPermissionButton
                Button(
                    enabled = !PermissionsManager.allIsPermitted.value,
                    onClick = { onRequestPermissions() }
                ) {
                    Text( stringResource(R.string.GetPermissionButton) )
                }
            }
        }
    }

}