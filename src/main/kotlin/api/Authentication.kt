package org.iamramtin.api

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.vertx.core.Vertx
import io.vertx.ext.auth.PubSecKeyOptions
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.auth.jwt.JWTAuthOptions
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.JWTAuthHandler
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import java.util.*

class Authentication (private val vertx: Vertx) {
    private val secret = "secret"
    private val jwtAuth: JWTAuth

    init {
        val jwtAuthOptions = JWTAuthOptions()
            .addPubSecKey(PubSecKeyOptions()
                .setAlgorithm("HS256")
                .setBuffer(secret))

        jwtAuth = JWTAuth.create(null, jwtAuthOptions)
    }

    fun setup(router: Router) {
        // Create a sub-router for authenticated routes
        val authenticatedRouter = Router.router(vertx)

        // Create a JWT auth handler
        val jwtAuthHandler = JWTAuthHandler.create((jwtAuth))

        // Apply JWT auth to all routes in the authenticated router
        authenticatedRouter.route().handler(jwtAuthHandler)

        // Setup login route on the main router (no authentication required)
        router.post("/login").handler { ctx ->
            val body = ctx.body().asJsonObject()
            val username = body.getString("username")
            val password = body.getString("password")

            // Here you should check the username and password against your user database
            if (username == "username" && password == "password") {
                val token = generateToken(username)
                ctx.response()
                    .putHeader("content-type", "application/json")
                    .end(json {
                        obj("token" to token)
                    }.encode())
            } else {
                ctx.response().setStatusCode(401).end("Invalid credentials")
            }
        }

        // Mount the authenticated sub-router to handle all other routes
        router.route("/*").subRouter(authenticatedRouter)
    }

    private fun generateToken(username: String): String {
        return JWT.create()
            .withSubject(username)
            .withExpiresAt(Date(System.currentTimeMillis() + 12 * 60 * 60 * 1000)) // 12 hours
            .sign(Algorithm.HMAC256(secret))
    }
}