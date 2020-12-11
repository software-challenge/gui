package sc.gui.controller.client

import sc.plugin2021.AbstractClient

abstract class AbstractGuiClient(host: String, port: Int): ClientInterface, AbstractClient(host, port) {
    private val location = "$host:$port"
    override fun toString() = super.toString() + " type $type on $location"
}
