package com.grocemaxxer.app

import platform.UIKit.UIPasteboard

actual fun shareListText(text: String) {
    UIPasteboard.generalPasteboard.string = text
}
