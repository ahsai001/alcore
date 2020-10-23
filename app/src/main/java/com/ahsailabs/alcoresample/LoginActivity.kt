package com.ahsailabs.alcoresample

import com.ahsailabs.alcore.activities.BaseLoginActivity
import com.ahsailabs.alcore.interfaces.LoginCallbackResult
import com.ahsailabs.alutils.HttpClientUtil
import org.json.JSONObject
import java.util.HashMap

/**
 * Created by ahmad s on 22/10/20.
 */
class LoginActivity:BaseLoginActivity() {
    override fun getUserIdInvalidMessage(): String? {
        return null
    }

    override fun getLoginExplaination(): String? {
        return null
    }

    override fun clearAllCache(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getLoginUrl(): String {
        return ""
    }

    override fun getPasswordHint(): String? {
        TODO("Not yet implemented")
    }

    override fun getLoginTypeViewValueList(): HashMap<String, String>? {
        TODO("Not yet implemented")
    }

    override fun getLoginTypeFieldName(): String? {
        TODO("Not yet implemented")
    }

    override fun getCookedPassword(rawPassword: String?): String {
        TODO("Not yet implemented")
    }

    override fun isHandleCustomLogin(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getIconResId(): Int {
        TODO("Not yet implemented")
    }

    override fun getAPIVersion(): String? {
        TODO("Not yet implemented")
    }

    override fun getAuthType(): HttpClientUtil.AuthType? {
        return HttpClientUtil.AuthType.NONE
    }

    override fun getHttpBuilderConfig(): HttpClientUtil.BuilderConfig? {
        TODO("Not yet implemented")
    }

    override fun isHandleCustomSuccessResponse(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getPasswordFieldName(): String? {
        TODO("Not yet implemented")
    }

    override fun handleCustomData(data: JSONObject?) {
        TODO("Not yet implemented")
    }

    override fun getIconUrl(): String? {
        TODO("Not yet implemented")
    }

    override fun handleCustomSuccessResponse(
        response: JSONObject?,
        loginCallbackResult: LoginCallbackResult?
    ) {
        TODO("Not yet implemented")
    }

    override fun isUserIDValid(userId: String?): Boolean {
        TODO("Not yet implemented")
    }

    override fun isUsingErrorOfTextInputLayout(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getCustomHeaderMap(): MutableMap<String, String> {
        TODO("Not yet implemented")
    }

    override fun isMeidIncluded(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isPasswordValid(password: String?): Boolean {
        TODO("Not yet implemented")
    }

    override fun handleCustomLogin(
        appType: String?,
        username: String?,
        password: String?,
        loginCallbackResult: LoginCallbackResult?
    ) {
        TODO("Not yet implemented")
    }

    override fun getUserIdHint(): String? {
        TODO("Not yet implemented")
    }

    override fun getUserIdFieldName(): String? {
        TODO("Not yet implemented")
    }

    override fun getPasswordInvalidMessage(): String? {
        TODO("Not yet implemented")
    }

    override fun getDefaultValueLoginType(): String? {
        TODO("Not yet implemented")
    }

    override fun getButtonLoginText(): String {
        return "hello"
    }

}