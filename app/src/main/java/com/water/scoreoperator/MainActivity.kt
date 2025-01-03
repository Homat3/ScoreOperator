package com.water.scoreoperator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.water.scoreoperator.services.TcpLink
import com.water.scoreoperator.ui.theme.ScoreOperatorTheme
import com.water.scoreoperator.views.ControlView
import com.water.scoreoperator.views.SettingView
import kotlinx.serialization.Serializable


class MainActivity : ComponentActivity() {

    private val tcpLink : TcpLink = TcpLink()

    @Serializable
    val settingView : SettingView = SettingView()

    @Serializable
    val controlView : ControlView = ControlView()


    init {
        SettingView.BaseTool.bindTools(this, tcpLink)
        ControlView.BaseTool.bindTools(this, tcpLink)
    }

    fun NavHostController.navigateSingleTopTo(route: String) =
        this@navigateSingleTopTo.navigate(route) { launchSingleTop = true }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { Content(Modifier.padding(20.dp)) }
    }

    //主内容
    @Composable
    fun Content(modifier: Modifier) {
        ScoreOperatorTheme {
            val navController = rememberNavController()
            Scaffold(
                modifier = modifier,
                topBar = { NavBar( navController ) },
                bottomBar = { Text( tcpLink.connectState.value ) }
            ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = settingView.getRoute(),
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(settingView.getRoute()) { settingView.OnCreate(modifier) }
                        composable(controlView.getRoute()) { controlView.OnCreate(modifier) }
                    }
            }
        }

    }

    @Composable
    fun NavBar(navController: NavHostController){
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            //ToConnectionSettingButton
            Button(onClick = { navController.navigateSingleTopTo(settingView.getRoute()) }) {
                Text( stringResource(R.string.ToConnectionSettingButton) )
            }
            //ToControlConsoleButton
            Button(onClick = { navController.navigateSingleTopTo(controlView.getRoute()) }
            ) {
                Text( stringResource(R.string.ToControlConsoleButton) )
            }
        }
    }

}
