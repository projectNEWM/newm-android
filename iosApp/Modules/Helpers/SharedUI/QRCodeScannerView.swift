import Foundation
import SwiftUI
import QRCodeReader
import AVFoundation

public struct QRCodeScannerView: UIViewControllerRepresentable {
	public class Coordinator: NSObject, QRCodeReaderViewControllerDelegate {
		public var parent: QRCodeScannerView

		public init(parent: QRCodeScannerView) {
			self.parent = parent
		}

		public func reader(_ reader: QRCodeReaderViewController, didScanResult result: QRCodeReaderResult) {
			parent.completion(.success(result.value))
		}

		public func readerDidCancel(_ reader: QRCodeReaderViewController) {
			parent.completion(.failure(CancellationError()))
		}
	}
	
	let completion: (Result<String, Error>) -> ()
	
	public init(completion: @escaping (Result<String, Error>) -> ()) {
		self.completion = completion
	}

	public func makeCoordinator() -> Coordinator {
		return Coordinator(parent: self)
	}

	public func makeUIViewController(context: Context) -> QRCodeReaderViewController {
		let reader = QRCodeReaderViewController(builder: QRCodeReaderViewControllerBuilder {
			$0.reader = QRCodeReader(metadataObjectTypes: [AVMetadataObject.ObjectType.qr])
			$0.reader.previewLayer.backgroundColor = UIColor.white.cgColor
			$0.showCancelButton = false
			$0.showTorchButton = false
			$0.showSwitchCameraButton = false
		})
		reader.delegate = context.coordinator
		return reader
	}

	public func updateUIViewController(_ uiViewController: QRCodeReaderViewController, context: Context) {}
}
