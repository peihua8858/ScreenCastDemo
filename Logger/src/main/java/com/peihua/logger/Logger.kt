package com.peihua.logger

import android.util.Log
import androidx.annotation.IntDef
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

class Logger {
    private val logs = mutableStateListOf<LogData>()
    val itemCount: Int
        get() = logs.size

    operator fun get(index: Int): LogData {
        return logs[index]
    }

    private fun addLog(log: String) {
        logs.add(0, LogData(Log.DEBUG, log, System.currentTimeMillis()))
        Log.d("Logger", log)
    }

    private fun addELog(log: String) {
        logs.add(0, LogData(Log.ERROR, log, System.currentTimeMillis()))
        Log.e("Logger", log)
    }

    private fun addWLog(log: String) {
        logs.add(0, LogData(Log.WARN, log, System.currentTimeMillis()))
        Log.w("Logger", log)
    }

    private fun addILog(log: String) {
        logs.add(0, LogData(Log.INFO, log, System.currentTimeMillis()))
        Log.i("Logger", log)
    }


    companion object {
        val mLogger = Logger()
        fun addLog(log: String) {
            mLogger.addLog(log)
        }

        fun addELog(log: String) {
            mLogger.addELog(log)
        }

        fun addWLog(log: String) {
            mLogger.addWLog(log)
        }
    }

}

@Composable
fun LoggerList(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0x99000000))
            .padding(10.dp),
        reverseLayout = true
    ) {
        items(Logger.mLogger.itemCount) {
            LogItemView(Logger.mLogger[it])
        }
    }
}

@Composable
private fun LogItemView(log: LogData) {
    when (log.type) {
        Log.DEBUG -> Text(log.log, color = Color.Blue)
        Log.ERROR -> Text(log.log, color = Color.Red)
        Log.WARN -> Text(log.log, color = Color.Yellow)
        Log.INFO -> Text(log.log, color = Color.Green)
        Log.VERBOSE -> Text(log.log, color = Color.Gray)
        else -> Text(log.log, color = Color.Gray)
    }
}

@IntDef(Log.ASSERT, Log.ERROR, Log.WARN, Log.INFO, Log.DEBUG, Log.VERBOSE)
@Retention(AnnotationRetention.SOURCE)
annotation class Level
data class LogData(@Level val type: Int, val log: String, val time: Long)