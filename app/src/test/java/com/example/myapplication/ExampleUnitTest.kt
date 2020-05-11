package com.example.myapplication

import android.os.Environment
import org.junit.Test
import java.io.File
import java.io.OutputStream
import java.lang.Double.NaN
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@Suppress("DEPRECATION")
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {


        print(6356*2*Math.asin(sqrt(
            Math.pow(
                sin((0.001*3.1415926535/180.0) / 2),
                2.0
            ) + cos(59.0*3.1415926535/180.0)*sin(59.0*3.1415926535/180.0)* Math.pow(
                sin((0*3.1415926535/180.0) / 2), 2.0
            )
        )))
    }

    fun alignFile(inputName: String, lineLength: Int, outputName: String) {
        val writer = File(outputName).bufferedWriter()
        var currentLineLength = 0
        for (line in File(inputName).readLines()) {
            if (line.isEmpty()) {
                writer.newLine()
                if (currentLineLength > 0) {
                    writer.newLine()
                    currentLineLength = 0
                }
            }
            for (word in line.split(Regex("\\s+"))) {
                if (currentLineLength > 0) {
                    if (word.length + currentLineLength >= lineLength) {
                        writer.newLine()
                        currentLineLength = 0
                    }
                    else {
                        writer.write(" ")
                        currentLineLength++
                    }
                }
                writer.write(word)
                currentLineLength += word.length
            }
        }
        writer.close()
    }
}

