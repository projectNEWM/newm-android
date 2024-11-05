import Foundation
import Combine
import ModuleLinker
import Resolver

enum DownloadManagerError: Error {
	case canceledUnknownURL(URL)
}

class DownloadManager: NSObject, ObservableObject {
	@Published private(set) var downloads: [URL: URLSessionDownloadTask] = [:]
	@Published private(set) var progressHandlers: [URL: (Double) -> Void] = [:]
	
	private var urlSession: URLSession!
	private var downloadCompletionHandlers: [URL: (Result<URL, Error>) -> Void] = [:]
	
	@Injected private var logger: any ErrorReporting
	
	override init() {
		super.init()
		urlSession = URLSession(configuration: .default, delegate: self, delegateQueue: OperationQueue.main)
	}
	
	func download(url: URL, completion: @escaping (Result<URL, Error>) -> Void, progressHandler: @escaping (Double) -> Void) {
		guard downloads[url] == nil else { return }
		
		let task = urlSession.downloadTask(with: url)
		downloads[url] = task
		downloadCompletionHandlers[url] = completion
		progressHandlers[url] = progressHandler
		task.resume()
	}
	
	@discardableResult
	func download(url: URL, progressHandler: @escaping (Double) -> Void) async throws -> URL {
		try await withCheckedThrowingContinuation { [weak self] continuation in
			guard let self else { fatalError() }
			download(url: url, completion: { [weak self] result in
				continuation.resume(with: result)
				self?.objectWillChange.send()
			}, progressHandler: progressHandler)
		}
	}
	
	func cancelDownload(url: URL) {
		guard let download = downloads[url] else {
			logger.logError(DownloadManagerError.canceledUnknownURL(url))
			return
		}
		download.cancel()
		objectWillChange.send()
	}
	
	private func cleanUpDownload(url: URL) {
		downloadCompletionHandlers.removeValue(forKey: url)
		progressHandlers.removeValue(forKey: url)
		downloads.removeValue(forKey: url)
		objectWillChange.send()
	}
	
	private func handleDownloadCompletion(remoteUrl: URL, tmpLocalUrl: URL?, error: Error?) {
		defer {
			cleanUpDownload(url: remoteUrl)
		}

		if let error {
			let userDidCancelCode = -999
			if (error as NSError).code != userDidCancelCode {
				logger.logError(error)
			}
			downloadCompletionHandlers[remoteUrl]?(.failure(error))
			return
		}
		
		guard let tmpLocalUrl else {
			let error = URLError(.cannotCreateFile)
			logger.logError(error)
			downloadCompletionHandlers[remoteUrl]?(.failure(error))
			return
		}
		
		do {
			let persistentUrl = try fileURL(forDownloadURL: remoteUrl)
			try FileManager.default.moveItem(at: tmpLocalUrl, to: persistentUrl)
			downloadCompletionHandlers[remoteUrl]?(.success(persistentUrl))
		} catch {
			logger.logError(error)
			downloadCompletionHandlers[remoteUrl]?(.failure(error))
		}
	}
}

extension DownloadManager: URLSessionDownloadDelegate {
	func urlSession(_ session: URLSession, downloadTask: URLSessionDownloadTask, didFinishDownloadingTo location: URL) {
		guard let url = downloadTask.originalRequest?.url else { return } //NFTTrack.songUrl
		handleDownloadCompletion(remoteUrl: url, tmpLocalUrl: location, error: nil)
	}
	
	func urlSession(_ session: URLSession, downloadTask: URLSessionDownloadTask, didWriteData bytesWritten: Int64, totalBytesWritten: Int64, totalBytesExpectedToWrite: Int64) {
		guard let url = downloadTask.originalRequest?.url else { return }
		let progress = Double(totalBytesWritten) / Double(totalBytesExpectedToWrite)
		progressHandlers[url]?(progress)
	}
	
	func urlSession(_ session: URLSession, task: URLSessionTask, didCompleteWithError error: Error?) {
		guard let url = task.originalRequest?.url else { return }
		/// If there's no error, then urlSession(session: URLSession, downloadTask: URLSessionDownloadTask, didFinishDownloadingTo location: URL) will call handleDownloadCompletion above.
		if let error {
			handleDownloadCompletion(remoteUrl: url, tmpLocalUrl: nil, error: error)
		} else {
			/// This gets called in handleDownloadCompletion if there's an error.
			objectWillChange.send()
		}
	}
}
