package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppScreen
import com.example.ui.DocumentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecuritySettingsScreen(
    viewModel: DocumentViewModel,
    modifier: Modifier = Modifier
) {
    var passwordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(
                            text = "ENCRYPTED WORKSPACE",
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            letterSpacing = 1.2.sp,
                            color = Color(0xFF6750A4)
                        )
                        Text(
                            text = "Vault Cryptography",
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            fontWeight = FontWeight.Light,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.currentScreen = AppScreen.DASHBOARD }) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color(0xFF6750A4))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFEF7FF)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFFEF7FF))
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // High Security Shield Banner
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFF3EDF7))
                    .border(1.dp, Color(0xFFD0BCFF), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.EnhancedEncryption,
                    contentDescription = null,
                    tint = Color(0xFF6750A4),
                    modifier = Modifier.size(40.dp)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "On-Device Cryptography",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF1D1B20)
                )
                Text(
                    text = "Your files are encrypted using hardware-backed AES-256.",
                    fontSize = 11.sp,
                    color = Color(0xFF49454F),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Current Vault State Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFCAC4D0), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Encryption Level:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF49454F))
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFEADDFF), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("AES-256-CBC", fontSize = 10.sp, color = Color(0xFF21005D), fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Passphrase Status:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF49454F))
                        val isCustom = viewModel.securityPassword != null
                        Text(
                            text = if (isCustom) "Elevated Custom Password" else "Secure Background Keychain",
                            color = if (isCustom) Color(0xFF6750A4) else Color(0xFF1D1B20),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Active Lock State:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF49454F))
                        Text(
                            text = if (viewModel.isVaultUnlocked) "Session UNLOCKED" else "Session LOCKED",
                            color = if (viewModel.isVaultUnlocked) Color(0xFF6750A4) else MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Divider(color = Color(0xFFCAC4D0).copy(alpha = 0.5f))

            // Passphrase Configuration Input fields
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Configure Vault Passphrase",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFF1D1B20)
                )
                Text(
                    text = "Setting a custom passphrase derives a unique AES-256 key locally. If you lock the vault or sign out, files cannot be decrypted without this password.",
                    fontSize = 10.sp,
                    color = Color(0xFF49454F)
                )
                
                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = { passwordInput = it },
                    label = { Text("Vault Master Password") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF3EDF7),
                        unfocusedContainerColor = Color(0xFFF3EDF7),
                        focusedBorderColor = Color(0xFFCAC4D0),
                        unfocusedBorderColor = Color(0xFFCAC4D0)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("vault_password_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            if (passwordInput.isNotEmpty()) {
                                viewModel.securityPassword = passwordInput
                                viewModel.isVaultUnlocked = true
                                passwordInput = ""
                            }
                        },
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6750A4),
                            contentColor = Color.White
                        ),
                        modifier = Modifier.weight(1f).testTag("save_password_btn")
                    ) {
                        Icon(Icons.Default.VpnKey, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Set Key", fontSize = 12.sp)
                    }

                    if (viewModel.securityPassword != null) {
                        OutlinedButton(
                            onClick = {
                                viewModel.securityPassword = null
                                viewModel.isVaultUnlocked = true
                                passwordInput = ""
                            },
                            shape = RoundedCornerShape(24.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                            modifier = Modifier.weight(1f).testTag("clear_password_btn")
                        ) {
                            Text("Reset Key", fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Lock / Unlock session actions
            if (viewModel.securityPassword != null) {
                Button(
                    onClick = {
                        viewModel.isVaultUnlocked = !viewModel.isVaultUnlocked
                    },
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth().testTag("lock_session_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (viewModel.isVaultUnlocked) Color(0xFFEF4444) else Color(0xFF6750A4)
                    )
                ) {
                    Icon(
                        imageVector = if (viewModel.isVaultUnlocked) Icons.Default.Lock else Icons.Default.LockOpen,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (viewModel.isVaultUnlocked) "Lock Secure Session" else "Unlock Secure Session",
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Open-Source Audit/Transparency Disclosure card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFD0BCFF), RoundedCornerShape(16.dp))
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, "Audited", tint = Color(0xFF6750A4), modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Audited Secure Sandbox", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1D1B20))
                        Text(
                            "PrivaEdit complies with the highest Android security profiles: 0 trackers, 0 cloud-calls, 0 telemetry. Fully verifiable and transparent.",
                            fontSize = 10.sp,
                            color = Color(0xFF49454F)
                        )
                    }
                }
            }
        }
    }
}
