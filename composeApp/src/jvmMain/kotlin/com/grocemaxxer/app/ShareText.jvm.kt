package com.grocemaxxer.app

import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

actual fun shareListText(text: String) {
    Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(text), null)
}
