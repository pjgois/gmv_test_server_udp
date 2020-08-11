package com.gmv

import java.io.*
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.text.MessageFormat

const val STX: Byte = 0x02
const val ETX: Byte = 0x03

class UDPServer : Thread() {

    private val datagramSocket = DatagramSocket(4445)
    private val buf = ByteArray(256)
    private var running = false

    override fun run() {

        running = true
        while (running /*!datagramSocket.isClosed*/) {

            var datagramPacket = DatagramPacket(buf, buf.size)

            datagramSocket.receive(datagramPacket) // This one is the blocking one!

            val address = datagramPacket.address
            val port = datagramPacket.port

            val byteArrayInputStream = ByteArrayInputStream(datagramPacket.data)
            val byteArrayOutputStream = ByteArrayOutputStream()
            val dataInputStream = DataInputStream(byteArrayInputStream)

            var readInt: Int?
            do {
                readInt = byteArrayInputStream.read()
                when (readInt) {
                    STX.toInt() -> byteArrayOutputStream.write(readInt)
                    ETX.toInt() -> {
                        byteArrayOutputStream.write(readInt)
                        println(MessageFormat.format("Received new message: {0}", byteArrayOutputStream))
                        byteArrayOutputStream.reset()
                    }
                    else -> byteArrayOutputStream.write(readInt.toInt())
                }
            } while (readInt != -1)

            val toSendByteArray = msgByteArray()

            datagramPacket = DatagramPacket(toSendByteArray, toSendByteArray.size, address, port)

            datagramSocket.send(datagramPacket)
        }
        datagramSocket.close()
    }

    private fun msgByteArray(): ByteArray {

        val msg = "OK"
        try {
            ByteArrayOutputStream().use { byteArrayOutputStream ->
                DataOutputStream(byteArrayOutputStream).use { dataOutputStream ->
                    dataOutputStream.write(STX.toInt())
                    dataOutputStream.write(msg.toByteArray())
                    dataOutputStream.write(ETX.toInt())
                    dataOutputStream.flush()
                }
                return byteArrayOutputStream.toByteArray()
            }
        } catch (e: IOException) {
            println(MessageFormat.format("Error while creating message: {0}", e.message))
            return ByteArray(0)
        }
    }
}