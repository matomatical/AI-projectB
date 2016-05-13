
	/*
	minimax(last edge played, piece to play):
	
		// base cases
		if cutoff:
			return (null, winning piece) // really all we care about is who wins

		if timeout:
			throw timeout exception?
		
		result = null
		
		// recursive case
		for all edges => last edge played // (for all edges: if edge < last edge played: continue, ...)
			
			play edge
			
			pair = minimax(edge, (captured) ? piece : other piece)
			
			unplay edge

			if pair == null:
				// no pieces down this path! skip this edge
				continue
			
			pair.edge = this
			if pair.piece == piece
				return pair
			else // (pair.piece == other piece)
				// this move is not winning, continue searching
				result = pair
			
		// if we make it out of this loop, no pieces were winning for this player
		// (either there were no smaller edges or there were no winning edges)
		
		return result
		
		// (null if there were no possibilities)
	 */
