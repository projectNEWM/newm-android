import Foundation
import Kingfisher
import ModuleLinker
import Resolver

public class KingfisherErrorHandler: ImageDownloaderDelegate {
	@Injected var logger: ErrorReporting
	public init() {}
	
	public func imageDownloader(_ downloader: ImageDownloader, didFinishDownloadingImageForURL url: URL, with response: URLResponse?, error: Error?) {
		if let error {
			logger.logBreadcrumb("\(error)", level: .info)
		}
	}
}
