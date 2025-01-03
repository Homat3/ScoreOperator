package com.water.scoreoperator.views

import android.annotation.SuppressLint
import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.water.scoreoperator.R
import com.water.scoreoperator.services.TcpLink
import com.water.scoreoperator.utils.Beep
import com.water.scoreoperator.utils.Data
import com.water.scoreoperator.utils.DeviceKind
import com.water.scoreoperator.utils.DoubleLed
import com.water.scoreoperator.utils.Fan
import com.water.scoreoperator.utils.LCDScreen
import com.water.scoreoperator.utils.MainDevice
import com.water.scoreoperator.utils.MessageBox
import com.water.scoreoperator.utils.Rgb
import com.water.scoreoperator.utils.TeamData
import com.water.scoreoperator.utils.mainJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Suppress("ControlFlowWithEmptyBody")
@Serializable
open class ControlView : View() {
    //View标识
    override fun getRoute(): String = ViewPage.ControlView.name
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
    //单设备
    val deviceView = "deviceView"
    //主设备
    val mainDevice = object : MainDevice{
        //设备标识
        override val deviceKind = DeviceKind.MainDevice

        //设备变量
        override var messageToBeSent = mutableStateOf("")
        override var isUsing : Boolean = false

        //设备运行时
        override val job = Job(mainJob)
        override val scope = CoroutineScope(job)

        //工具函数
        override fun postMessage(message: String) {
            tcpLink?.sendTask("^$message!")
        }

        //设备UI
        @Composable
        override fun DeviceBlock(modifier: Modifier) {
            Card(modifier) {
                Column {
                    Text( text = deviceKind.name, modifier = modifier )
                    SendMessageBlock(modifier)
                }
            }
        }

        //UI所需响应函数
        fun onSendMessageButton() = scope.launch{
            while (isUsing) {}
            isUsing = true
            postMessage(messageToBeSent.value)
            isUsing = false
        }

        //UI组件
        @Composable
        fun SendMessageBlock(modifier: Modifier){
            Column(modifier) {
                TextField(
                    value = messageToBeSent.value,
                    onValueChange = { messageToBeSent.value = it },
                    label = { Text(stringResource(R.string.MessageToSend)) },
                    singleLine = true,
                )
                //GetConnectionToDeviceButton
                Button(
                    onClick = { onSendMessageButton() }
                ) {
                    Text( stringResource(R.string.SendMessageButton) )
                }
                tcpLink?.oTaskOutput?.let { Text( "Output:\n${it.value}" ) }
            }
        }
    }
    //蜂鸣器
    val beep = object : Beep{
        //设备标识
        override val deviceKind = DeviceKind.Beep
        override lateinit var mainDevice: MainDevice

        //设备变量
        override var encodedMessage: String? = null
        override var beepFrequency = mutableIntStateOf(200)

        //设备运行时
        override val job = Job(mainJob)
        override val scope = CoroutineScope(job)

        //设备UI
        @Composable
        override fun DeviceBlock(modifier: Modifier) {
            Card(modifier) {
                Column {
                    Text( text = deviceKind.name, modifier = modifier )
                    BeepSettingBlock(modifier)
                }
            }
        }

        //UI所需响应函数
        fun onBeepButton() = scope.launch{
            encodedMessage = "-${deviceKind.tag}-${beepFrequency.intValue}-"
            while (mainDevice.isUsing) {}
            mainDevice.isUsing = true
            postToMainDevice()
            mainDevice.isUsing = false
        }
        fun onStopBeepButton() = scope.launch{
            encodedMessage = "-${deviceKind.tag}-0-"
            while (mainDevice.isUsing) {}
            mainDevice.isUsing = true
            postToMainDevice()
            mainDevice.isUsing = false
        }

        @Composable
        fun BeepSettingBlock(modifier: Modifier){
            Column(modifier) {
                Row {
                    //BeepButton
                    Button(
                        onClick = { onBeepButton() }
                    ) {
                        Text( stringResource(R.string.BeepButton) )
                    }
                    TextField(
                        value = beepFrequency.intValue.toString(),
                        onValueChange = { beepFrequency.intValue = it.toIntOrNull().let { if (it == null) 0 else it }  },
                        label = { Text(stringResource(R.string.BeepFrequency)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
                //StopBeepButton
                Button(
                    onClick = { onStopBeepButton() }
                ) {
                    Text( stringResource(R.string.StopBeepButton) )
                }
            }
        }

    }
    //风扇
    val fan = object : Fan{
        //设备标识
        override val deviceKind = DeviceKind.Fan
        override lateinit var mainDevice: MainDevice

        //设备变量
        override var encodedMessage: String? = null
        override var rollSpeed = mutableIntStateOf(200)

        //设备运行时
        override val job = Job(mainJob)
        override val scope = CoroutineScope(job)

        //设备UI
        @Composable
        override fun DeviceBlock(modifier: Modifier) {
            Card(modifier) {
                Column {
                    Text( text = deviceKind.name, modifier = modifier )
                    RollingSettingBlock(modifier)
                }
            }
        }

        //UI所需响应函数
        fun onRollButton() = scope.launch{
            encodedMessage = "-${deviceKind.tag}-${rollSpeed.intValue}-"
            while (mainDevice.isUsing) {}
            mainDevice.isUsing = true
            postToMainDevice()
            mainDevice.isUsing = false
        }
        fun onStopRollButton() = scope.launch{
            encodedMessage = "-${deviceKind.tag}-0-"
            while (mainDevice.isUsing) {}
            mainDevice.isUsing = true
            postToMainDevice()
            mainDevice.isUsing = false
        }

        @Composable
        fun RollingSettingBlock(modifier: Modifier){
            Column(modifier) {
                Row {
                    //RollButton
                    Button(
                        onClick = { onRollButton() }
                    ) {
                        Text( stringResource(R.string.RollButton) )
                    }
                    TextField(
                        value = rollSpeed.intValue.toString(),
                        onValueChange = { rollSpeed.intValue = it.toIntOrNull().let { if (it == null) 0 else it }  },
                        label = { Text(stringResource(R.string.RollSpeed)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                //StopRollButton
                Button(
                    onClick = { onStopRollButton() }
                ) {
                    Text( stringResource(R.string.StopRollButton) )
                }
            }
        }
    }
    //RGB灯
    val rgb = object : Rgb{
        //设备标识
        override val deviceKind = DeviceKind.Rgb
        override lateinit var mainDevice: MainDevice

        //设备变量
        override var encodedMessage: String? = null
        val colors = arrayListOf("Red", "Green", "Blue")
        override var currentColor = mutableStateOf(colors[0])

        //设备运行时
        override val job = Job(mainJob)
        override val scope = CoroutineScope(job)

        //设备UI
        @Composable
        override fun DeviceBlock(modifier: Modifier)  {
            Card(modifier) {
                Column {
                    Text( text = deviceKind.name, modifier = modifier )
                    ColorSettingBlock(modifier)
                }
            }
        }

        //UI所需响应函数
        fun onLightButton() = scope.launch{
            encodedMessage = "-${deviceKind.tag}-${currentColor.value}-"
            while (mainDevice.isUsing) {}
            mainDevice.isUsing = true
            postToMainDevice()
            mainDevice.isUsing = false
        }
        fun onStopLightButton() = scope.launch{
            encodedMessage = "-${deviceKind.tag}-Null-"
            while (mainDevice.isUsing) {}
            mainDevice.isUsing = true
            postToMainDevice()
            mainDevice.isUsing = false
        }

        @Composable
        fun ColorSettingBlock(modifier: Modifier){
            Column(modifier) {
                Row {
                    colors.forEach {
                        Column {
                            RadioButton(
                                selected = it == currentColor.value,
                                onClick = {
                                    currentColor.value = it
                                }
                            )
                            Text(text = it)
                        }
                    }
                }
                //LightButton
                Button(
                    onClick = { onLightButton() }
                ) {
                    Text( stringResource(R.string.LightButton) )
                }
                //StopLightButton
                Button(
                    onClick = { onStopLightButton() }
                ) {
                    Text( stringResource(R.string.StopLightButton) )
                }
            }
        }
    }
    //双色LED
    val doubleLed = object : DoubleLed{
        //设备标识
        override val deviceKind = DeviceKind.Led
        override lateinit var mainDevice: MainDevice

        //设备变量
        override var encodedMessage: String? = null
        val colors = arrayListOf("Red", "Green")
        override var currentColor = mutableStateOf(colors[0])

        //设备运行时
        override val job = Job(mainJob)
        override val scope = CoroutineScope(job)

        //设备UI
        @Composable
        override fun DeviceBlock(modifier: Modifier) {
            Card(modifier) {
                Column {
                    Text( text = deviceKind.name, modifier = modifier )
                    ColorSettingBlock(modifier)
                }
            }
        }

        //UI所需响应函数
        fun onLightButton() = scope.launch{
            encodedMessage = "-${deviceKind.tag}-${currentColor.value}-"
            while (mainDevice.isUsing) {}
            mainDevice.isUsing = true
            postToMainDevice()
            mainDevice.isUsing = false
        }
        fun onStopLightButton() = scope.launch{
            encodedMessage = "-${deviceKind.tag}-Null-"
            while (mainDevice.isUsing) {}
            mainDevice.isUsing = true
            postToMainDevice()
            mainDevice.isUsing = false
        }

        @Composable
        fun ColorSettingBlock(modifier: Modifier){
            Column(modifier) {
                Row {
                    colors.forEach {
                        Row {
                            RadioButton(
                                selected = it == currentColor.value,
                                onClick = {
                                    currentColor.value = it
                                }
                            )
                            Text(text = it)
                        }
                    }
                }
                //LightButton
                Button(
                    onClick = { onLightButton() }
                ) {
                    Text( stringResource(R.string.LightButton) )
                }
                //StopLightButton
                Button(
                    onClick = { onStopLightButton() }
                ) {
                    Text( stringResource(R.string.StopLightButton) )
                }
            }
        }
    }
    //LCD显示屏
    val lcdScreen = object : LCDScreen{
        //设备标识
        override val deviceKind = DeviceKind.Lcd
        override lateinit var mainDevice: MainDevice

        //设备变量
        override var encodedMessage: String? = null
        override var showText = mutableStateOf("")

        //设备运行时
        override val job = Job(mainJob)
        override val scope = CoroutineScope(job)

        //设备UI
        @Composable
        override fun DeviceBlock(modifier: Modifier) {
            Card(modifier) {
                Column {
                    Text( text = deviceKind.name, modifier = modifier )
                    ShowTextSettingBlock(modifier)
                }
            }
        }

        //UI所需响应函数
        fun onShowingButton() = scope.launch{
            encodedMessage = "-${deviceKind.tag}-${showText.value}-"
            while (mainDevice.isUsing) {}
            mainDevice.isUsing = true
            postToMainDevice()
            mainDevice.isUsing = false
        }

        @Composable
        fun ShowTextSettingBlock(modifier: Modifier){
            Column(modifier) {
                TextField(
                    value = showText.value,
                    onValueChange = { showText.value = it },
                    label = { Text(stringResource(R.string.TextToBeShownOnTheScreen)) },
                    singleLine = true,
                )
                //LightButton
                Button(
                    onClick = { onShowingButton() }
                ) {
                    Text( stringResource(R.string.ShowingButton) )
                }
            }
        }
    }
    //数据
    val dataView = "dataView"
    //数据设备

    @OptIn(ExperimentalMaterial3Api::class)
    val dataDevice = object : Data{
        //设备标识
        override val deviceKind = DeviceKind.Data
        override lateinit var mainDevice: MainDevice

        //设备变量
        override var encodedMessage: String? = null

        override val teamA = TeamData(
            this, "A", mutableStateOf("A"),
            mutableIntStateOf(1), mutableIntStateOf(1), mutableIntStateOf(400),
            mutableIntStateOf(20), mutableIntStateOf(40),
            8
        )
        override val teamB = TeamData(
            this, "B", mutableStateOf("B"),
            mutableIntStateOf(1), mutableIntStateOf(1), mutableIntStateOf(300),
            mutableIntStateOf(80), mutableIntStateOf(100),
            8
        )
        var contestLong = mutableIntStateOf(10);

        var selectedIndex = mutableIntStateOf(0)
        val options = listOf("A", "B", "Both")

        var face = mutableStateOf(true);

        //设备运行时
        override val job = Job(mainJob)
        override val scope = CoroutineScope(job)

        //设备UI
        @Composable
        override fun DeviceBlock(modifier: Modifier) {
            Card(modifier.fillMaxSize()) {
                Column {
                    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text( text = deviceKind.name )
                        Button(
                            onClick = { face.value = !face.value }
                        ) { Text(stringResource(R.string.SwitchFace)) }
                    }
                    if (face.value) TeamBox(modifier)
                    else TeamControlBox(modifier)
                }
            }
        }

        fun onResetTeamButton() = scope.launch{
            encodedMessage = "-${deviceKind.tag}-1-${options[selectedIndex.intValue]}-"
            while (mainDevice.isUsing) {}
            mainDevice.isUsing = true
            postToMainDevice()
            mainDevice.isUsing = false
        }
        fun onContestBeginButton() = scope.launch{
            encodedMessage = "-${deviceKind.tag}-2-"
            while (mainDevice.isUsing) {}
            mainDevice.isUsing = true
            postToMainDevice()
            mainDevice.isUsing = false
        }
        fun onContestFinishedButton() = scope.launch{
            encodedMessage = "-${deviceKind.tag}-3-"
            while (mainDevice.isUsing) {}
            mainDevice.isUsing = true
            postToMainDevice()
            mainDevice.isUsing = false
        }
        fun onSetContestLongButton() = scope.launch{
            encodedMessage = "-${deviceKind.tag}-4-${contestLong.intValue}-"
            while (mainDevice.isUsing) {}
            mainDevice.isUsing = true
            postToMainDevice()
            mainDevice.isUsing = false
        }
        fun onSwitchLedModeButton() = scope.launch{
            encodedMessage = "-${deviceKind.tag}-5-"
            while (mainDevice.isUsing) {}
            mainDevice.isUsing = true
            postToMainDevice()
            mainDevice.isUsing = false
        }

        @Composable
        fun TeamBox(modifier: Modifier){
            BoxWithConstraints( modifier.fillMaxSize() ) {
                val teamBoxHeight = this.maxHeight / 2
                Column {
                    teamA.TeamBox(Modifier.height(teamBoxHeight).fillMaxWidth())
                    teamB.TeamBox(Modifier.height(teamBoxHeight).fillMaxWidth())
                }
            }
        }

        @Composable
        fun TeamControlBox(modifier: Modifier){
            OutlinedCard(
                modifier = modifier.fillMaxSize(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface,),
                border = BorderStroke(1.dp, Color.Black)
            ){
                Column {
                    Button(
                        onClick = { onResetTeamButton() },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text( stringResource(R.string.ResetButton) ) }
                    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                        options.forEachIndexed { index, label ->
                            SegmentedButton(
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = index,
                                    count = options.size
                                ),
                                onClick = { selectedIndex.intValue = index },
                                selected = index == selectedIndex.intValue,
                                label = { Text(label) }
                            )
                        }
                    }
                    Spacer(Modifier.height(30.dp))
                    Button(
                        onClick = { onContestBeginButton() },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text( stringResource(R.string.ContestBeginButton) ) }
                    Spacer(Modifier.height(30.dp))
                    Button(
                        onClick = { onSetContestLongButton() },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text( stringResource(R.string.SetContestLongButton) ) }
                    TextField(
                        value = contestLong.intValue.toString(),
                        onValueChange = { contestLong.intValue = it.toIntOrNull().let { if (it == null) 0 else it }  },
                        label = { Text(stringResource(R.string.ContestLong)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Spacer(Modifier.height(30.dp))
                    Button(
                        onClick = { onContestFinishedButton() },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text( stringResource(R.string.ContestFinishedButton) ) }
                    Spacer(Modifier.height(30.dp))
                    Button(
                        onClick = { onSwitchLedModeButton() },
                        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                    ) { Text( stringResource(R.string.SwitchLedModeButton) ) }
                }
            }
        }

    }

    //链接至主设备
    init {
        beep.mainDevice = mainDevice
        fan.mainDevice = mainDevice
        rgb.mainDevice = mainDevice
        doubleLed.mainDevice = mainDevice
        lcdScreen.mainDevice = mainDevice
        dataDevice.mainDevice = mainDevice
    }

    fun NavHostController.navigateSingleTopTo(route: String) =
        this@navigateSingleTopTo.navigate(route) { launchSingleTop = true }

    //View UI
    @Composable
    override fun OnCreate(modifier: Modifier) {
        val navController = rememberNavController()
        Scaffold(
            modifier = modifier,
            topBar = { NavBar( navController ) },
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = dataView,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(dataView) {
                    OutlinedCard(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface,),
                        border = BorderStroke(1.dp, Color.Black),
                        modifier = Modifier.fillMaxSize()
                    ){
                        dataDevice.DeviceBlock(modifier)
                    }
                }
                composable(deviceView) {
                    OutlinedCard(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface,),
                        border = BorderStroke(1.dp, Color.Black),
                        modifier = Modifier.fillMaxSize()
                    ){
                        LazyColumn {
                            val itemModifier = modifier.fillMaxWidth()
                            item { mainDevice.DeviceBlock(itemModifier) }
                            item { beep.DeviceBlock(itemModifier) }
                            item { fan.DeviceBlock(itemModifier) }
                            item { rgb.DeviceBlock(itemModifier) }
                            item { doubleLed.DeviceBlock(itemModifier) }
                            item { lcdScreen.DeviceBlock(itemModifier) }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun NavBar(navController: NavHostController){
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            //ToDataButton
            Button(onClick = { navController.navigateSingleTopTo(dataView) }) {
                Text( stringResource(R.string.ToDataButton) )
            }
            //ToDeviceButton
            Button(onClick = { navController.navigateSingleTopTo(deviceView) }) {
                Text( stringResource(R.string.ToDeviceButton) )
            }
        }
    }

    @Preview
    @Composable
    fun Preview() {
        dataDevice.teamA.TeamBox(Modifier.padding(20.dp))
    }

}