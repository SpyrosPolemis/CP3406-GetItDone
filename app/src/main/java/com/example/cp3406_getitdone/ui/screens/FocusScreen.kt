package com.example.cp3406_getitdone.ui.screens

import androidx.compose.runtime.*
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONArray

@Composable
fun FocusScreen() {
    var focusTimeMinutes by remember { mutableStateOf(10) }
    var timeLeft by remember { mutableStateOf(focusTimeMinutes * 60) }
    var isFocusing by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    val toneGenerator = remember {
        ToneGenerator(AudioManager.STREAM_ALARM, 100)
    }

    // Manage beep job lifecycle separately
    var beepJob by remember { mutableStateOf<Job?>(null) }

    var quoteText by remember { mutableStateOf("Loading inspirational quote...") }

    // Fetch quote when screen loads
    LaunchedEffect(Unit) {
        quoteText = fetchRandomQuote()
    }

    // Lifecycle observer to start/stop beep loop
    DisposableEffect(Unit) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> {
                    if (isFocusing && timeLeft > 0) {
                        // Start looping beep
                        beepJob = coroutineScope.launch {
                            while (isActive && isFocusing && timeLeft > 0) {
                                toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP)
                                delay(500)
                                toneGenerator.stopTone()
                                delay(1000)
                            }
                        }
                    }
                }

                Lifecycle.Event.ON_START -> {
                    // Stop beep when app is foreground
                    beepJob?.cancel()
                    beepJob = null
                    toneGenerator.stopTone()
                }

                else -> {}
            }
        }
        ProcessLifecycleOwner.get().lifecycle.addObserver(observer)

        onDispose {
            ProcessLifecycleOwner.get().lifecycle.removeObserver(observer)
            beepJob?.cancel()
            toneGenerator.stopTone()
            toneGenerator.release()
        }
    }

    // Timer
    DisposableEffect(isFocusing) {
        val timerJob = if (isFocusing) {
            coroutineScope.launch {
                while (timeLeft > 0 && isFocusing) {
                    delay(1000L)
                    timeLeft--
                }
                if (timeLeft <= 0) {
                    isFocusing = false
                    // stop beep when timer ends
                    beepJob?.cancel()
                    beepJob = null
                    toneGenerator.stopTone()
                }
            }
        } else null

        onDispose {
            timerJob?.cancel()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Set Focus Time (minutes):")
        Slider(
            value = focusTimeMinutes.toFloat(),
            onValueChange = {
                if (!isFocusing) {
                    focusTimeMinutes = it.toInt()
                    timeLeft = focusTimeMinutes * 60
                }
            },
            valueRange = 1f..120f,
            steps = 119,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        Text("$focusTimeMinutes minutes", fontSize = 18.sp)

        Spacer(modifier = Modifier.height(24.dp))

        Text("Time left: ${timeLeft / 60}m ${timeLeft % 60}s", fontSize = 32.sp)

        Spacer(modifier = Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = { if (!isFocusing && timeLeft > 0) isFocusing = true },
                enabled = !isFocusing && timeLeft > 0
            ) {
                Text("Start")
            }
            Button(
                onClick = { isFocusing = false },
                enabled = isFocusing
            ) {
                Text("Stop")
            }
            Button(
                onClick = {
                    isFocusing = false
                    timeLeft = focusTimeMinutes * 60
                }
            ) {
                Text("Reset")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(quoteText, fontSize = 14.sp, modifier = Modifier.padding(16.dp))
    }
}

suspend fun fetchRandomQuote(): String {
    return withContext(Dispatchers.IO) {
        try {
            val url = URL("https://zenquotes.io/api/today")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"

            if (connection.responseCode == 200) {
                val response = connection.inputStream.bufferedReader().readText()
                val jsonArray = JSONArray(response)
                val jsonObject = jsonArray.getJSONObject(0)
                val quote = jsonObject.getString("q")
                val author = jsonObject.getString("a")
                "\"$quote\" — $author"
            } else {
                "Failed to load quote."
            }
        } catch (e: Exception) {
            "Error loading quote."
        }
    }
}
