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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.androiddevchallenge.ui.theme.MyTheme

class MyCountdownTimer {
    lateinit var showMinute: String
    lateinit var timerImpl: CountDownTimer

    @Composable
    fun Timer() {
        val TIP_ERROR = "Please input legal number(0~59)."
        val TIP_DONE = "Time up!!!"

        var minValue by rememberSaveable { mutableStateOf("") }
        var secValue by rememberSaveable { mutableStateOf("") }
        var showMin by rememberSaveable { mutableStateOf("") }
        var showSec by rememberSaveable { mutableStateOf("") }
        var ready by rememberSaveable { mutableStateOf(true) }
        var resetEnable by rememberSaveable { mutableStateOf(false) }
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
            //display zone
            Row() {
                BigDigit(text = showMin)
                BigDigit(text = ":", false)
                BigDigit(text = showSec)
            }
            //setting zone
            Row(verticalAlignment = Alignment.CenterVertically) {
                fun secValueChange(value: String) {
                    var text = if (value.length <= 2) value else value.substring(0, 2)
                    if (text.toInt() > 59) {
                        openDialog.value = TIP_ERROR
                        text = ""
                    }
                    showSec = text
                    secValue = text
                }

                fun minValueChange(value: String) {
                    var text = if (value.length <= 2) value else value.substring(0, 2)
                    if (text.toInt() > 59) {
                        openDialog.value = TIP_ERROR
                        text = ""
                    }
                    showMin = text
                    minValue = text
                }
                InputField(enable = ready, value = minValue, onValueChange = { minValueChange(it) })
                Text(text = "min", modifier = Modifier.width(30.dp))
                InputField(enable = ready, value = secValue, onValueChange = { secValueChange(it) })
                Text(text = "sec", modifier = Modifier.width(30.dp))
            }
            //buttons
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = {
                        resetEnable = true;
                        if (ready) { //clicked start
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
                                }

                                override fun onFinish() {
                                    Log.d("zyf", "onFinish")
                                    reset()
                                    openDialog.value = TIP_DONE
                                }
                            }.start()
                        } else {    //clicked pause
                            timerImpl.cancel()
                        }
                        ready = !ready

                    }) {
                    Text(text = if (ready) "START" else "PAUSE")
                }
                Spacer(modifier = Modifier.width(10.dp))
                Button(
                    enabled = resetEnable,
                    onClick = {
                        reset()
                    }) {
                    Text(text = "RESET")
                }
            }

        }
        //warning dialog
        if (!openDialog.value.isEmpty()) {
            MyDialog(text = openDialog.value) { openDialog.value = "" }
        }

    }


}

@Composable
fun BigDigit(text: String, check: Boolean = true) {
    var real: String = text
    if (check) {
        if (text.isEmpty()) {
            real = "00"
        } else if (text.toInt() < 10) {
            real = "0$text"
        }
    }
    Text(
        text = if (check) real else text,
        style = MaterialTheme.typography.h1,
        color = Color.White
    )
}

@Composable
fun InputField(enable: Boolean = true, value: String, onValueChange: (String) -> Unit) {
    TextField(
        enabled = enable,
//        value = if (value.length<=2)value else value.substring(0,2),
        value = value,
        singleLine = true,
        modifier = Modifier
            .width(130.dp),
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


@Preview("Light Theme", widthDp = 360, heightDp = 640)
@Composable
fun LightPreview() {
    MyTheme {
        MyCountdownTimer().Timer()
    }
}

