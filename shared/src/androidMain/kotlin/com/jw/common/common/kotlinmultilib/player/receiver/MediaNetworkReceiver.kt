package com.jw.common.common.kotlinmultilib.player.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import com.jw.common.common.kotlinmultilib.player.listener.OnConnectionChangeListener
import java.util.logging.Logger

/**
 *Created by Joyce.wang on 2024/9/11 17:32
 *@Description TODO
 */
class MediaNetworkReceiver : BroadcastReceiver {
    companion object {
        private val TAG: String = MediaNetworkReceiver::class.java.simpleName
    }

    constructor(listener: OnConnectionChangeListener?) : super() {
        this.listener = listener
    }

    private var listener: OnConnectionChangeListener? = null
    private var type: OnConnectionChangeListener.ConnectionType =
        OnConnectionChangeListener.ConnectionType.UNKNOWN

    fun getType(): OnConnectionChangeListener.ConnectionType {
        return type
    }
    override fun onReceive(context: Context?, intent: Intent?) {
        Logger.getLogger(TAG).info("VodMediaReceiver onReceive....")
        if (intent!!.action == Intent.ACTION_LOCALE_CHANGED) {
            Logger.getLogger(TAG).info("system language change")
            if (listener != null) listener!!.onLocaleChange()
        } else if (intent!!.action == ConnectivityManager.CONNECTIVITY_ACTION) {
            var newType = OnConnectionChangeListener.ConnectionType.UNKNOWN
            val connectivityManager =
                context!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connectivityManager.activeNetworkInfo
            if (networkInfo == null) {
                /** 没有任何网络  */
                Logger.getLogger(TAG).info("no active network")
                newType = OnConnectionChangeListener.ConnectionType.NONE
            } else {
                if (!networkInfo.isConnected) {
                    Logger.getLogger(TAG).info("no connected network")
                    newType = OnConnectionChangeListener.ConnectionType.NONE
                } else {
                    Logger.getLogger(TAG).info("active network is:" + networkInfo.type + ", state:" + networkInfo.isConnectedOrConnecting + ":" + networkInfo.isConnected)
                    if (networkInfo.type == ConnectivityManager.TYPE_ETHERNET) {
                        /** 以太网网络  */
                        newType = OnConnectionChangeListener.ConnectionType.ETHERNET
                    } else if (networkInfo.type == ConnectivityManager.TYPE_WIFI) {
                        /** wifi网络，当激活时，默认情况下，所有的数据流量将使用此连接  */
                        newType = OnConnectionChangeListener.ConnectionType.WIFI
                    } else if (networkInfo.type == ConnectivityManager.TYPE_MOBILE) {
                        /** mobile网络  */
                        newType = OnConnectionChangeListener.ConnectionType.MOBILE
                    }
                }
            }

            if (null != listener && newType !== type) {
                type = newType
                listener!!.onConnectionChange(type)
            }
        }
    }
}