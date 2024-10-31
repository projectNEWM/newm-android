import Foundation
import shared
import Utilities

struct PlayQueue {
	var originalTracks: Set<NFTTrack> = [] {
		didSet {
			filterAndSortOriginalTracksIntoCurrentQueue()
		}
	}
	
	private var currentQueue: [NFTTrack] = []
	private var currentIndex: Int?
	
	var isEmpty: Bool {
		currentQueue.isEmpty
	}
	
	var hasNextTrack: Bool {
		indexForNextTrack(userInitiated: true) != nil || indexForNextTrack(userInitiated: false) != nil
	}
	
	var hasPrevTrack: Bool {
		indexForPrevTrack() != nil
	}
	
	private var filters = Filters() {
		didSet {
			applyFiltersToCurrentQueue()
		}
	}
	
	var textFilter: String? {
		get { filters.text }
		set {
			guard newValue != textFilter else { return }
			filters.text = newValue
		}
	}
	
	var durationFilter: Int? {
		get { filters.duration }
		set {
			guard newValue != durationFilter else { return }
			guard let newValue, newValue > 0 else {
				filters.duration = nil
				return
			}
			filters.duration = newValue
		}
	}
	
	@UserDefault(defaultValue: .artist(ascending: true)) var sortCriteria: Sort {
		didSet {
			applySortingToCurrentQueue()
		}
	}
	
	@UserDefault(defaultValue: false) var shuffle: Bool {
		didSet {
			guard oldValue != shuffle else { return }
			shuffle ? shuffleCurrentQueue() : unshuffleCurrentQueue()
		}
	}
	
	@UserDefault(defaultValue: .none) var repeatMode: RepeatMode
	
	mutating
	private func shuffleCurrentQueue() {
		if let currentIndex = currentIndex {
			let currentTrack = currentQueue[currentIndex]
			currentQueue.shuffle()
			currentQueue.firstIndex(of: currentTrack).flatMap { currentQueue.swapAt(0, $0) }
			self.currentIndex = 0
		}
	}
	
	mutating
	private func unshuffleCurrentQueue() {
		let currentTrack = currentIndex.flatMap { currentQueue.indices.contains($0) ? currentQueue[$0] : nil }
		filterAndSortOriginalTracksIntoCurrentQueue()
		currentIndex = currentTrack.flatMap { currentQueue.firstIndex(of: $0) }
	}
	
	mutating
	private func filterAndSortOriginalTracksIntoCurrentQueue() {
		currentQueue = Array(originalTracks)
		applyFiltersToCurrentQueue()
		applySortingToCurrentQueue()
	}
	
	func currentTrack() throws -> NFTTrack? {
		guard let currentIndex else { return nil }
		guard currentQueue.indices.contains(currentIndex) else { throw PlayQueueError.invalidIndex }
		return currentQueue[currentIndex]
	}
	
	mutating
	func seekToFirst() throws {
		guard isEmpty == false else { throw PlayQueueError.queueIsEmpty }
		currentIndex = 0
	}
	
	mutating
	func seekToTrack(_ track: NFTTrack) throws {
		guard isEmpty == false else { throw PlayQueueError.queueIsEmpty }
		guard let currentIndex = currentQueue.firstIndex(of: track) else { throw PlayQueueError.trackNotInQueue }
		self.currentIndex = currentIndex
	}
	
	private func indexForNextTrack(userInitiated: Bool) -> Int? {
		guard !currentQueue.isEmpty, let oldIndex = currentIndex else { return nil }
		let newIndex: Int
		switch repeatMode {
		case .all:
			newIndex = (oldIndex + 1) % currentQueue.count
		case .one:
			if userInitiated {
				fallthrough
			} else {
				newIndex = oldIndex
			}
		case .none:
			newIndex = oldIndex + 1
		}
		return currentQueue.indices.contains(newIndex) ? newIndex : nil
	}
	
	@discardableResult
	mutating
	func nextTrack(userInitiated: Bool = false) -> NFTTrack? {
		guard !currentQueue.isEmpty else { return nil }
		
		if let newIndex = indexForNextTrack(userInitiated: userInitiated) {
			currentIndex = newIndex
			return currentQueue[newIndex]
		} else {
			currentIndex = nil
			return nil
		}
	}
	
	private func indexForPrevTrack() -> Int? {
		guard !currentQueue.isEmpty, let oldIndex = currentIndex else { return nil }
		
		let newIndex: Int
		switch repeatMode {
		case .none, .one:
			newIndex = max(0, oldIndex - 1)
		case .all:
			newIndex = (oldIndex - 1 + currentQueue.count) % currentQueue.count
		}
		return currentQueue.indices.contains(newIndex) ? newIndex : nil
	}
	
	@discardableResult
	mutating
	func previousTrack() -> NFTTrack? {
		if let newIndex = indexForPrevTrack() {
			currentIndex = newIndex
			return currentQueue[newIndex]
		} else {
			currentIndex = nil
			return nil
		}
	}
	
	mutating
	private func applyFiltersToCurrentQueue() {
		guard let filter = filters.filter else { return }
		let prevItem = try? currentTrack()
		currentQueue = Array(originalTracks).filter(filter)
		if let prevItem {
			currentIndex = currentQueue.firstIndex(of: prevItem)
		}
	}
	
	mutating
	private func applySortingToCurrentQueue() {
		let prevItem = try? currentTrack()
		currentQueue.sort(by: sortCriteria.comparator)
		if let prevItem {
			currentIndex = currentQueue.firstIndex(of: prevItem)
		}
		print("sorted: \(currentQueue.map(\.title))")
	}
	
	mutating
	func cycleRepeatMode() {
		repeatMode = switch repeatMode {
		case .all:
				.one
		case .one:
				.none
		case .none:
				.all
		}
	}
}

extension PlayQueue {
	enum PlayQueueError: Error {
		case queueIsEmpty
		case invalidIndex
		case trackNotInQueue
		
		var errordDescription: String? {
			"There was an error playing the song."
		}
	}
}
