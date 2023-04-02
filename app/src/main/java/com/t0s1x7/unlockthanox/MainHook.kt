package com.t0s1x7.unlockthanox

import android.app.Activity
import android.app.AlertDialog
import android.app.Application
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.Keep
import androidx.core.content.ContextCompat.startActivity
import de.robv.android.xposed.*
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import org.json.JSONException
import org.json.JSONObject
import java.io.File

@Keep
class MainHook : IXposedHookLoadPackage {
    override fun handleLoadPackage(lpparam: LoadPackageParam?) {
        val jsstr =
            "{\"3.5-prc\": \"ia.k\", \"3.6-prc\": \"ia.k\", \"3.7-prc\": \"ia.k\", \"3.7.1-prc\": \"ia.k\", \"3.7.3-prc\": \"ia.k\", \"3.8.1-prc\": \"ia.k\", \"3.9-prc\": \"ha.k\", \"3.9.1-prc\": \"ha.k\", \"3.9.3-prc\": \"x9.m\", \"3.9.4-prc\": \"z9.l\", \"3.9.5-prc\": \"ta.l\", \"3.9.6-prc\": \"hb.m\", \"3.9.7-prc\": \"gb.m\", \"3.9.8-1-72ef6f2-prc\": \"gb.m\", \"4.0.0-beta1-prc\": \"vb.l\", \"3.9.9.2-prc\": \"vb.l\", \"4.0.0-prc\": \"hc.l\", \"4.0.2-prc\": \"jc.l\", \"4.0.3-prc\": \"ic.l\", \"4.0.4-prc\": \"xb.l\", \"4.0.5-prc\": \"ub.q\", \"4.0.6-prc\": \"wb.p\", \"4.0.7-prc\": \"vb.p\", \"4.0.8-prc\": \"wb.q\", \"4.0.9-prc\": \"wb.r\", \"4.1.0-prc\": \"ac.r\", \"4.1.1-prc\": \"ac.r\", \"4.1.2-prc\": \"tc.r\", \"4.1.4-prc\": \"uc.r\"}"
        val map = getMap(jsstr)
        if (lpparam?.packageName == "github.tornaco.android.thanos") {
            val versionName = getPackageVersion(lpparam)
            val init = lpparam.classLoader.loadClass("github.tornaco.android.thanos.app.Init")
            try {
                val purchase = lpparam.classLoader.loadClass(map?.get(versionName).toString())
                XposedHelpers.findAndHookMethod(
                    purchase,
                    "e",
                    Context::class.java,
                    object : XC_MethodReplacement() {
                        override fun replaceHookedMethod(param: MethodHookParam?): Any {
                            return true
                        }

                    })
                XposedHelpers.findAndHookMethod(
                    init,
                    "a",
                    Application::class.java,
                    object : XC_MethodReplacement() {
                        override fun replaceHookedMethod(param: MethodHookParam?): Any {
                            val context = param?.args?.get(0) as Context
                            myToast(
                                context,
                                "已解锁灭霸高级版\n当前版本：$versionName",
                                Toast.LENGTH_SHORT
                            )
                            return 0
                        }
                    }
                )
            } catch (e: Throwable) {
                XposedHelpers.findAndHookMethod(
                    Activity::class.java, "onCreate",
                    Bundle::class.java, object : XC_MethodHook() {
                        @Throws(Throwable::class)
                        override fun afterHookedMethod(param: MethodHookParam) {
                            val thisObject = param.thisObject as Activity
                            XposedBridge.log("当前 Activity : " + thisObject.javaClass.name)
                            val dialog: AlertDialog = AlertDialog.Builder(thisObject)
                                .setTitle("提示") //设置标题
                                .setMessage("插件不支持当前灭霸版本，需要更新") //设置要显示的内容
                                .setNegativeButton(
                                    "取消",
                                    DialogInterface.OnClickListener { dialogInterface, _ ->
                                        myToast(thisObject, "取消更新", Toast.LENGTH_SHORT)
                                        dialogInterface.dismiss() //销毁对话框
                                    })
                                .setPositiveButton(
                                    "更新",
                                    DialogInterface.OnClickListener { dialog, _ ->
                                        val uri: Uri = Uri.parse("http://www.baidu.com")
                                        val intent = Intent()
                                        intent.action = "android.intent.action.VIEW"
                                        intent.data = uri
                                        startActivity(thisObject,intent, null)
                                        dialog.dismiss() //销毁对话框
                                    }).create() //create（）方法创建对话框

                            dialog.show() //显示对话框

                        }
                    })
            }
        }
    }

    private fun myToast(context: Context, text: CharSequence, duration: Int) {
        Toast.makeText(context, text, duration).show()
    }

    // 获取目标应用 VersionName
    private fun getPackageVersion(lpparam: LoadPackageParam): String? {
        return try {
            val parserCls =
                XposedHelpers.findClass("android.content.pm.PackageParser", lpparam.classLoader)
            val parser = parserCls.newInstance()
            val apkPath = File(lpparam.appInfo.sourceDir)
            val pkg = XposedHelpers.callMethod(parser, "parsePackage", apkPath, 0)
            val versionName = XposedHelpers.getObjectField(pkg, "mVersionName") as String
            String.format("%s", versionName)
        } catch (e: Throwable) {
            "(unknown)"
        }
    }

    private fun getMap(jsonString: String): HashMap<String, Any>? {
        val jsonObject: JSONObject
        try {
            jsonObject = JSONObject(jsonString)
            val keyIter: Iterator<String> = jsonObject.keys()
            var key: String
            var value: Any
            val valueMap = HashMap<String, Any>()
            while (keyIter.hasNext()) {
                key = keyIter.next()
                value = jsonObject[key] as Any
                valueMap[key] = value
            }
            return valueMap
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return null
    }
}
