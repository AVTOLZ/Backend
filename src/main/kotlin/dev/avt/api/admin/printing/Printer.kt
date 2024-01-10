package dev.avt.api.admin.printing

import dev.avt.dotEnv
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*
import io.ktor.util.date.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.jsoup.Jsoup
import java.io.File
import java.io.InputStream
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.atomic.AtomicLong
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager
import kotlin.math.min

class Printer {
    val store = CustomCookiesStorage()
    private val httpClient = HttpClient(Apache) {
        install(HttpCookies) {
            storage = store
        }

        engine {
            sslContext = SSLContext.getInstance("TLS")
                .apply {
                    init(null, arrayOf(TrustAllX509TrustManager()), SecureRandom())
                }
        }
    }

    suspend fun login(username: String, password: String): Boolean {
        val csrfRequest = httpClient.get("https://print.nuovo.eu/end-user/ui/login")

        val jsoup = Jsoup.parse(csrfRequest.bodyAsText())
        val csrf = jsoup.selectXpath("//*[@id=\"login-form\"]/input").`val`()


        val params = parameters {
            append("username", username)
            append("password", password)
            append("_csrf", csrf)
        }

        val request = httpClient.submitForm("https://print.nuovo.eu/end-user/ui/j_spring_security_check", formParameters = params)

        return request.status == HttpStatusCode.Found
    }

    suspend fun print(file: InputStream, bw: Boolean = true, duplex: Boolean = false) {
        val csrfRequest = httpClient.get("https://print.nuovo.eu/end-user/ui/login")

        val jsoup = Jsoup.parse(csrfRequest.bodyAsText())
        val csrf = jsoup.selectXpath("//*[@id=\"navbar-upper-logout-form\"]/input").`val`()

        println(csrf)
        val response = httpClient.submitFormWithBinaryData(url = "https://print.nuovo.eu/end-user/ui/upload-job",
            formData = formData {
                append("bw", bw)
                append("duplex", duplex)
                append("importFile", file.readBytes(), Headers.build {
                    append(HttpHeaders.ContentDisposition, "filename=upload.pdf")
                })
            }
        ) {
            header("X-CSRF-TOKEN", csrf)
        }

        println(response.status)
    }


}

suspend fun main() {
    val printer = Printer()
    printer.login(dotEnv["PRINTER_ACCOUNT"], dotEnv["PRINTER_PASSWORD"])
    printer.print(File("print.pdf").inputStream())
}


class TrustAllX509TrustManager : X509TrustManager {
    override fun getAcceptedIssuers(): Array<X509Certificate?> = arrayOfNulls(0)

    override fun checkClientTrusted(certs: Array<X509Certificate?>?, authType: String?) {}

    override fun checkServerTrusted(certs: Array<X509Certificate?>?, authType: String?) {}
}


public class CustomCookiesStorage : CookiesStorage {
    private val container: MutableList<Cookie> = mutableListOf()
    private val oldestCookie: AtomicLong = AtomicLong(0L)
    private val mutex = Mutex()

    override suspend fun get(requestUrl: Url): List<Cookie> = mutex.withLock {
        val now = getTimeMillis()
        if (now >= oldestCookie.get()) cleanup(now)

        return@withLock container.filter { it.matches(requestUrl) }
    }

    override suspend fun addCookie(requestUrl: Url, cookie: Cookie): Unit = mutex.withLock {
        with(cookie) {
            if (cookie.name == "yui.locale") return@withLock
            if (name.isBlank()) return@withLock
        }

        container.removeAll { it.name == cookie.name && it.matches(requestUrl) }
        container.add(cookie.fillDefaults(requestUrl))
        cookie.expires?.timestamp?.let { expires ->
            if (oldestCookie.get() > expires) {
                oldestCookie.set(expires)
            }
        }
    }

    override fun close() {
    }

    private fun cleanup(timestamp: Long) {
        container.removeAll { cookie ->
            val expires = cookie.expires?.timestamp ?: return@removeAll false
            expires < timestamp
        }

        val newOldest = container.fold(Long.MAX_VALUE) { acc, cookie ->
            cookie.expires?.timestamp?.let { min(acc, it) } ?: acc
        }

        oldestCookie.set(newOldest)
    }
}

internal fun Cookie.matches(requestUrl: Url): Boolean {
    val domain = domain?.toLowerCasePreservingASCIIRules()?.trimStart('.')
        ?: error("Domain field should have the default value")

    val path = with(path) {
        val current = path ?: error("Path field should have the default value")
        if (current.endsWith('/')) current else "$path/"
    }

    val host = requestUrl.host.toLowerCasePreservingASCIIRules()
    val requestPath = let {
        val pathInRequest = requestUrl.encodedPath
        if (pathInRequest.endsWith('/')) pathInRequest else "$pathInRequest/"
    }

    if (host != domain && (hostIsIp(host) || !host.endsWith(".$domain"))) {
        return false
    }

    if (path != "/" &&
        requestPath != path &&
        !requestPath.startsWith(path)
    ) {
        return false
    }

    return !(secure && !requestUrl.protocol.isSecure())
}

internal fun Cookie.fillDefaults(requestUrl: Url): Cookie {
    var result = this

    if (result.path?.startsWith("/") != true) {
        result = result.copy(path = requestUrl.encodedPath)
    }

    if (result.domain.isNullOrBlank()) {
        result = result.copy(domain = requestUrl.host)
    }

    return result
}
