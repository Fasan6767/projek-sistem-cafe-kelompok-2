// Muhammad Adil Abdirrahim
// 225443042
package com.kopikita

import com.kopikita.service.KafeService
import com.kopikita.ui.KafeUI

fun main() {
    val kafeService = KafeService()
    val kafeUI = KafeUI(kafeService)
    kafeUI.mulai()
}