package test

import com.codeborne.selenide.Selenide.`$` as findElement

object SelenideUtils {
    fun submit() =
        findElement("button[type='submit']")
            .click()
}
