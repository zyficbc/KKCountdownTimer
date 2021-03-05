/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.androiddevchallenge.ui

import android.os.CountDownTimer
import android.util.Log
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.androiddevchallenge.ui.theme.MyTheme

lateinit var timerImpl: CountDownTimer

@Composable
fun Timer() {
    val tipError = "Please input legal number(0~59)."
    val tipDone = "Time up!!!"

    var minValue by rememberSaveable { mutableStateOf("") }
    var secValue by rememberSaveable { mutableStateOf("") }
    var showMin by remember { mutableStateOf("") }
    var showSec by remember { mutableStateOf("") }
    var ready by remember { mutableStateOf(true) }
    var resetEnable by remember { mutableStateOf(false) }
    val openDialog = remember { mutableStateOf("") }

    fun reset() {
        resetEnable = false
        ready = true
        timerImpl.cancel()
        showMin = ""
        showSec = ""
        secValue = ""
        minValue = ""
    }
    Column(
        modifier = Modifier
            .background(Color.DarkGray)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // display zone
        Row() {
            BigDigit(text = showMin)
            if (ready) {
                BigDigit(text = ":", false)
            } else {
                val infiniteTransition = rememberInfiniteTransition()
                val alpha by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 0f,
                    animationSpec = infiniteRepeatable(
                        animation = keyframes {
                            durationMillis = 500
                            0.9f at 250
                        },
                        repeatMode = RepeatMode.Reverse
                    )
                )
                BigDigit(text = ":", false, alpha = alpha)
            }
            BigDigit(text = showSec)
        }
        Spacer(modifier = Modifier.height(30.dp))
        // setting zone
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            fun secValueChange(value: String) {
                var text = if (value.length <= 2) value else value.substring(0, 2)
                if (text.toIntOrNull() == null || text.isNotEmpty() && text.toInt() > 59) {
                    openDialog.value = tipError
                    text = ""
                }
                showSec = text
                secValue = text
            }

            fun minValueChange(value: String) {
                var text = if (value.length <= 2) value else value.substring(0, 2)
                if (text.toIntOrNull() == null || text.isNotEmpty() && text.toInt() > 59) {
                    openDialog.value = tipError
                    text = ""
                }
                showMin = text
                minValue = text
            }
            InputField(enable = ready, value = minValue, onValueChange = { minValueChange(it) })
            Text(
                text = "min",
                modifier = Modifier
                    .width(30.dp),
                color = Color.LightGray
            )
            Spacer(modifier = Modifier.width(10.dp))
            InputField(enable = ready, value = secValue, onValueChange = { secValueChange(it) })
            Text(
                text = "sec",
                modifier = Modifier
                    .width(30.dp),
                color = Color.LightGray
            )
        }
        Spacer(modifier = Modifier.height(60.dp))

        // buttons
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(
                onClick = {

                    if (minValue.isEmpty() && secValue.isEmpty()) {
                        openDialog.value = tipError
                        return@Button
                    }

                    resetEnable = true
                    if (ready) { // clicked start
                        var min: Int = (if (minValue.isEmpty()) 0 else minValue.toInt())
                        var sec = if (secValue.isEmpty()) 0 else secValue.toInt()
                        val time = (min * 60 + sec) * 1000

                        timerImpl = object : CountDownTimer(time.toLong(), 1000) {
                            var temp = 0
                            override fun onTick(millisUntilFinished: Long) {
                                if (temp == 0) {
                                    temp = 1
                                    return
                                }
                                Log.d("zyf", "onTick: $millisUntilFinished")
                                if (sec == 0) {
                                    sec = 59
                                    min--
                                } else {
                                    sec--
                                }
                                showSec = "$sec"
                                showMin = "$min"
                                Log.d("zyf", "onTick: $showMin, $showSec")
                            }

                            override fun onFinish() {
                                Log.d("zyf", "onFinish")
                                openDialog.value = tipDone
                                reset()
                            }
                        }.start()
                    } else { // clicked pause
                        timerImpl.cancel()
                        minValue = if (showMin.toIntOrNull() == 0) "" else showMin
                        secValue = showSec
                    }
                    ready = !ready
//                    runningAnimation = !runningAnimation
                }
            ) {
                Text(text = if (ready) "START" else "PAUSE")
            }
            Spacer(modifier = Modifier.width(20.dp))
            Button(
                enabled = resetEnable,
                onClick = {
//                    reset()
                    resetEnable = false
                    ready = true
                    timerImpl.cancel()
                    showMin = ""
                    showSec = ""
                    secValue = ""
                    minValue = ""
                }
            ) {
                Text(text = "RESET")
            }
        }
    }
    // warning dialog
    if (!openDialog.value.isEmpty()) {
        MyDialog(text = openDialog.value) { openDialog.value = "" }
    }
}

@Composable
fun BigDigit(text: String, check: Boolean = true, alpha: Float = 1f) {

    Log.d("zyf", "BigDigit: $text : $alpha")
    var real: String = text
    if (check) {
        if (text.isEmpty()) {
            real = "00"
        } else if (text.toInt() < 10) {
            real = "0$text"
        }
    }
    if (check) {
        Text(
            text = if (check) real else text,
            style = MaterialTheme.typography.h1,
            color = Color.White
        )
    } else {
        Text(
            text = if (check) real else text,
            style = MaterialTheme.typography.h1,
            color = Color.White.copy(alpha = alpha)
        )
    }
}

@Composable
fun InputField(enable: Boolean = true, value: String, onValueChange: (String) -> Unit) {
    TextField(
        enabled = enable,
        value = value,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = Modifier
            .width(100.dp)
            .padding(10.dp)
            .background(Color.White),
        onValueChange = onValueChange
    )
}

@Composable
fun MyDialog(text: String, onClick: () -> Unit) {
    AlertDialog(
        onDismissRequest = {
        },
        text = {
            Text(
                text
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onClick()
                }
            ) {
                Text("Confirm")
            }
        }
    )
}

@ExperimentalAnimationApi
@Preview("Light Theme", widthDp = 360, heightDp = 640)
@Composable
fun LightPreview() {
    MyTheme {
        Timer()
    }
}
