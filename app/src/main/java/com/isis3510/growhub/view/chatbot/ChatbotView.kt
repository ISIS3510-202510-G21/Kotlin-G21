package com.isis3510.growhub.view.chatbot

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.FabPosition
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.analytics.FirebaseAnalytics
import com.isis3510.growhub.R
import com.isis3510.growhub.utils.ConnectionStatus
import com.isis3510.growhub.viewmodel.ChatbotViewModel
import com.isis3510.growhub.viewmodel.ConnectivityViewModel

/**
 * Created by: Juan Manuel Jáuregui
 */

@Composable
fun ChatbotView(firebaseAnalytics: FirebaseAnalytics, navController: NavController) {
    val chatbotViewModel: ChatbotViewModel = viewModel()
    val connectivityViewModel: ConnectivityViewModel = viewModel()
    var userInput by remember { mutableStateOf("") }
    val isNetworkAvailable by connectivityViewModel.networkStatus.collectAsState()
    val isBotActive = chatbotViewModel.isBotActive.value

    LaunchedEffect(Unit) {
        chatbotViewModel.checkBotStatus()
        if (isNetworkAvailable == ConnectionStatus.Available) {
            chatbotViewModel.sendInitialBotMessage()
        }
    }

    Box {
        Scaffold(
            containerColor = Color.White,
            topBar = {
                TopBar(
                    modifier = Modifier,
                    isNetworkAvailable = isNetworkAvailable,
                    isBotActive = isBotActive,
                    onNavigateBack = { navController.popBackStack() })
            },
            floatingActionButton = {
                ChatBar(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp).offset(y = 60.dp),
                    value = userInput,
                    onValueChange = { userInput = it },
                    onClickSend = {
                        if (userInput.isNotEmpty()) {
                            chatbotViewModel.sendMessage(userInput, firebaseAnalytics)
                            userInput = ""
                        }
                    },
                    isNetworkAvailable = isNetworkAvailable,
                    isBotActive = isBotActive
                )
            },
            floatingActionButtonPosition = FabPosition.Center
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues = paddingValues)
                    .fillMaxSize()
                    .imePadding()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(space = 8.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    if (isNetworkAvailable == ConnectionStatus.Available) {
                        items(chatbotViewModel.messages) { message ->
                            if (message.role == "user") {
                                UserChat(
                                    modifier = Modifier.align(Alignment.End),
                                    message = message.content
                                )
                            } else {
                                AssistantChat(message = message.content)
                            }
                        }
                    }
                    else {
                        item {
                            ChatBotSectionEmpty()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TopBar(
    modifier: Modifier = Modifier,
    isNetworkAvailable: ConnectionStatus,
    isBotActive: Boolean,
    onNavigateBack: () -> Unit = {}
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color.White)
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            IconButton(onClick = { onNavigateBack() })
            {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }

            Column(
                modifier = Modifier.padding(start = 20.dp)
            ) {
                Text(
                    text = "GrowHubGPT",
                    fontSize = 20.sp,
                    color = Color(0xFF5669FF),
                    fontWeight = FontWeight.Medium,
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .size(6.dp)
                            .background(
                                color = if (isNetworkAvailable == ConnectionStatus.Unavailable) Color.Red else if (!isBotActive) Color(
                                    0xFFFFEB3B
                                ) else Color(
                                    0xFF4CAF50
                                ), shape = CircleShape
                            )
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = if (isNetworkAvailable == ConnectionStatus.Unavailable) "Offline" else if (!isBotActive) "Bot Inactive" else "Online",
                        color = if (isNetworkAvailable == ConnectionStatus.Unavailable) Color.Red else if (!isBotActive) Color(
                            0xFFFFEB3B
                        ) else Color(
                            0xFF4CAF50
                        ),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }

        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp),
            color = Color(0xFFDCD9D9)
        )
    }
}

@Composable
fun UserChat(
    modifier: Modifier = Modifier,
    message: String = ""
) {
    Surface(
        modifier = modifier,
        color = Color(0xFF5669FF),
        shape = RoundedCornerShape(topStart = 25.dp, bottomEnd = 25.dp, bottomStart = 25.dp)
    ) {
        Text(
            modifier = Modifier
                .wrapContentSize()
                .padding(horizontal = 24.dp, vertical = 18.dp),
            text = message,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelLarge.copy(color = Color.White)
        )
    }
}

@Composable
fun AssistantChat(
    modifier: Modifier = Modifier,
    message: String = ""
) {
    Row(
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier
                .wrapContentSize()
                .align(Alignment.Bottom),
            shape = CircleShape,
            color = Color.White,
            shadowElevation = 4.dp
        ) {
            Icon(
                painter = painterResource(id = R.drawable.chatbot),
                contentDescription = "Chatbot",
                modifier = Modifier.size(28.dp),
                tint = Color.Unspecified
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            shape = RoundedCornerShape(topStart = 25.dp, topEnd = 25.dp, bottomEnd = 25.dp),
            color = Color(0xFFA9B2C9)
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 24.dp),
                text = message,
                style = MaterialTheme.typography.labelLarge.copy(color = Color(0xFF505050))
            )
        }
    }
}

@Composable
fun ChatBar(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    onClickSend: () -> Unit,
    isNetworkAvailable: ConnectionStatus,
    isBotActive: Boolean
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shadowElevation = 2.dp,
        color = Color.White,
        shape = RoundedCornerShape(25.dp),
    ) {
        TextField(
            modifier = Modifier.background(color = Color.White),
            value = value,
            onValueChange = { value ->
                onValueChange(value)
            },
            placeholder = {
                Text(
                    text = "Ask a question...",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFDCD9D9)
                )
            },
            trailingIcon = {
                IconButton(
                    onClick = { onClickSend() },
                    enabled = isNetworkAvailable == ConnectionStatus.Available && isBotActive
                ){
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Sending Button",
                        modifier = Modifier
                            .size(24.dp),
                        tint = if (isNetworkAvailable == ConnectionStatus.Available && isBotActive) Color(0xFF5669FF) else Color(0xFFDCD9D9),
                    )
                }

            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                disabledContainerColor = Color.White,
                errorContainerColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent
            )

        )
    }
}

@Composable
fun ChatBotSectionEmpty() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "No internet connection icon",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No Internet Connection",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
