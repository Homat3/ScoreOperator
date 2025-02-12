package com.water.scoreoperator.utils

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.water.scoreoperator.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

enum class DeviceKind(val tag: Char) {
    MainDevice('M'), ConnectDevice('C'),
    Data('V'), Beep('B'), Fan('F'), Rgb('R'), Led('D'), Lcd('L');
}

@Suppress("ControlFlowWithEmptyBody")
class TeamData(
    val base: Base, val tag : String, var name : MutableState<String>,
    var addStep : MutableIntState, var removeStep : MutableIntState,
    var frequency : MutableIntState,
    var rangeFrom : MutableIntState, var rangeTo : MutableIntState,
    maxMemberCount: Int
){
    data class Member(val index: Int, var addStep : MutableIntState, var removeStep : MutableIntState, var isEnable: MutableState<Boolean>)
    var face = mutableStateOf(false)
    val memberArray = Array(maxMemberCount) { Member(it, mutableIntStateOf(1), mutableIntStateOf(1), mutableStateOf(false)) }

    fun onAddButton() = base.scope.launch{
        base.encodedMessage = "-${base.deviceKind.tag}-A-$tag-${addStep.intValue}-"
        while (base.mainDevice.isUsing) {}
        base.mainDevice.isUsing = true
        base.postToMainDevice()
        base.mainDevice.isUsing = false
    }
    fun onRemoveButton() = base.scope.launch{
        base.encodedMessage = "-${base.deviceKind.tag}-R-$tag-${addStep.intValue}-"
        while (base.mainDevice.isUsing) {}
        base.mainDevice.isUsing = true
        base.postToMainDevice()
        base.mainDevice.isUsing = false
    }
    fun onAddMemberButton(member : Member) = base.scope.launch{
        base.encodedMessage = "-${base.deviceKind.tag}-a-$tag-${member.index}-${member.addStep.intValue}-"
        while (base.mainDevice.isUsing) {}
        base.mainDevice.isUsing = true
        base.postToMainDevice()
        base.mainDevice.isUsing = false
    }
    fun onRemoveMemberButton(member : Member) = base.scope.launch{
        base.encodedMessage = "-${base.deviceKind.tag}-r-$tag-${member.index}-${member.removeStep.intValue}-"
        while (base.mainDevice.isUsing) {}
        base.mainDevice.isUsing = true
        base.postToMainDevice()
        base.mainDevice.isUsing = false
    }
    fun onPunishButton(index: Int) = base.scope.launch{
        base.encodedMessage = "-${base.deviceKind.tag}-P-$tag-$index-"
        while (base.mainDevice.isUsing) {}
        base.mainDevice.isUsing = true
        base.postToMainDevice()
        base.mainDevice.isUsing = false
    }
    fun onUnpunishButton(index: Int) = base.scope.launch{
        base.encodedMessage = "-${base.deviceKind.tag}-p-$tag-$index-"
        while (base.mainDevice.isUsing) {}
        base.mainDevice.isUsing = true
        base.postToMainDevice()
        base.mainDevice.isUsing = false
    }
    fun onFrequencyButton() = base.scope.launch{
        base.encodedMessage = "-${base.deviceKind.tag}-F-$tag-${frequency.intValue}-"
        while (base.mainDevice.isUsing) {}
        base.mainDevice.isUsing = true
        base.postToMainDevice()
        base.mainDevice.isUsing = false
    }
    fun onSetRangeButton() = base.scope.launch{
        base.encodedMessage = "-${base.deviceKind.tag}-G-$tag-${rangeFrom.intValue}-${rangeTo.intValue}-"
        while (base.mainDevice.isUsing) {}
        base.mainDevice.isUsing = true
        base.postToMainDevice()
        base.mainDevice.isUsing = false
    }
    fun onSetNameButton() = base.scope.launch{
        base.encodedMessage = "-${base.deviceKind.tag}-N-$tag-${name.value}-"
        while (base.mainDevice.isUsing) {}
        base.mainDevice.isUsing = true
        base.postToMainDevice()
        base.mainDevice.isUsing = false
    }

    @Composable
    fun TeamBox(modifier: Modifier){
        OutlinedCard(
            modifier = modifier,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface,),
            border = BorderStroke(1.dp, Color.Black)
        ) {
            Column {
                Row {
                    Text(modifier = Modifier.padding(10.dp), text = "Team: ${name.value}")
                    Button(modifier = Modifier.padding(10.dp), onClick = {face.value = !face.value}){
                        Text(stringResource(R.string.SwitchFace))
                    }
                }
                if (face.value){ MemberFace(Modifier.fillMaxSize().padding(10.dp)) }
                else{ TeamFace(Modifier.fillMaxSize().padding(10.dp)) }
            }
        }
    }

    @Composable
    fun MemberFace(modifier: Modifier) = Card(modifier) { LazyColumn { memberArray.forEach { item{
        Column {
            Text(modifier = modifier, text = "Member ${it.index.toString()}:")
            Row(Modifier.padding(20.dp)) {
                Button(
                    enabled = it.isEnable.value,
                    onClick = { onAddMemberButton(it) }
                ) { Text( stringResource(R.string.AddButton) ) }
                TextField(
                    enabled = it.isEnable.value,
                    value = it.addStep.intValue.toString(),
                    onValueChange = { value -> it.addStep.intValue = value.toInt() },
                    label = { Text(stringResource(R.string.Step)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
            Row(Modifier.padding(20.dp)) {
                Button(
                    enabled = it.isEnable.value,
                    onClick = { onRemoveMemberButton(it) }
                ) { Text( stringResource(R.string.RemoveButton) ) }
                TextField(
                    enabled = it.isEnable.value,
                    value = it.removeStep.intValue.toString(),
                    onValueChange = { value -> it.removeStep.intValue = value.toInt() },
                    label = { Text(stringResource(R.string.Step)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
            Button(enabled = it.isEnable.value, onClick = { onPunishButton(it.index) }) {
                Text( stringResource(R.string.PunishButton) )
            }
            Button(enabled = it.isEnable.value, onClick = { onUnpunishButton(it.index) }) {
                Text( stringResource(R.string.UnpunishButton) )
            }
            Button(onClick = { it.isEnable.value = !it.isEnable.value }) {
                Text( "Change to ${!it.isEnable.value}" )
            }
        }
    } } } }

    @Composable
    fun TeamFace(modifier: Modifier){
        Card(modifier) {
            LazyColumn {
                item{
                    Row(Modifier.padding(20.dp)) {
                        Button(onClick = { onAddButton() }) {
                            Text( stringResource(R.string.AddButton) )
                        }
                        TextField(
                            value = addStep.intValue.toString(),
                            onValueChange = { addStep.intValue = it.toInt() },
                            label = { Text(stringResource(R.string.Step)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }
                item{
                    Row(Modifier.padding(20.dp)) {
                        Button(onClick = { onRemoveButton() }) {
                            Text( stringResource(R.string.RemoveButton) )
                        }
                        TextField(
                            value = removeStep.intValue.toString(),
                            onValueChange = { removeStep.intValue = it.toIntOrNull().let { if (it == null) 0 else it }  },
                            label = { Text(stringResource(R.string.Step)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }
                item{
                    Row(Modifier.padding(20.dp)) {
                        Button(onClick = { onFrequencyButton() }) {
                            Text( stringResource(R.string.FrequencyButton) )
                        }
                        TextField(
                            value = frequency.intValue.toString(),
                            onValueChange = { frequency.intValue = it.toIntOrNull().let { if (it == null) 0 else it } },
                            label = { Text(stringResource(R.string.Frequency)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }
                item{
                    Column(Modifier.padding(20.dp)) {
                        Button(onClick = { onSetRangeButton() }) {
                            Text( stringResource(R.string.Range) )
                        }
                        TextField(
                            value = rangeFrom.intValue.toString(),
                            onValueChange = { rangeFrom.intValue = it.toIntOrNull().let { if (it == null) 0 else it } },
                            label = { Text(stringResource(R.string.RangeFrom)) },
                            singleLine = true,
                        )
                        TextField(
                            value = rangeTo.intValue.toString(),
                            onValueChange = { rangeTo.intValue = it.toIntOrNull().let { if (it == null) 0 else it } },
                            label = { Text(stringResource(R.string.RangeTo)) },
                            singleLine = true,
                        )
                    }
                }
                item{
                    Row(Modifier.padding(20.dp)) {
                        Button(onClick = { onSetNameButton() }) {
                            Text( stringResource(R.string.Name) )
                        }
                        TextField(
                            value = name.value,
                            onValueChange = { name.value = it.let { if(it.length > 4) it.substring(0, 4) else it } },
                            label = { Text(stringResource(R.string.Name)) },
                            singleLine = true,
                        )
                    }
                }
            }
        }
    }
}

val mainJob = Job()

interface Device {
    //当前设备类型
    val deviceKind : DeviceKind
    val job : Job                  //运行控制
    val scope : CoroutineScope     //运行时

    @Composable
    //设备界面
    fun DeviceBlock(modifier: Modifier)
}

//处理人工发送+设备初始化
interface MainDevice : Device {
    var messageToBeSent : MutableState<String>
    var isUsing : Boolean
    fun postMessage(message: String)
}
//处理连接
interface ConnectDevice : Device {
    var IP : MutableState<String>
    var PORT : MutableState<String>
}

//各个设备
interface Base : Device{
    var mainDevice: MainDevice
    var encodedMessage: String?

    fun postToMainDevice(){
        encodedMessage?.let { mainDevice.postMessage(it) }
    }
}
//数据储存
interface Data : Base{
    //Team A
    val teamA : TeamData

    //Team B
    val teamB : TeamData
}
//蜂鸣器
interface Beep : Base{
    var beepFrequency : MutableIntState
}
//风扇
interface Fan : Base{
    var rollSpeed : MutableIntState
}
//RGB灯
interface Rgb : Base{
    var currentColor : MutableState<String>
}
//双色LED
interface DoubleLed : Base{
    var currentColor : MutableState<String>
}
//LCD显示屏
interface LCDScreen : Base{
    var showText : MutableState<String>
}

