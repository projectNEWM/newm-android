import Foundation
import Combine

class DownloadManager: NSObject, ObservableObject {
	@Published private(set) var downloads: [URL: URLSessionDownloadTask] = [:]
	private var progressHandlers: [URL: (Double) -> Void] = [:]
	
	private var urlSession: URLSession!
	private var downloadCompletionHandlers: [URL: (Result<URL, Error>) -> Void] = [:]
	
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
	
	func download(url: URL, progressHandler: @escaping (Double) -> Void) async throws -> URL {
		try await withCheckedThrowingContinuation { [weak self] continuation in
			guard let self else { fatalError() }
			download(url: url) { result in
				continuation.resume(with: result)
			} progressHandler: { progress in
//				print("\n\n\nCANCELLED\n\n\n")
//				self.downloads[url]?.cancel()
//				self.handleDownloadCompletion(remoteUrl: url, tmpLocalUrl: nil, error: CancellationError())
//				return
				progressHandler(progress)
			}
		}
	}
	
	private func handleDownloadCompletion(remoteUrl: URL, tmpLocalUrl: URL?, error: Error?) {
		defer {
			downloadCompletionHandlers.removeValue(forKey: remoteUrl)
			downloads.removeValue(forKey: remoteUrl)
		}
		
		if let error = error {
			downloadCompletionHandlers[remoteUrl]?(.failure(error))
			return
		}
		
		guard let tmpLocalUrl else {
			downloadCompletionHandlers[remoteUrl]?(.failure(URLError(.cannotCreateFile)))
			return
		}
		
		let persistentUrl = fileURL(forDownloadURL: remoteUrl)
		do {
			try FileManager.default.moveItem(at: tmpLocalUrl, to: persistentUrl)
			downloadCompletionHandlers[remoteUrl]?(.success(persistentUrl))
		} catch {
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
		self.progressHandlers[url]?(progress)
	}
}