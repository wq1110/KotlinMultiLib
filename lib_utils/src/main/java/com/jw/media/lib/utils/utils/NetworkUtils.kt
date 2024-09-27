package com.jw.media.lib.utils.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.telephony.TelephonyManager
import com.jw.media.lib.utils.provider.ContextProvider

/**
 *Created by Joyce.wang on 2024/9/27 10:18
 *@Description TODO
 */
class NetworkUtils {
    companion object {
        fun isMobileNetwork(): Boolean {
            val connectMgr = ContextProvider.getContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connectMgr.activeNetworkInfo

            if (networkInfo != null && networkInfo.isConnected && networkInfo.type == ConnectivityManager.TYPE_MOBILE) {
                return true
            }
            return false
        }

        fun isConnectingToInternet(): Boolean {
            val connectMgr = ContextProvider.getContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val info: Array<NetworkInfo> = connectMgr.getAllNetworkInfo()
            for (i in info.indices) if (info[i].state == NetworkInfo.State.CONNECTED) {
                return true
            }
            return false
        }

        /**
         * 判断当前网络类型-1为未知网络0为没有网络连接1网络断开或关闭2为以太网3为WiFi4为2G5为3G6为4G
         */
        fun getNetworkType(): Int {
            val connectMgr = ContextProvider.getContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            val networkInfo = connectMgr.activeNetworkInfo
                ?:
                /** 没有任何网络  */
                return 0
            if (!networkInfo.isConnected) {
                /** 网络断开或关闭  */
                return 1
            }
            if (networkInfo.type == ConnectivityManager.TYPE_ETHERNET) {
                /** 以太网网络  */
                return 2
            } else if (networkInfo.type == ConnectivityManager.TYPE_WIFI) {
                /** wifi网络，当激活时，默认情况下，所有的数据流量将使用此连接  */
                return 3
            } else if (networkInfo.type == ConnectivityManager.TYPE_MOBILE) {
                /** 移动数据连接,不能与连接共存,如果wifi打开，则自动关闭  */
                when (networkInfo.subtype) {
                    TelephonyManager.NETWORK_TYPE_GPRS, TelephonyManager.NETWORK_TYPE_EDGE, TelephonyManager.NETWORK_TYPE_CDMA, TelephonyManager.NETWORK_TYPE_1xRTT, TelephonyManager.NETWORK_TYPE_IDEN ->
                        /** 2G网络  */
                        return 4

                    TelephonyManager.NETWORK_TYPE_UMTS, TelephonyManager.NETWORK_TYPE_EVDO_0, TelephonyManager.NETWORK_TYPE_EVDO_A, TelephonyManager.NETWORK_TYPE_HSDPA, TelephonyManager.NETWORK_TYPE_HSUPA, TelephonyManager.NETWORK_TYPE_HSPA, TelephonyManager.NETWORK_TYPE_EVDO_B, TelephonyManager.NETWORK_TYPE_EHRPD, TelephonyManager.NETWORK_TYPE_HSPAP ->
                        /** 3G网络  */
                        return 5

                    TelephonyManager.NETWORK_TYPE_LTE ->
                        /** 4G网络  */
                        return 6
                }
            }

            /** 未知网络  */
            return -1
        }
    }
}