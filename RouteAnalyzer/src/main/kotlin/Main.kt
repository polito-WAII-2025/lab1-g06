package org.example
import java.io.File
import java.io.InputStream
import java.io.IO.println
import javax.lang.model.type.NullType

fun main(){
    val inputStream = object {}.javaClass.getResourceAsStream("/waypoints.csv")
    val lines = inputStream?.bufferedReader()?.readLines()
    if (lines != null) {
        for (line in lines){
            val splitted_line = line.split(";")
            println("Timestamp : ${splitted_line[0]} - Latitude ${splitted_line[1]} - Longitude ${splitted_line[1]}")
        }
    }
    else{
        println("Error opening file!")
    }


}