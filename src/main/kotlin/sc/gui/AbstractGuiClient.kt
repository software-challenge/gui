package sc.gui

import sc.plugin2021.AbstractClient

abstract class AbstractGuiClient(host: String, port: Int): ClientInterface, AbstractClient(host, port) {
}