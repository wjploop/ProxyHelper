package com.wjp.carrot.proxyhelper

import android.content.Context
import android.net.ProxyInfo
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val wifiManager=applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val configurationList=wifiManager.configuredNetworks
        lateinit var wifiConfiguration: WifiConfiguration
        val cur=wifiManager.connectionInfo.networkId
        configurationList.forEach{
            if (it.networkId == cur) {
                wifiConfiguration=it
            }
        }

        wifiConfiguration.setHttpProxyCompat(ProxyInfo.buildDirectProxy("192.168.10.77", 8888))

        wifiManager.updateNetwork(wifiConfiguration)
        wifiManager.disconnect()
        wifiManager.reconnect()
    }
}


fun WifiConfiguration.setHttpProxyCompat(proxyInfo: ProxyInfo) {
    if (Build.VERSION.SDK_INT >= 26) {
        httpProxy = proxyInfo
        Timber.i("Setting proxy using 26+ method")
    } else {
        val proxySettings = Class.forName("android.net.IpConfiguration\$ProxySettings")
        val valueOf = proxySettings.getMethod("valueOf", String::class.java)
        val static = valueOf.invoke(proxySettings, "STATIC")

        val setProxy = this::class.java.getDeclaredMethod("setProxy", proxySettings, ProxyInfo::class.java)
        setProxy.isAccessible = true

        setProxy.invoke(this, static, proxyInfo)
        Timber.i("Setting proxy using reflection")
    }
}
