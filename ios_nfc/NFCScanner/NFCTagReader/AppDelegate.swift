/*
See LICENSE folder for this sample’s licensing information.

Abstract:
Main application delegate
*/

import UIKit

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {
    var window: UIWindow?

    func application(_ application: UIApplication,
                     continue userActivity: NSUserActivity,
                     restorationHandler: @escaping ([Any]?) -> Void) -> Bool {
        guard userActivity.activityType == NSUserActivityTypeBrowsingWeb else {
            return false
        }

        // Confirm that the NSUserActivity object contains a valid NDEF message.
        let ndefMessage = userActivity.ndefMessagePayload
        guard ndefMessage.records.count > 0,
            ndefMessage.records[0].typeNameFormat != .empty else {
                return false
        }

        // Send the message to `MessagesTableViewController` for processing.
        guard let navigationController = window?.rootViewController as? UINavigationController else {
            return false
        }

        navigationController.popToRootViewController(animated: true)
        let messageTableViewController = navigationController.topViewController as? MessagesTableViewController
        messageTableViewController?.addMessage(fromUserActivity: ndefMessage)

        return true
    }
}
