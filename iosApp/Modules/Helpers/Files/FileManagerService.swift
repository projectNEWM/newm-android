import Foundation
import Combine
import ModuleLinker
import shared
import Resolver

public class FileManagerService: ObservableObject {
	public typealias ProgressHandler = (Double) -> Void
	
	let downloadManager = DownloadManager()
	
	@Injected private var errorLogger: ErrorReporting
	private var cancels: Set<AnyCancellable> = []
	
	public init() {
		downloadManager.objectWillChange.sink { [weak self] in self?.objectWillChange.send() }.store(in: &cancels)
	}
	
	/// Returns the downloaded URL if available, otherwise the remote URL.
	public func getPlaybackURL(for downloadURL: URL) throws -> URL {
		return if fileExists(for: downloadURL) {
			try fileURL(forDownloadURL: downloadURL)
		} else {
			downloadURL
		}
	}
	
	public func fileExists(for url: URL) -> Bool {
		do {
			let url = try fileURL(forDownloadURL: url)
			return FileManager.default.fileExists(atPath: url.path)
		} catch {
			errorLogger.logError(error)
			return false
		}
	}
	
	public func clearFiles() {
		let fileManager = FileManager.default
		guard let documentsDirectory = fileManager.urls(for: .documentDirectory, in: .userDomainMask).first else { return }
		
		do {
			let fileURLs = try fileManager.contentsOfDirectory(at: documentsDirectory, includingPropertiesForKeys: nil)
			
			for fileURL in fileURLs {
				try fileManager.removeItem(at: fileURL)
			}
		} catch {
			errorLogger.logError("Error clearing documents directory: \(error)")
		}
	}
	
	public func clearFile(at url: URL) async {
		await Task {
			do {
				try FileManager.default.removeItem(at: try fileURL(forDownloadURL: url))
				objectWillChange.send()
			} catch {
				errorLogger.logError("Error clearing documents directory: \(error)")
			}
		}.value
	}
	
	public func download(url: URL, progressHandler: @escaping ProgressHandler) async throws {
		progressHandler(0)
		try await downloadManager.download(url: url, progressHandler: progressHandler)
	}
	
	public func cancelDownload(url: URL) {
		downloadManager.cancelDownload(url: url)
	}
}

func fileURL(forDownloadURL downloadURL: URL) throws -> URL {
	let allowedCharacters = CharacterSet(charactersIn: "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_.")
	let path = downloadURL.absoluteString.addingPercentEncoding(withAllowedCharacters: allowedCharacters)
	
	return FileManager.default.documentsDirectory.appendingPathComponent(path!)
}

extension FileManager {
	var documentsDirectory: URL {
		return FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0]
	}
}
