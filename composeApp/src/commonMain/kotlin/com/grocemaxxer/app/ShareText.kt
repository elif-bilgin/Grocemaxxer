package com.grocemaxxer.app

/**
 * Hands [text] to the platform's share mechanism: the share sheet on
 * Android (SMS/iMessage/WhatsApp/etc.), the clipboard on desktop and iOS.
 */
expect fun shareListText(text: String)
