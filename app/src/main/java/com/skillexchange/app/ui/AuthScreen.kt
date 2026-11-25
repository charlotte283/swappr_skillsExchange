package com.skillexchange.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.skillexchange.app.data.AREAS

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onLogin: (email: String, pass: String) -> Boolean,
    onRegister: (email: String, pass: String, name: String, area: String, bio: String) -> Boolean
) {
    var isSignUp by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("Kilimani") }
    var bio by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // App Branding Logo Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF34D399)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Swappr Logo",
                        tint = Color(0xFF064E3B),
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = "swappr",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF8FAFC)
                    )
                    Text(
                        text = "Skills Exchange Marketplace",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            // Mode Toggle (Sign In vs Create Account)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = !isSignUp,
                    onClick = { isSignUp = false; errorMessage = null },
                    label = { Text("Sign In") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF34D399),
                        selectedLabelColor = Color(0xFF064E3B),
                        containerColor = Color(0xFF1E293B),
                        labelColor = Color(0xFFF8FAFC)
                    ),
                    border = FilterChipDefaults.filterChipBorder(enabled = true, selected = !isSignUp, borderColor = Color(0xFF334155))
                )
                FilterChip(
                    selected = isSignUp,
                    onClick = { isSignUp = true; errorMessage = null },
                    label = { Text("Create Account") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF34D399),
                        selectedLabelColor = Color(0xFF064E3B),
                        containerColor = Color(0xFF1E293B),
                        labelColor = Color(0xFFF8FAFC)
                    ),
                    border = FilterChipDefaults.filterChipBorder(enabled = true, selected = isSignUp, borderColor = Color(0xFF334155))
                )
            }

            // Glassmorphic Auth Form Card
            MarketplaceCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = if (isSignUp) "Create Your Account" else "Welcome Back",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF8FAFC)
                    )

                    if (errorMessage != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x33EF4444))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = errorMessage!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFFCA5A5)
                            )
                        }
                    }

                    if (isSignUp) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            singleLine = true,
                            label = { Text("Full Name") },
                            placeholder = { Text("e.g. Charlotte Wanjiru") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF34D399)) },
                            colors = marketplaceTextFieldColors(),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        singleLine = true,
                        label = { Text("Email Address") },
                        placeholder = { Text("you@example.com") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF34D399)) },
                        colors = marketplaceTextFieldColors(),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        singleLine = true,
                        label = { Text("Password") },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF34D399)) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle Password Visibility",
                                    tint = Color(0xFF94A3B8)
                                )
                            }
                        },
                        colors = marketplaceTextFieldColors(),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (isSignUp) {
                        Dropdown(
                            label = "Neighborhood Area",
                            options = AREAS.map { it.name },
                            selected = area,
                            onSelect = { area = it },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = bio,
                            onValueChange = { bio = it },
                            minLines = 2,
                            maxLines = 3,
                            label = { Text("Bio / Intro (Optional)") },
                            placeholder = { Text("Share your background and skills...") },
                            colors = marketplaceTextFieldColors(),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    Button(
                        onClick = {
                            errorMessage = null
                            if (isSignUp) {
                                if (name.isBlank() || email.isBlank() || password.isBlank()) {
                                    errorMessage = "Please fill in all required fields."
                                    return@Button
                                }
                                val success = onRegister(email, password, name, area, bio.ifBlank { "Skill exchange enthusiast." })
                                if (!success) {
                                    errorMessage = "Account already exists with this email."
                                }
                            } else {
                                if (email.isBlank() || password.isBlank()) {
                                    errorMessage = "Please enter your email and password."
                                    return@Button
                                }
                                val success = onLogin(email, password)
                                if (!success) {
                                    errorMessage = "Invalid email or password. Please try again or create an account."
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(26.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34D399), contentColor = Color(0xFF064E3B))
                    ) {
                        Text(
                            text = if (isSignUp) "Create Account" else "Sign In",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF064E3B)
                        )
                    }
                }
            }

            // Footer helper prompt
            Text(
                text = if (isSignUp) "Already have an account? Sign In" else "Don't have an account? Create One",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF34D399),
                modifier = Modifier.clickable { isSignUp = !isSignUp; errorMessage = null }
            )
        }
    }
}
