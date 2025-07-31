// Version 3: Added Top App Bar with Settings and Add buttons, and a task creation dialog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SimpleApp()
        }
    }
}

@Composable
fun SimpleApp() {
    var selectedScreen by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.List, contentDescription = "Tasks") },
                    selected = selectedScreen == 0,
                    onClick = { selectedScreen = 0 },
                    label = { Text("Tasks") }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Star, contentDescription = "Goals") },
                    selected = selectedScreen == 1,
                    onClick = { selectedScreen = 1 },
                    label = { Text("Goals") }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Lock, contentDescription = "Focus") },
                    selected = selectedScreen == 2,
                    onClick = { selectedScreen = 2 },
                    label = { Text("Focus") }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedScreen) {
                0 -> ShortTermTaskScreen()
                1 -> PlaceholderScreen("Goals & Habits Coming Soon")
                2 -> PlaceholderScreen("Focus Mode Coming Soon")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShortTermTaskScreen() {
    val tasks = remember { mutableStateListOf<String>() }
    var showDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Today's Tasks") },
            navigationIcon = {
                IconButton(onClick = { /* TODO: Navigate to settings */ }) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings")
                }
            },
            actions = {
                IconButton(onClick = { showDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Task")
                }
            }
        )

        LazyColumn(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(tasks) { task ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(task, modifier = Modifier.padding(16.dp))
                }
            }
        }
    }

    if (showDialog) {
        var name by remember { mutableStateOf(TextFieldValue("")) }
        var dueDate by remember { mutableStateOf(TextFieldValue("")) } // Placeholder
        var reminder by remember { mutableStateOf(TextFieldValue("")) } // Placeholder

        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (name.text.isNotBlank()) {
                            tasks.add(name.text)
                            showDialog = false
                        }
                    }
                ) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            },
            title = { Text("New Task") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextField(value = name, onValueChange = { name = it }, label = { Text("Task Name") })
                    TextField(value = dueDate, onValueChange = { dueDate = it }, label = { Text("Due Date") })
                    TextField(value = reminder, onValueChange = { reminder = it }, label = { Text("Reminder") })
                }
            }
        )
    }
}

@Composable
fun PlaceholderScreen(text: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text)
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    SimpleApp()
}
