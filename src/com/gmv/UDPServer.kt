package com.gmv

import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.text.MessageFormat

const val STX: Byte = 0x02
const val ETX: Byte = 0x03

class UDPServer : Thread() {

    private val socket: DatagramSocket = DatagramSocket(4445)
    private val buf = ByteArray(256)
    private var running = false

    override fun run() {

        running = true
        while (running /*!socket.isClosed*/) {
            var packet = DatagramPacket(buf, buf.size)
            socket.receive(packet)
            val address = packet.address
            val port = packet.port

            println(MessageFormat.format("Received message: {0}", String(packet.data, 0, packet.length)))

            val toSendByteArray = msgByteArray()

            packet = DatagramPacket(toSendByteArray, toSendByteArray.size, address, port)

            socket.send(packet)
        }
        socket.close()
    }

    private fun msgByteArray(): ByteArray {

        val ok = "OK"
        val nok = "NOK"
        val msg = ok
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