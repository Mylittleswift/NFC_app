/*
See LICENSE folder for this sample’s licensing information.

Abstract:
Payload table view controller
*/

import UIKit
import CoreNFC

class PayloadsTableViewController: UITableViewController {

    // MARK: - Properties

    let reuseIdentifier = "reuseIdentifier"
    var payloads = [NFCNDEFPayload]()

    // MARK: - Table View Data Source

    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return payloads.count
    }

    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: reuseIdentifier, for: indexPath)

        let payload = payloads[indexPath.row]

        switch payload.typeNameFormat {
        case .nfcWellKnown:
            if let type = String(data: payload.type, encoding: .utf8) {
                cell.textLabel?.text = "NFC Well Known type: " + type
            } else {
                cell.textLabel?.text = "Invalid data"
            }
        case .absoluteURI:
            if let text = String(data: payload.payload, encoding: .utf8) {
                cell.textLabel?.text = text
            } else {
                cell.textLabel?.text = "Invalid data"
            }
        case .media:
            if let type = String(data: payload.type, encoding: .utf8) {
                cell.textLabel?.text = "Media type: " + type
            } else {
                cell.textLabel?.text = "Invalid data"
            }
        case .nfcExternal:
            cell.textLabel?.text = "NFC External type"
        case .unknown:
            cell.textLabel?.text = "Unknown type"
        case .unchanged:
            cell.textLabel?.text = "Unchanged type"
        default:
            cell.textLabel?.text = "Invalid data"
        }

        return cell
    }

}
