package org.iamramtin

import org.iamramtin.api.Server
import io.vertx.core.Vertx

fun main() {
    val vertx = Vertx.vertx()
    vertx.deployVerticle(Server())
}
